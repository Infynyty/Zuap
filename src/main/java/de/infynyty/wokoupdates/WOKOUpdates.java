package de.infynyty.wokoupdates;

import javax.security.auth.login.LoginException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

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
        final HttpClient wgZimmerClient = HttpClient.newHttpClient();
        HttpRequest wgZimmerRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://www.wgzimmer.ch/wgzimmer/search/mate.html?"))
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("query=&priceMin=200&priceMax=650&state=all&permanent=all&student=true&typeofwg=all&orderBy=%40sortDate&orderDir=descending&startSearchMate=true&wgStartSearch=true&start=0"))
            .build();

        HttpResponse<byte[]> wgZimmerResponse = wgZimmerClient.send(wgZimmerRequest,
            HttpResponse.BodyHandlers.ofByteArray());

        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(wgZimmerResponse.body()));
        String output = new String(gzipInputStream.readAllBytes());
        System.out.println("Output: " + output);

        final WOKOInsertionHandler insertionHandler = new WOKOInsertionHandler(jda, dotenv);

        while (true) {
            insertionHandler.updateCurrentInsertions();
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
