package de.infynyty.zuap.insertion;

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class WOKOInsertion extends Insertion {

    /**
     * This symbol is used on the WOKO-website to denote that the rent is rounded and that there are no centimes.
     */
    private static final String PRICE_SUFFIX = ".--";

    private static final String PROTOCOL = "https://";
    private static final String DOMAIN = "www.woko.ch";

    /**
     * Constructs a new insertion object from a given html string.
     *
     * @param element The given html file.
     *
     * @throws NumberFormatException If the insertion number cannot be read, an object cannot be constructed successfully.
     */
    public WOKOInsertion(final Element element) throws IllegalStateException {
        super(element);
    }

    @Override
    protected @NotNull URI setInsertionURI() {
        try {
            return new URI(PROTOCOL + DOMAIN + super.getElement().getElementsByTag("a").get(0).attr("href"));
        } catch (IndexOutOfBoundsException | URISyntaxException e) {
            throw new IllegalStateException("URI to insertion could not be parsed.\n\n" + e.getMessage());
        }
    }

    @Override
    protected SortedMap<String, Optional<String>> setProperties() {
        SortedMap<String, Optional<String>> map = new TreeMap<>();
        map.put("Rent", Optional.of(String.valueOf(setRent())));
        map.put("Move-in Date", Optional.of(new SimpleDateFormat("dd.MM.yyyy").format(setMoveInDate())));
        map.put("Next Tenant Wanted", Optional.of(setIsNewTenantWanted() ? "Yes" : "No"));
        return map;
    }

    //TODO: Use html parser instead of regex

    @Override
    protected int setRent() {
        final Elements priceElements = super.getElement().getElementsByClass("preis");
        String price = priceElements.html();
        try {
            price = price.substring(0, price.length() - PRICE_SUFFIX.length());
        } catch (IndexOutOfBoundsException e) {
            log.severe("Rent could not be parsed because of an incorrectly formatted string");
            log.severe("Rent string: \n\n" + price + "\n\n");
            log.severe("HTML: \n\n" + super.getElement());
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
    protected boolean setIsNewTenantWanted() {
        return super.getElement().html().contains("Nachmieter gesucht");
    }


    @Override
    protected @Nullable Date setMoveInDate() {
        final Pattern pattern = Pattern.compile("(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})");
        final Matcher matcher = pattern.matcher(super.getElement().html());
        final String stringDate = matcher.results().toList().get(1).group();
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
    protected @Nullable Date setPostDate() {
        final Pattern pattern = Pattern.compile("(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})");
        final Matcher matcher = pattern.matcher(super.getElement().html());
        final String stringDate = matcher.results().toList().get(0).group();
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(stringDate);
        } catch (ParseException e) {
            log.severe("Date could not be parsed from html!");
            log.severe("Tried parsing: " + stringDate);
            e.printStackTrace();
            return new Date(0);
        }
    }
}
