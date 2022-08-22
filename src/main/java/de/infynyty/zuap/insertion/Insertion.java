package de.infynyty.zuap.insertion;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

/**
 * This class contains all information for an insertion on the  <a href="https://www.woko.ch">WOKO platform</a>.
 * All methods of this class will only work for this exact website and will break, if there are any significant changes
 * to the html file.
 */
@Getter
@Log
public abstract class Insertion {

    protected static final int RENT_UNDEFINED = -1;

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
     *
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
     *
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
