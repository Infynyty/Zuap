package de.infynyty.zuap;

import javax.security.auth.login.LoginException;

import java.util.concurrent.TimeUnit;

import de.infynyty.zuap.insertionHandler.MeinWGZimmerHandler;
import de.infynyty.zuap.insertionHandler.WGZimmerHandler;
import de.infynyty.zuap.insertionHandler.WOKOInsertionHandler;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

@Log
public class WOKOUpdates {

    public final static long MAIN_CHANNEL_ID = 1002178166112137249L;
    public final static long LOG_CHANNEL_ID = 1004378115751030885L;
    public final static int UPDATE_DELAY_IN_MINS = 5;

    private final static Dotenv dotenv = Dotenv.load();

    public static void main(String[] args) throws InterruptedException {
        final JDA jda = prepareDiscordBot();
        if (jda == null) return;

        parseWebsiteData(jda);
    }

    private static void parseWebsiteData(final JDA jda) throws InterruptedException {

        final WOKOInsertionHandler wokoInsertionHandler = new WOKOInsertionHandler(jda, dotenv, "WOKO: ");
        final WGZimmerHandler wgZimmerHandler = new WGZimmerHandler(jda, dotenv, "WGZimmer: ");
        final MeinWGZimmerHandler meinWGZimmerHandler = new MeinWGZimmerHandler(jda, dotenv, "MeinWGZimmer: ");

        while (true) {
            wokoInsertionHandler.updateCurrentInsertions();
            wgZimmerHandler.updateCurrentInsertions();
            meinWGZimmerHandler.updateCurrentInsertions();
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
