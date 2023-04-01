package de.infynyty.zuap.insertion;

import lombok.Getter;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class contains all information for an insertion on the  <a href="https://www.woko.ch">WOKO platform</a>.
 * All methods of this class will only work for this exact website and will break, if there are any significant changes
 * to the html file.
 */
@Getter
@Log
public abstract class Insertion {

    protected static final int RENT_UNDEFINED = -1;
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @Nullable
    private Element element;

    @Nullable
    private JSONObject jsonObject;

    @NotNull
    protected final URL insertionURI;
    @NotNull
    private final Date moveInDate;
    @Range(from = RENT_UNDEFINED, to = Integer.MAX_VALUE)
    private final int rent;
    private final boolean isNextTenantWanted;

    @Nullable
    private final Date postDate;


    /**
     * Constructs a new insertion object from a given html string. This constructor should be used when there is no
     * possibility of obtaining a JSON file containing all needed data.
     *
     * @param element The given html file.
     * @throws IllegalStateException If the insertion URL cannot be read, an object cannot be constructed
     *                               successfully.
     */
    public Insertion(@NotNull final Element element) throws IllegalStateException {
        this.element = element;
        this.insertionURI = setInsertionURL();
        this.moveInDate = setMoveInDate();

        this.isNextTenantWanted = setIsNewTenantWanted();
        this.rent = setRent();

        this.postDate = setPostDate();
    }

    /**
     * Constructs a new insertion object from a given html string. This constructor should be used when it is
     * possible to get the needed data from a JSON file.
     *
     * @param jsonObject The given JSON data.
     * @throws NumberFormatException If the insertion URL cannot be read, an object cannot be constructed
     *                               successfully.
     */
    public Insertion(@NotNull final JSONObject jsonObject) throws IllegalStateException {
        this.jsonObject = jsonObject;
        this.insertionURI = setInsertionURL();
        this.moveInDate = setMoveInDate();

        this.isNextTenantWanted = setIsNewTenantWanted();
        this.rent = setRent();

        this.postDate = setPostDate();
    }

    @NotNull
    protected abstract URL setInsertionURL() throws IllegalStateException;

    /**
     * Parses the rent from a given html string.
     *
     * @return The rent or {@value -1}, if it was not possible to parse the rent.
     */
    @Range(from = RENT_UNDEFINED, to = Integer.MAX_VALUE)
    protected int setRent() {
        return RENT_UNDEFINED;
    }

    /**
     * Returns whether a new tenant or a subtenant is wanted.
     *
     * @return {@code True}, if a new tenant is wanted, otherwise {@code false}.
     */
    protected boolean setIsNewTenantWanted() {
        return false;
    }

    /**
     * Reads a date from the given html.
     *
     * @return A date.
     */
    @NotNull
    protected abstract Date setMoveInDate();

    /**
     * Returns the date that this insertion was posted online. Should be overridden by subclasses.
     *
     * @return A date or {@code null}, if the date cannot be parsed for a certain website.
     */
    @Nullable
    protected Date setPostDate() {
        return null;
    }

    public Message toEmbed() {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonlink.io/api/extract?url=" + insertionURI))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        final JSONObject jsonObject = new JSONObject(response.body());
        String insertionDescription = null;
        if (jsonObject.has("description")) {
            insertionDescription = jsonObject.getString("description");
            if (insertionDescription.length() > MAX_DESCRIPTION_LENGTH) {
                insertionDescription = insertionDescription.substring(0, MAX_DESCRIPTION_LENGTH);
                insertionDescription = insertionDescription + "...";
            }
        }

        String imageLink = null;
        if (jsonObject.has("images")) {
            final JSONArray images = jsonObject.getJSONArray("images");
            if (!images.isEmpty()) {
                imageLink = images.getString(0);
            }
        }


        MessageBuilder messageBuilder = new MessageBuilder();
        final Button linkButton = Button.link(String.valueOf(insertionURI), "Insertion Link");
        final Button reportButton = Button.link("https://github.com/Infynyty/Zuap/issues", "Report Issues");
        final ActionRow actionRow = ActionRow.of(linkButton, reportButton);

        messageBuilder.setActionRows(actionRow);

        final EmbedBuilder builder = new EmbedBuilder();

        if (rent != RENT_UNDEFINED) {
            builder.addField(new MessageEmbed.Field("Rent", rent + " CHF", false));
        }

        builder.addField(new MessageEmbed.Field("Next Tenant Wanted", isNextTenantWanted ? "Yes" : "No", false))
                .addField("Move-in Date", new SimpleDateFormat("dd.MM.yyyy").format(moveInDate), false);

        if (postDate != null) {
            builder.addField(new MessageEmbed.Field("Date Of Insertion Posting", new SimpleDateFormat("dd.MM.yyyy").format(postDate), false));
        }

        if (imageLink != null && !imageLink.isBlank()) {
            builder.setImage(imageLink);
        }
        if (insertionDescription != null && !insertionDescription.isBlank()) {
            builder.addField("Description", insertionDescription, false);
        }
        builder.setTitle("New Insertion On " + insertionURI.getHost(), insertionURI.toString()).setColor(Color.YELLOW);
        messageBuilder.setEmbeds(builder.build());

        return messageBuilder.build();
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Insertion: \n")
                .append("Link: ").append(insertionURI).append("\n");

        if (rent != RENT_UNDEFINED) {
            stringBuilder.append("Rent: CHF ").append(rent).append(", \n");
        } else {
            stringBuilder.append("Rent could not be parsed.\n");
        }

        stringBuilder
                .append("Next tenant wanted: ").append(isNextTenantWanted).append(", \n")
                .append("Move-in date: ").append(new SimpleDateFormat("dd.MM.yyyy").format(moveInDate)).append("\n");

        if (postDate != null) {
            stringBuilder.append("Date of insertion posting: ").append(new SimpleDateFormat("dd.MM.yyyy").format(postDate)).append("\n");
        }

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Insertion)) return false;
        return this.insertionURI.equals(((Insertion) o).getInsertionURI());
    }
}
