package de.infynyty.wokoupdates.insertionHandler;

import de.infynyty.wokoupdates.insertion.Insertion;
import de.infynyty.wokoupdates.insertion.WGZimmerInsertion;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;

import java.io.IOException;
import java.util.ArrayList;

public class WGZimmerHandler extends InsertionHandler<WGZimmerInsertion> {
    protected WGZimmerHandler(final JDA jda, final Dotenv dotenv) {
        super(jda, dotenv);
    }

    @Override
    protected String pullUpdatedHTML() throws IOException, InterruptedException {
        return null;
    }

    @Override
    protected ArrayList<WGZimmerInsertion> getInsertionsFromHTML(final String html) {
        return null;
    }
}
