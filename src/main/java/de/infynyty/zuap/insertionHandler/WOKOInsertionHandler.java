package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.Zuap;
import de.infynyty.zuap.insertion.WOKOInsertion;
import org.jetbrains.annotations.NotNull;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.logging.Level;

public class WOKOInsertionHandler extends InsertionHandler<WOKOInsertion> {


    public WOKOInsertionHandler(@NotNull String logPrefix, @NotNull InsertionAnnouncer announcer, @NotNull HttpClient httpClient) {
        super(logPrefix, announcer, httpClient);
    }

    @Override
    protected String pullUpdatedData() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.woko.ch/de/zimmer-in-zuerich"))
            .GET()
            .build();

        HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 299) {
            throw new HttpStatusException(
                    "Failed to update WOKO"
                    , response.statusCode()
                    , request.uri().toString()
            );
        }

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
                Zuap.log(Level.WARNING, getHandlerName(),"Insertion could not be included because of a missing insertion URL!");
            }
        });
        return insertions;
    }
}
