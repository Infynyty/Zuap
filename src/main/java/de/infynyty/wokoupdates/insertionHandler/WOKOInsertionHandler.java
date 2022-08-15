package de.infynyty.wokoupdates.insertionHandler;

import de.infynyty.wokoupdates.insertion.WOKOInsertion;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

@Log
public class WOKOInsertionHandler extends InsertionHandler<WOKOInsertion> {

    public WOKOInsertionHandler(
        final JDA jda,
        final Dotenv dotenv,
        final String logPrefix
    ) {
        super(jda, dotenv, logPrefix);
    }

    @Override
    protected String pullUpdatedHTML() throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.woko.ch/de/zimmer-in-zuerich"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    @Override
    protected ArrayList<WOKOInsertion> getInsertionsFromHTML(final String html) {
        final Document document = Jsoup.parse(html);
        final Elements elements = document.getElementsByClass("inserat");
        final ArrayList<WOKOInsertion> insertions = new ArrayList<>();
        elements.forEach(element -> {
            try {
                insertions.add(new WOKOInsertion(element));
            } catch (NumberFormatException e) {
            log.warning("Insertion could not be included because of a missing insertion number!");
            }
        });
        return insertions;
    }
}
