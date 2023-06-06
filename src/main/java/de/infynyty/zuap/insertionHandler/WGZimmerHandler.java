package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.Zuap;
import de.infynyty.zuap.insertion.WGZimmerInsertion;
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

public class WGZimmerHandler extends InsertionHandler<WGZimmerInsertion> {


    public WGZimmerHandler(@NotNull String logPrefix, @NotNull InsertionAnnouncer announcer, @NotNull HttpClient httpClient) {
        super(logPrefix, announcer, httpClient);
    }

    //TODO: Make it possible to change search variables
    @Override
    protected String pullUpdatedData() throws IOException, InterruptedException {
        HttpRequest wgZimmerRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://www.wgzimmer.ch/wgzimmer/search/mate.html?"))
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("query=&priceMin=200&priceMax=650&state=all&permanent"
                + "=all&student=true&typeofwg=all&orderBy=%40sortDate&orderDir=descending&startSearchMate=true&wgStartSearch=true&start=0"))
            .build();

        HttpResponse<String> wgZimmerResponse = getHttpClient().send(wgZimmerRequest,
            HttpResponse.BodyHandlers.ofString());

        if (wgZimmerResponse.statusCode() >= 299) {
            throw new HttpStatusException(
                    "Failed to update WGZimmer"
                    , wgZimmerResponse.statusCode()
                    , wgZimmerRequest.uri().toString()
                    );
        }
        return wgZimmerResponse.body();
    }

    @Override
    protected ArrayList<WGZimmerInsertion> getInsertionsFromData(final String data) {
        final Document document = Jsoup.parse(data);
        final Elements elements = document.getElementsByClass("search-result-entry search-mate-entry");
        final ArrayList<WGZimmerInsertion> insertions = new ArrayList<>();
        elements.forEach(element -> {
            try {
                insertions.add(new WGZimmerInsertion(element));
            } catch (NumberFormatException e) {
                Zuap.log(Level.WARNING,getHandlerName(),"Insertion could not be included because of a missing insertion number!");
            }
        });
        return insertions;
    }
}
