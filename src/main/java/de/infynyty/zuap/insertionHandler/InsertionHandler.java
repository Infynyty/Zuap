package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.Zuap;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Level;

@RequiredArgsConstructor
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
    private final String handlerName;
    @NotNull
    private final InsertionAnnouncer announcer;
    private boolean isInitialized = false;

    /**
     * Update the html data containing all insertions.
     *
     * @return The updated html.
     * @throws IOException
     * @throws InterruptedException
     */
    protected abstract String pullUpdatedData() throws IOException, InterruptedException;

    /**
     * Parses the entire data file, so that all insertions are read into {@link Insertion} objects.
     *
     * @param data The data containing all insertions.
     * @return A list containing all parsed insertions.
     * @throws IllegalStateException If the link to a given insertion cannot be parsed an exception is thrown because
     *                               the links are considered critical information for an {@link Insertion} object.
     */
    protected abstract ArrayList<Insertion> getInsertionsFromData(final String data) throws IllegalStateException;

    /**
     * Updates the currently saved insertions. Online changes to insertions will be mirrored locally in
     * {@link InsertionHandler#currentInsertions}.
     */
    public void updateCurrentInsertions() {
        //clear local list of updated insertions every time this method is called
        updatedInsertions.clear();
        try {
            final String updatedData = pullUpdatedData();
            updatedInsertions.addAll(getInsertionsFromData(updatedData));
        } catch (IOException | InterruptedException | IllegalStateException e) {
            Zuap.log(Level.SEVERE, handlerName, "An exception occurred while trying to update the insertions. " + e.getMessage());
            return;
        }
        if (!isInitialized) {
            addInitialInsertions();
            announcer.announce(currentInsertions.get(0));
            return;
        }
        addNewInsertions();
        removeDeletedInsertions();
        Zuap.log(
                Level.INFO,
                handlerName,
                "Insertions updated at " + Date.from(Instant.now()) + ", numbers of insertions: " + updatedInsertions.size()
        );
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
            Zuap.log(Level.INFO, handlerName, "One or more insertions were removed.");
        }
    }

    /**
     * Add every new insertion to {@link InsertionHandler#currentInsertions a local list}.
     */
    private void addNewInsertions() {
        for (final Insertion updatedInsertion : updatedInsertions) {
            if (!(currentInsertions.contains(updatedInsertion))) {
                currentInsertions.add(updatedInsertion);
                announcer.announce(updatedInsertion);
            }
        }
    }

    /**
     * Adds all insertions without checking for duplicates, if there are no insertions saved locally.
     */
    private void addInitialInsertions() {
        isInitialized = true;
        currentInsertions.addAll(updatedInsertions);
        Zuap.log(Level.INFO, handlerName, "Initial download of all insertions complete.");
    }

    public @NotNull String getHandlerName() {
        return handlerName;
    }
}
