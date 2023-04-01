package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.Zuap;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public abstract class InsertionHandler<Insertion extends de.infynyty.zuap.insertion.Insertion> {

    /**
     * Contains all locally saved insertions.
     */
    private final ArrayList<Insertion> currentInsertions = new ArrayList<>();
    /**
     * Used to compare updated insertions with locally saved info. Should be empty before updating local insertions.
     */
    private final ArrayList<Insertion> updatedInsertions = new ArrayList<>();

    @NotNull
    private final JDA jda;
    @NotNull
    private final String logPrefix;

    /**
     * Creates an insertion handler for a new website.
     *
     * @param jda        A reference to the discord bot.
     * @param logPrefix
     */
    protected InsertionHandler(@NotNull final JDA jda, @NotNull final String logPrefix) {
        this.jda = jda;
        this.logPrefix = logPrefix;
    }

    /**
     * Update the html data containing all insertions.
     *
     * @return The updated html.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    protected abstract String pullUpdatedData() throws IOException, InterruptedException;

    /**
     * Parses the entire data file, so that all insertions are read into {@link Insertion} objects.
     *
     * @param data The data containing all insertions.
     *
     * @return A list containing all parsed insertions.
     *
     * @throws IllegalStateException If the link to a given insertion cannot be parsed an exception is thrown because
     *                               the links are considered critical information for an {@link Insertion} object.
     */
    protected abstract ArrayList<Insertion> getInsertionsFromData(final String data) throws IllegalStateException;

    /**
     * Updates the currently saved insertions. Online changes to insertions will be mirrored locally in
     * {@link InsertionHandler#currentInsertions}.
     *
     * @throws InterruptedException
     */
    public void updateCurrentInsertions() throws InterruptedException {
        //clear local list of updated insertions every time this method is called
        updatedInsertions.clear();
        parseUpdatedInsertions();

        if (currentInsertions.isEmpty()) {
            final EmbedBuilder initalMessage = new EmbedBuilder();
            initalMessage.setTitle("Loading...")
                    .setColor(Color.ORANGE)
                    .addField("Loading " + logPrefix + ".....................", " ", true)
                    .addBlankField(true)
                    .addField(":arrows_counterclockwise:", " ", false);
            final EmbedBuilder updatedMessage = new EmbedBuilder();
            updatedMessage.setTitle("Done")
                    .setColor(Color.GREEN)
                    .addField("Loading " + logPrefix + ".....................", " ", true)
                    .addBlankField(true)
                    .addField(":white_check_mark:", " ", false);
            jda.getChannelById(TextChannel.class, Zuap.getLogChannelId()).sendMessageEmbeds(initalMessage.build()).queue(
                    message -> {
                        addInitialInsertions();
                        message.editMessageEmbeds(updatedMessage.build()).queue();
                    },
                    message -> {
                        updatedInsertions.clear();
                        currentInsertions.clear();
                        Zuap.log(Level.SEVERE, "Could not update loading message for discord. Initializing insertions anyway.");
                        addInitialInsertions();
                    }
            );
            return;
        }
        addNewInsertions();
        removeDeletedInsertions();

        logUpdates(
            Level.INFO,
            "Insertions updated at " + Date.from(Instant.now()) + ", numbers of insertions: " + updatedInsertions.size(),
            Zuap.getLogChannelId()
        );
    }

    /**
     * Logs any information on discord and using the Java Logger.
     *
     * @param level        The level of the information.
     * @param logText      The message.
     * @param logChannelId The discord channel that should be used to post this information.
     */
    private void logUpdates(final Level level, final String logText, final long logChannelId) {
        Zuap.log(level, logPrefix + logText);
        if (jda.getChannelById(TextChannel.class, logChannelId) == null) {
            Zuap.log(Level.SEVERE, "Discord channel could not be found!");
            return;
        }
        jda.getChannelById(TextChannel.class, logChannelId).sendMessage(logPrefix + logText).queue();
    }

    private void logUpdates(final Level level, final Insertion insertion, final long logChannelId) {
        Zuap.log(level, logPrefix + "Found new insertion: " + insertion.toString());
        if (jda.getChannelById(TextChannel.class, logChannelId) == null) {
            Zuap.log(Level.SEVERE, "Discord channel could not be found!");
            return;
        }
        jda.getChannelById(TextChannel.class, logChannelId).sendMessage(insertion.toMessage()).queue();
    }

    /**
     * Removes any local insertion that does not exist online anymore.
     */
    private void removeDeletedInsertions() {
        // go through all current insertions and check that they are still in the updated insertions
        // remove them, if that isn't the case
        final boolean wasRemoved = currentInsertions.removeIf(
            currentInsertion -> (!(updatedInsertions.contains(currentInsertion)))
        );
        if (wasRemoved) {
            logUpdates(Level.INFO, "One or more insertions were removed.", Zuap.getLogChannelId());
        }
    }

    /**
     * Add every new insertion to {@link InsertionHandler#currentInsertions a local list}.
     */
    private void addNewInsertions() {
        for (final Insertion updatedInsertion : updatedInsertions) {
            if (!(currentInsertions.contains(updatedInsertion))) {
                currentInsertions.add(updatedInsertion);
                logUpdates(
                    Level.INFO,
                    updatedInsertion,
                    Zuap.getMainChannelId()
                );
            }
        }
    }

    /**
     * Adds all insertions without checking for duplicates, if there are no insertions saved locally.
     */
    private void addInitialInsertions() {
        // go through all new insertions and check whether they are contained in the current insertions
        // update, if that isn't the case
        currentInsertions.addAll(updatedInsertions);
        Zuap.log(Level.INFO, logPrefix + "Initial download of all insertions complete.");
        logUpdates(
                Level.INFO,
                currentInsertions.get(0),
                Zuap.getLogChannelId());
    }

    /**
     * Tries to read all insertions from the updated html file. On failure retries happen automatically after
     * {@link Zuap#UPDATE_DELAY_IN_MINS}.
     *
     * @throws InterruptedException
     */
    private void parseUpdatedInsertions() throws InterruptedException {
        try {
            updatedInsertions.addAll(getInsertionsFromData(pullUpdatedData()));
        } catch (IOException | InterruptedException | IllegalStateException e) {
            logUpdates(
                Level.SEVERE,
                "An exception occurred while trying to update the insertions. " +
                    e.getMessage() +
                    "Retrying in " + Zuap.UPDATE_DELAY_IN_MINS + " minutes.",
                Zuap.getLogChannelId()
            );
            TimeUnit.MINUTES.sleep(Zuap.UPDATE_DELAY_IN_MINS);
            parseUpdatedInsertions();
        }
    }
}
