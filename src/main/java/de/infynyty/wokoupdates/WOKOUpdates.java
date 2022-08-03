package de.infynyty.wokoupdates;

import javax.security.auth.login.LoginException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

@Log
public class WOKOUpdates {
    final static Dotenv dotenv = Dotenv.load();

    private final static long MAIN_CHANNEL_ID = 1002178166112137249L;
    private final static long LOG_CHANNEL_ID = 1004378115751030885L;

    public static void main(String[] args) throws InterruptedException, IOException {
        final JDA jda = prepareDiscordBot();
        if (jda == null) return;

        final ArrayList<Insertion> currentInsertions = new ArrayList<>();
        parseWebsiteData(jda, currentInsertions);
    }

    private static void parseWebsiteData(JDA jda, ArrayList<Insertion> currentInsertions)
        throws IOException, InterruptedException {
        while (true) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.woko.ch/de/zimmer-in-zuerich"))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            final String responseText = response.body();
            final ArrayList<Insertion> updatedInsertions = Insertion.getAllInsertions(responseText);

            if (currentInsertions.isEmpty()) {
                currentInsertions.addAll(updatedInsertions);
                log.info("Initial download of all insertions completed successfully!");
                jda.getChannelById(TextChannel.class, LOG_CHANNEL_ID).sendMessage(
                    "Initial download of all insertions completed successfully!"
                ).queue();
                currentInsertions.forEach(insertion -> System.out.println(insertion.toString()));
            }

            // go through all new insertions and check whether they are contained in the current insertions
            // update, if that isn't the case
            for (final Insertion updatedInsertion : updatedInsertions) {
                if (!(currentInsertions.contains(updatedInsertion))) {
                    currentInsertions.add(updatedInsertion);
                    log.info("New insertion found:\n\n" + updatedInsertion.toString());
                    jda.getChannelById(TextChannel.class, MAIN_CHANNEL_ID).sendMessage(
                        "**New insertion found:**\n" + updatedInsertion
                    ).queue();
                    if(updatedInsertion.isNextTenantWanted() && updatedInsertion.getRent() < 650) {
                        jda.getChannelById(TextChannel.class, MAIN_CHANNEL_ID).sendMessage(dotenv.get("PING")).queue();
                    }
                }
            }

            // go through all current insertions and check that they are still in the updated insertions
            // remove them, if that isn't the case
            final boolean wasRemoved = currentInsertions.removeIf(
                currentInsertion -> (!(updatedInsertions.contains(currentInsertion)))
            );
            if (wasRemoved) {
                log.info("One or more insertions were removed.");
                jda.getChannelById(TextChannel.class, LOG_CHANNEL_ID).sendMessage(
                    "One or more insertions were removed."
                ).queue();
            }

            log.info(
                "Insertions updated at " + Date.from(Instant.now()) + ", numbers of insertions: " + updatedInsertions.size()
            );
            jda.getChannelById(TextChannel.class, LOG_CHANNEL_ID).sendMessage(
                "Insertions updated at " + Date.from(Instant.now()) + ", numbers of insertions: " + updatedInsertions.size()
            ).queue();
            TimeUnit.MINUTES.sleep(15);
        }
    }

    @Nullable
    private static JDA prepareDiscordBot() throws InterruptedException {
        final JDA jda;
        try {
            jda = JDABuilder.createDefault(dotenv.get("TOKEN")).build();
        } catch (LoginException e) {
            log.severe("JDA login failed");
            return null;
        }
        jda.awaitReady();
        log.info("JDA bot ready");
        jda.getChannelById(TextChannel.class, LOG_CHANNEL_ID).sendMessage(
            "Bot online."
        ).queue();
        return jda;
    }
}
