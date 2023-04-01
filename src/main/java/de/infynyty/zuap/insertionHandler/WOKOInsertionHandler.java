package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.insertion.WOKOInsertion;
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
        final String logPrefix
    ) {
        super(jda, logPrefix);
    }

    @Override
    protected String pullUpdatedData() throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.woko.ch/de/zimmer-in-zuerich"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    @Override
    protected ArrayList<WOKOInsertion> getInsertionsFromData(final String data) {
        final Document document = Jsoup.parse(data);
        final Elements elements = document.getElementsByClass("inserat");
        final ArrayList<WOKOInsertion> insertions = new ArrayList<>();
        elements.forEach(element -> {
            try {
                insertions.add(new WOKOInsertion(element));
            } catch (IllegalStateException e) {
            log.warning("Insertion could not be included because of a missing insertion URL!");
            }
        });
        return insertions;
    }
}
