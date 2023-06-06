package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.Zuap;
import de.infynyty.zuap.insertion.FlatfoxInsertion;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.logging.Level;

public class FlatfoxHandler extends InsertionHandler<FlatfoxInsertion> {
    public FlatfoxHandler(@NotNull JDA jda, @NotNull String handlerName, @NotNull InsertionAnnouncer announcer) {
        super(jda, handlerName, announcer);
    }

    @Override
    protected String pullUpdatedData() throws IOException, InterruptedException {
        final String data = getPins();
        return getInsertionsFromPins(data);
    }

    @Override
    protected ArrayList<FlatfoxInsertion> getInsertionsFromData(String data) throws IllegalStateException {
        final JSONObject jsonObject = new JSONObject(data);
        final JSONArray insertionsJSON = new JSONArray(jsonObject.getJSONArray("results"));
        final ArrayList<FlatfoxInsertion> insertions = new ArrayList<>();
        insertionsJSON.forEach(insertion -> insertions.add(new FlatfoxInsertion((JSONObject) insertion)));
        return insertions;
    }

    private String getPins() throws IOException, InterruptedException {
        final HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://flatfox.ch/api/v1/pin/?east=8.701075&max_count=30&north=47.434662&object_category=APARTMENT&object_category=SHARED&offer_type=RENT&ordering=-insertion&south=47.320258&west=8.372241"))
                .build();
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() > 299) {
            Zuap.log(Level.WARNING, "Could not pull Pin data from Flatfox. Got the following response: ");
            Zuap.log(Level.WARNING, response.body());
            throw new HttpStatusException("Could not pull Pin data from Flatfox", response.statusCode(), request.uri().toString());
        }
        return response.body();
    }

    private String getInsertionsFromPins(@NotNull final String data) throws IOException, InterruptedException {
        final JSONArray pins = new JSONArray(data);
        final HttpClient client = HttpClient.newHttpClient();

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("https://flatfox.ch/api/v1/public-listing/?");
        pins.forEach(pin -> stringBuilder.append("pk=").append(((JSONObject) pin).get("pk")).append("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stringBuilder.toString()))
                .build();
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() > 299) {
            Zuap.log(Level.WARNING, "Could not pull insertion data from Flatfox. Got the following response: ");
            Zuap.log(Level.WARNING, response.body());
            throw new HttpStatusException("Could not pull insertion data from Flatfox", response.statusCode(), request.uri().toString());
        }

        return response.body();
    }
}
