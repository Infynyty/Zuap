package de.infynyty.zuap.discord;

import de.infynyty.zuap.Zuap;
import de.infynyty.zuap.insertion.Insertion;
import de.infynyty.zuap.insertionHandler.InsertionAnnouncer;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.LoginException;
import java.util.logging.Level;

/**
 * This class handles all logic regarding the Discord bot.
 */
@RequiredArgsConstructor
public class DiscordHandler implements InsertionAnnouncer {


    private final static Dotenv dotenv = Dotenv.load();

    /** The id of the channel that new insertions should be posted to. **/
    private final long mainChannelID;

    @Nullable
    private JDA jda;

    /**
     * Initializes the connection to the Discord bot.
     * @return A JDA object to use for further Discord related activities.
     * @throws InterruptedException
     * @throws LoginException If the Discord API token is invalid.
     */
    @NotNull
    public JDA prepareDiscordBot() throws InterruptedException, LoginException {
        final JDA jda;
        try {
            jda = JDABuilder.createDefault(dotenv.get("TOKEN")).build();
        } catch (LoginException e) {
            Zuap.log(Level.SEVERE, "JDA login failed");
            throw new LoginException("JDA login failed");
        }
        jda.awaitReady();
        this.jda = jda;
        Zuap.log(Level.INFO,"Bot online.");
        return jda;
    }

    /**
     * Announces an insertion on the Discord server.
     * @param insertion The insertion to announce, which must not be null.
     */
    @Override
    public void announce(@NotNull final Insertion insertion) {
        if (jda == null) {
            Zuap.log(Level.SEVERE, "Cannot display new insertion on Discord, because the JDA has not been initialized.");
            return;
        }
        final TextChannel channel = jda.getChannelById(TextChannel.class, mainChannelID);
        if (channel == null) {
            Zuap.log(Level.SEVERE,"Cannot log to the Discord logging channel using the unknown channel id: " + mainChannelID);
            return;
        }
        try {
            channel.sendMessage(insertion.toMessage()).queue();
        } catch (Exception ex) {
            Zuap.log(Level.SEVERE, "Cannot display new insertion on Discord, because an error occurred while sending the message.");
            Zuap.log(Level.SEVERE, ex.getMessage());
        }
    }
}
