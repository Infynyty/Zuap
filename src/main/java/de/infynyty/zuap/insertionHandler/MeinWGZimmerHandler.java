package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.Zuap;
import de.infynyty.zuap.insertion.MeinWGZimmerInsertion;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.logging.Level;

public class MeinWGZimmerHandler extends InsertionHandler<MeinWGZimmerInsertion> {


    public MeinWGZimmerHandler(@NotNull String logPrefix, @NotNull InsertionAnnouncer announcer, @NotNull HttpClient httpClient) {
        super(logPrefix, announcer, httpClient);
    }

    @Override
    protected String pullUpdatedData() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api1.meinwgzimmer.ch/live/classes/Room"))
            .header("Accept", "*/*")
            .header("Content-Type", "text/plain")
            .POST(HttpRequest.BodyPublishers.ofString("{\"where\":{\"Status\":\"active\",\"Price\":{\"$lte\":2000},"
                + "\"Location\":{\"$nearSphere\":{\"__type\":\"GeoPoint\",\"latitude\":47.37855,\"longitude\":8"
                + ".53703},\"$maxDistance\":0.0015696123057604772}},\"keys\":\"RoomNr,Id,CreatedAt,Location,"
                + "CreatedAtOrg,Price,RoomTitle,Street,Zip,ValidFrom,ValidUntil,City\",\"limit\":999999,"
                + "\"_method\":\"GET\","
                + "\"_ApplicationId\":\"94aa8f52080089940731d6952815ec7233b745cc\","
                + "\"_JavaScriptKey\":\"pjWJhcGN4ObY0pymyCQS\",\"_ClientVersion\":\"js2.1.0\","
                + "\"_InstallationId\":\"3be15981-8be6-7000-c772-93faf54970e4\"}\n"))
            .build();

        HttpResponse<String> response = getHttpClient().send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 299) {
            throw new HttpStatusException(
                    "Failed to update MeinWGZimmer"
                    , response.statusCode()
                    , request.uri().toString()
            );
        }
        if (response.body() == null) throw new IllegalStateException("Data received from MeinWGZimmer is null");
        return response.body();
    }

    @Override
    protected ArrayList<MeinWGZimmerInsertion> getInsertionsFromData(@NotNull final String data) throws IllegalStateException {
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(data);
        } catch (JSONException ex) {
            throw new IllegalStateException("Unable to parse MeinWGZimmer data as JSON: " + ex.getMessage());
        }
        if (!jsonObject.has("results")) throw new IllegalStateException("MeinWGZimmer data does not have the required 'results' attribute.");
        final JSONArray rooms = jsonObject.getJSONArray("results");
        final ArrayList<MeinWGZimmerInsertion> insertions = new ArrayList<>();
        for (int i = 0; i < rooms.length(); i++) {
            try {
                insertions.add(new MeinWGZimmerInsertion(rooms.getJSONObject(i)));
            } catch (IllegalStateException e) {
                Zuap.log(Level.WARNING, getHandlerName(), "Insertion could not be included because of a missing insertion URL!");
            }
        }
        return insertions;
    }
}
