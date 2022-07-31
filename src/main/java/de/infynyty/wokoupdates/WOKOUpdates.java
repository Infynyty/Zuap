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
import org.jetbrains.annotations.Nullable;

@Log
public class WOKOUpdates {
    final static Dotenv dotenv = Dotenv.load();

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
            final ArrayList<Insertion> insertions = Insertion.getAllInsertions(responseText);

            if (currentInsertions.isEmpty()) {
                currentInsertions.addAll(insertions);
                log.info("Initial download of all insertions completed successfully!");
                currentInsertions.forEach(insertion -> System.out.println(insertion.toString()));
            }

            for (final Insertion updatedInsertion : currentInsertions) {
                if (!(currentInsertions.contains(updatedInsertion))) {
                    log.info("New insertion found:\n\n" + updatedInsertion.toString());
                    jda.getTextChannels().get(0).sendMessage("**New insertion found:**\n" + updatedInsertion).queue();
                    if(updatedInsertion.isNextTenantWanted() && updatedInsertion.getRent() < 650) {
                        jda.getTextChannels().get(0).sendMessage(dotenv.get("PING")).queue();
                    }
                }
            }
            log.info(
                "Insertions updated at " + Date.from(Instant.now()) + ", numbers of insertions: " + insertions.size()
            );
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
        return jda;
    }
}
