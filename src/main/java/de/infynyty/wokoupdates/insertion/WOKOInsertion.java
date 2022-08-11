package de.infynyty.wokoupdates.insertion;

import lombok.extern.java.Log;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class WOKOInsertion extends Insertion {

    /**
     * This symbol is used on the WOKO-website to denote that the rent is rounded and that there are no centimes.
     */
    private static final String ROUNDED_RENT = ".--";

    /**
     * Constructs a new insertion object from a given html string.
     *
     * @param element The given html file.
     *
     * @throws NumberFormatException If the insertion number cannot be read, an object cannot be constructed successfully.
     */
    public WOKOInsertion(final Element element) throws NumberFormatException {
        super(element);
    }

    //TODO: Use html parser instead of regex

    @Override
    protected int setRent() {
        final Elements priceElements = super.element.getElementsByClass("preis");
        String price = priceElements.html();
        try {
            price = price.substring(0, price.length() - ROUNDED_RENT.length());
        } catch (IndexOutOfBoundsException e) {
            log.severe("Rent could not be parsed because of an incorrectly formatted string");
            log.severe("Rent string: \n\n" + price + "\n\n");
            log.severe("HTML: \n\n" + super.element);
            return -1;
        }
        try {
            return Integer.parseInt(price);
        } catch (NumberFormatException e) {
            log.severe("Rent could not be parsed because of an incorrectly formatted string");
            log.warning("Tried parsing: " + price);
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
    protected int setInsertionNumber() throws NumberFormatException {
        final Elements linkElements = super.element.getElementsByTag("a");
        final String link = linkElements.attr("href");

        final Pattern pattern = Pattern.compile("\\d+");
        final Matcher matcher = pattern.matcher(link);
        matcher.find();
        final String number = matcher.group();

        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            log.severe("Insertion number could not be parsed from html");
            log.severe("Tried parsing: " + number);
            throw new NumberFormatException();
        }
    }
}
