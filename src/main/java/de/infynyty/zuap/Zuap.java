package de.infynyty.zuap;

import de.infynyty.zuap.discord.DiscordHandler;
import de.infynyty.zuap.discord.DiscordLoggingHandler;
import de.infynyty.zuap.insertion.Insertion;
import de.infynyty.zuap.insertionHandler.*;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;

@Log
public class Zuap {

    public final static int UPDATE_DELAY_IN_MINS = 5;

    private final static Dotenv dotenv = Dotenv.load();

    private final static ArrayList<InsertionHandler<? extends Insertion>> handlers = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException, LoginException, IOException {
        final DiscordHandler discordHandler = new DiscordHandler(getMainChannelId());
        final JDA jda = discordHandler.prepareDiscordBot();

        log.addHandler(new FileHandler("Zuap.log", 1000000, 1, true));
        log.addHandler(new DiscordLoggingHandler(getLogChannelId(), jda));
        parseWebsiteData(discordHandler);
    }

    private static void parseWebsiteData(final InsertionAnnouncer announcer){
        final HttpClient httpClient = HttpClient.newHttpClient();
        handlers.add(new WOKOInsertionHandler("WOKO", announcer, httpClient));
        handlers.add(new MeinWGZimmerHandler("MeinWGZimmer", announcer, httpClient));
        handlers.add(new FlatfoxHandler("Flatfox", announcer, httpClient));
        if (!getNoWGZimmer()) {
            handlers.add(new WGZimmerHandler("WGZimmer", announcer, httpClient));
        }


        handlers.forEach(handler -> {
            final Thread t = new Thread(() -> {
                Zuap.log(Level.CONFIG, "Started new thread for " + handler.getClass());
                while (true) {
                    try {
                        handler.updateCurrentInsertions();
                        TimeUnit.MINUTES.sleep(UPDATE_DELAY_IN_MINS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            t.setName(handler.getHandlerName());
            t.start();
        });
    }

    public static boolean getNoWGZimmer() {
        return Boolean.parseBoolean(dotenv.get("NO_WGZIMMER"));
    }

    public static long getLogChannelId() {
        return Long.parseLong(dotenv.get("LOG_CHANNEL_ID"));
    }

    public static long getMainChannelId() {
        return Long.parseLong(dotenv.get("MAIN_CHANNEL_ID"));
    }

    public static void log(final Level level, final String message) {
        log.log(level, message);
    }

    public static void log(final Level level, final String prefix, final String message) {
        log.log(level, prefix + ": " + message);
    }
}
