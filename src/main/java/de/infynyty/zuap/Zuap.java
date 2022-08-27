package de.infynyty.zuap;

import javax.security.auth.login.LoginException;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.infynyty.zuap.insertion.Insertion;
import de.infynyty.zuap.insertionHandler.InsertionHandler;
import de.infynyty.zuap.insertionHandler.MeinWGZimmerHandler;
import de.infynyty.zuap.insertionHandler.WGZimmerHandler;
import de.infynyty.zuap.insertionHandler.WOKOInsertionHandler;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log
public class Zuap {

    //TODO: Remove hardcoded Discord implementation
    public final static long MAIN_CHANNEL_ID = 1002178166112137249L;
    public final static long LOG_CHANNEL_ID = 1004378115751030885L;
    public final static int UPDATE_DELAY_IN_MINS = 5;

    private final static Dotenv dotenv = Dotenv.load();

    private final static ArrayList<InsertionHandler<? extends Insertion>> handlers = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException, LoginException {
        final JDA jda = prepareDiscordBot();
        parseWebsiteData(jda);
    }

    private static void parseWebsiteData(final JDA jda) {
        handlers.add(new WOKOInsertionHandler(jda, dotenv, "WOKO: "));
        handlers.add(new WGZimmerHandler(jda, dotenv, "WGZimmer: "));
        handlers.add(new MeinWGZimmerHandler(jda, dotenv, "MeinWGZimmer: "));

        handlers.forEach(handler -> new Thread(() -> {
            log.info("Started new thread for " + handler.getClass());
            while (true) {
                try {
                    handler.updateCurrentInsertions();
                    TimeUnit.SECONDS.sleep(UPDATE_DELAY_IN_MINS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start());
    }

    @NotNull
    private static JDA prepareDiscordBot() throws InterruptedException, LoginException {
        final JDA jda;
        try {
            jda = JDABuilder.createDefault(dotenv.get("TOKEN")).build();
        } catch (LoginException e) {
            log.severe("JDA login failed");
            throw new LoginException("JDA login failed");
        }
        jda.awaitReady();
        log.info("JDA bot ready");
        jda.getChannelById(TextChannel.class, LOG_CHANNEL_ID).sendMessage(
            "Bot online."
        ).queue();
        return jda;
    }
}
