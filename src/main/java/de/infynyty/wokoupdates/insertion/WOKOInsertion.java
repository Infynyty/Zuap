package de.infynyty.wokoupdates.insertion;

import lombok.extern.java.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class WOKOInsertion extends Insertion {
    /**
     * Constructs a new insertion object from a given html string.
     *
     * @param html The given html file.
     *
     * @throws NumberFormatException If the insertion number cannot be read, an object cannot be constructed successfully.
     */
    public WOKOInsertion(final String html) throws NumberFormatException {
        super(html);
    }

    //TODO: Use html parser instead of regex

    @Override
    protected int setRent(final String html) {
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

    @Override
    protected boolean setIsNewTenantWanted(final String html) {
        return html.contains("Nachmieter gesucht");
    }

    @Override
    protected Date setDate(final String html, final int index) {
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

    @Override
    protected int setInsertionNumber(final String html) throws NumberFormatException {
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

    @Override
    public ArrayList<Insertion> getAllInsertions(final String html) {
        final String[] splitText = html.split("<div class=\"inserat\">");
        final ArrayList<Insertion> insertions = new ArrayList<>();
        for (int i = 0; i < splitText.length; i++) {
            if (i == 0) continue;
            try {
                insertions.add(new WOKOInsertion(splitText[i]));
            } catch (NumberFormatException e) {
                log.warning("Insertion could not be included because of a missing insertion number!");
            }
        }
        return insertions;
    }
}
