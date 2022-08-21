package de.infynyty.wokoupdates.insertionHandler;

import de.infynyty.wokoupdates.insertion.MeinWGZimmerInsertion;
import de.infynyty.wokoupdates.insertion.WOKOInsertion;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

@Log
public class MeinWGZimmerHandler extends InsertionHandler<MeinWGZimmerInsertion> {
    /**
     * Creates an insertion handler for a new website.
     *
     * @param jda       A reference to the discord bot.
     * @param dotenv    The file containing environment variables.
     * @param logPrefix
     */
    public MeinWGZimmerHandler(
        final @NotNull JDA jda,
        final @NotNull Dotenv dotenv,
        final @NotNull String logPrefix
    ) {
        super(jda, dotenv, logPrefix);
    }

    @Override
    protected String pullUpdatedData() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
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

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        return response.body();
    }

    @Override
    protected ArrayList<MeinWGZimmerInsertion> getInsertionsFromData(final String data) throws IllegalStateException {
        final JSONObject jsonObject = new JSONObject(data);
        final JSONArray rooms = jsonObject.getJSONArray("results");
        final ArrayList<MeinWGZimmerInsertion> insertions = new ArrayList<>();
        for (int i = 0; i < rooms.length(); i++) {
            try {
                insertions.add(new MeinWGZimmerInsertion(rooms.getJSONObject(i)));
            } catch (IllegalStateException e) {
                log.warning("Insertion could not be included because of a missing insertion URL!");
            }
        }
        return insertions;
    }
}
