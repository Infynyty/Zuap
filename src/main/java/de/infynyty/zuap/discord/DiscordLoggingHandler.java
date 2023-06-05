package de.infynyty.zuap.discord;

import com.google.inject.Singleton;
import de.infynyty.zuap.Zuap;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

@RequiredArgsConstructor
public class DiscordLoggingHandler extends Handler {
    private final long logChannelID;
    @NotNull
    private final JDA jda;
    @Override
    public void publish(LogRecord record) {
        final TextChannel channel = jda.getChannelById(TextChannel.class, logChannelID);
        if (channel == null) {
            System.err.println("Cannot log to the Discord logging channel using the unknown channel id: " + logChannelID);
            return;
        }
        try {
            channel.sendMessage(record.getMessage()).queue();
        } catch (InsufficientPermissionException ex) {
            System.err.println("Cannot log to the Discord channel with the id: " + logChannelID + " because of insufficient permissions");
        } catch (IllegalArgumentException ex) {
            System.err.println("Cannot log to the Discord channel with the id: " + logChannelID + " because of an incorrectly formatted message");
        } catch (UnsupportedOperationException ex) {
            System.err.println("Cannot log to the Discord channel with the id: " + logChannelID + " because of an unsupported action");
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
