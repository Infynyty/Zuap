package de.infynyty.wokoupdates;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.extern.java.Log;

/**
 * This class contains all information for an insertion on the  <a href="https://www.woko.ch">WOKO platform</a>.
 * All methods of this class will only work for this exact website and will break, if there are any significant changes
 * to the html file.
 */
@Getter
@Log
public class Insertion {
    private final int insertionNumber;
    private final Date postDate;
    private final Date moveInDate;
    private final boolean isNextTenantWanted;
    private final int rent;

    /**
     * Constructs a new insertion object from a given html string.
     *
     * @param html The given html file.
     * @throws NumberFormatException If the insertion number cannot be read, an object cannot be constructed successfully.
     */
    private Insertion(final String html) throws NumberFormatException {
        this.insertionNumber = setInsertionNumber(html);
        this.postDate = setDate(html, 0);
        this.moveInDate = setDate(html, 1);
        this.isNextTenantWanted = setIsNewTenantWanted(html);
        this.rent = setRent(html);
    }

    /**
     * Parses the rent from a given html string.
     *
     * @param html The given html.
     * @return The rent or {@value -1}, if it was not possible to parse the rent.
     */
    private int setRent(final String html) {
        final String substringStart = "<div class=\"preis\">";
        final int indexStart = html.indexOf(substringStart) + substringStart.length();
        final String substringEnd = ".--</div>";
        final int indexEnd = html.indexOf(substringEnd);
        final String number = html.substring(indexStart, indexEnd);
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            log.warning("Rent could not be parsed from html!");
            log.warning("Tried parsing: " + number);
            return -1;
        }
    }

    /**
     * Returns whether a new tenant or a subtenant is wanted.
     *
     * @param html The given html.
     * @return {@code True}, if a new tenant is wanted, otherwise {@code false}.
     */
    private boolean setIsNewTenantWanted(final String html) {
        return html.contains("Nachmieter gesucht");
    }

    /**
     * Reads a date from the given html.
     *
     * @param html  The given html.
     * @param index The index of the date that should be returned from the html file.
     * @return A date.
     */
    private Date setDate(final String html, final int index) {
        final Pattern pattern = Pattern.compile("(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})");
        final Matcher matcher = pattern.matcher(html);
        final String stringDate = matcher.results().toList().get(index).group();
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(stringDate);
        } catch (ParseException e) {
            log.severe("Date could not be parsed from html!");
            log.severe("Tried parsing: " + stringDate);
            e.printStackTrace();
            return new Date(0);
        }
    }

    /**
     * Parses the insertion number of a given insertion.
     *
     * @param html The given html file.
     * @return The insertion number.
     * @throws NumberFormatException If the insertion number cannot be parsed from the given html, an exception is thrown.
     */
    private int setInsertionNumber(final String html) throws NumberFormatException {
        final String substring = "/de/zimmer-in-zuerich-details/";
        final int index = html.indexOf(substring) + substring.length();
        final String number = html.substring(index, index + 4);
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            log.severe("Insertion number could not be parsed from html");
            log.severe("Tried parsing: " + number);
            throw new NumberFormatException();
        }
    }

    /**
     * Creates objects for all Insertions found in an html-String.
     *
     * @param html The given html
     * @return An array of all Insertions found.
     */
    public static ArrayList<Insertion> getAllInsertions(final String html) {
        final String[] splitText = html.split("<div class=\"inserat\">");
        final ArrayList<Insertion> insertions = new ArrayList<>();
        for (int i = 0; i < splitText.length; i++) {
            if (i == 0) continue;
            try {
                insertions.add(new Insertion(splitText[i]));
            } catch (NumberFormatException e) {
                log.warning("Insertion could not be included because of a missing insertion number!");
            }
        }
        return insertions;
    }

    @Override
    public String toString() {
        return "Insertion: \n" +
            "Insertion number: " + insertionNumber + ", \n" +
            "Next tenant wanted: " + isNextTenantWanted + ", \n" +
            "Rent: CHF " + rent + ", \n" +
            "Date of insertion posting: " + new SimpleDateFormat("dd.MM.yyyy").format(postDate) + ", \n" +
            "Move-in date: " + new SimpleDateFormat("dd.MM.yyyy").format(moveInDate) + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Insertion)) return false;
        return this.insertionNumber == ((Insertion) o).insertionNumber;
    }
}
