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

@Log
public class WGZimmerInsertion extends Insertion {

    private static final String PROTOCOL = "https://";
    private static final String DOMAIN = "www.wgzimmer.ch";

    public WGZimmerInsertion(final @NotNull Element element) throws IllegalStateException {
        super(element);
    }


    @Override
    protected @NotNull URI setInsertionURI() throws IllegalStateException {
        try {
            return new URI(PROTOCOL + DOMAIN + super.getElement().getElementsByTag("a").get(0).attr("href"));
        } catch (IndexOutOfBoundsException | URISyntaxException e) {
            throw new IllegalStateException("URI to insertion could not be parsed.\n\n" + e.getMessage());
        }
    }

    @Override
    protected SortedMap<String, Optional<String>> setProperties() {
        SortedMap<String, Optional<String>> map = new TreeMap<>();
        map.put("Region", Optional.of(setRegion()));
        map.put("Rent", Optional.of(String.valueOf(setRent())));
        map.put("Move-in Date", Optional.of(new SimpleDateFormat("dd.MM.yyyy").format(setMoveInDate())));
        map.put("Next Tenant Wanted", Optional.of(setIsNewTenantWanted() ? "Yes" : "No"));
        return map;
    }

    private String setRegion() {
        Element strongElement = super.getElement().select("span.thumbState strong").first();
        String strongText = strongElement.text();
        String textUntilBr = strongElement.parent().ownText().trim();
        return strongText + ", " + textUntilBr;
    }

    @Override
    protected int setRent() {
        final Elements priceElements = super.getElement().getElementsByClass("cost");
        String price = priceElements.text();
        try {
            return (int) Float.parseFloat(price);
        } catch (NumberFormatException e) {
            log.severe("Rent could not be parsed because of an incorrectly formatted string");
            log.warning("Tried parsing: " + price);
            return -1;
        }
    }

    @Override
    protected boolean setIsNewTenantWanted() {
        return super.getElement().html().contains("Bis: Unbefristet");
    }

    @Override
    protected @Nullable Date setMoveInDate() {
        final String moveInDate = super.getElement()
            .getElementsByClass("from-date")
            .get(0)
            .getElementsByTag("strong")
            .text();
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(moveInDate);
        } catch (ParseException e) {
            log.severe("Date could not be parsed from html!");
            log.severe("Tried parsing: " + moveInDate);
            e.printStackTrace();
            return new Date(0);
        }
    }
}
