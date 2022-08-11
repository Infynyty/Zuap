package de.infynyty.wokoupdates;

import javax.security.auth.login.LoginException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import de.infynyty.wokoupdates.insertion.Insertion;
import de.infynyty.wokoupdates.insertionHandler.WGZimmerHandler;
import de.infynyty.wokoupdates.insertionHandler.WOKOInsertionHandler;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

@Log
public class WOKOUpdates {

    public final static long MAIN_CHANNEL_ID = 1002178166112137249L;
    public final static long LOG_CHANNEL_ID = 1004378115751030885L;
    public final static int UPDATE_DELAY_IN_MINS = 15;

    private final static Dotenv dotenv = Dotenv.load();

    public static void main(String[] args) throws InterruptedException {
        final JDA jda = prepareDiscordBot();
        if (jda == null) return;

        parseWebsiteData(jda);
    }

    private static void parseWebsiteData(final JDA jda) throws InterruptedException {


        final WOKOInsertionHandler wokoInsertionHandler = new WOKOInsertionHandler(jda, dotenv);
        final WGZimmerHandler wgZimmerHandler = new WGZimmerHandler(jda, dotenv);

        while (true) {
            wokoInsertionHandler.updateCurrentInsertions();
            wgZimmerHandler.updateCurrentInsertions();
            TimeUnit.MINUTES.sleep(UPDATE_DELAY_IN_MINS);
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
