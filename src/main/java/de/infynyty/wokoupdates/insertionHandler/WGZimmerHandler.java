package de.infynyty.wokoupdates.insertionHandler;

import de.infynyty.wokoupdates.insertion.Insertion;
import de.infynyty.wokoupdates.insertion.WGZimmerInsertion;
import de.infynyty.wokoupdates.insertion.WOKOInsertion;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

@Log
public class WGZimmerHandler extends InsertionHandler<WGZimmerInsertion> {
    public WGZimmerHandler(final JDA jda, final Dotenv dotenv) {
        super(jda, dotenv);
    }

    //TODO: Make it possible to change search variables
    @Override
    protected String pullUpdatedHTML() throws IOException, InterruptedException {
        final HttpClient wgZimmerClient = HttpClient.newHttpClient();
        HttpRequest wgZimmerRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://www.wgzimmer.ch/wgzimmer/search/mate.html?"))
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("query=&priceMin=200&priceMax=650&state=all&permanent=all&student=true&typeofwg=all&orderBy=%40sortDate&orderDir=descending&startSearchMate=true&wgStartSearch=true&start=0"))
            .build();

        HttpResponse<byte[]> wgZimmerResponse = wgZimmerClient.send(wgZimmerRequest,
            HttpResponse.BodyHandlers.ofByteArray());


        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(wgZimmerResponse.body()));
        return new String(gzipInputStream.readAllBytes());
    }

    @Override
    protected ArrayList<WGZimmerInsertion> getInsertionsFromHTML(final String html) {
        final Document document = Jsoup.parse(html);
        final Elements elements = document.getElementsByClass("search-result-entry search-mate-entry");
        final ArrayList<WGZimmerInsertion> insertions = new ArrayList<>();
        elements.forEach(element -> {
            try {
                insertions.add(new WGZimmerInsertion(element));
            } catch (NumberFormatException e) {
                log.warning("Insertion could not be included because of a missing insertion number!");
            }
        });
        return insertions;
    }
}
