package de.infynyty.zuap.insertion;

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log
public class WGZimmerInsertion extends Insertion {

    private static final String PRICE_PREFIX = "SFr. ";
    private static final String PROTOCOL = "https://";
    private static final String DOMAIN = "www.wgzimmer.ch";

    public WGZimmerInsertion(final @NotNull Element element) throws IllegalStateException {
        super(element);
    }


    @Override
    protected @NotNull URL setInsertionURL() throws IllegalStateException {
        try {
            return new URL(PROTOCOL + DOMAIN + super.getElement().getElementsByTag("a").get(1).attr("href"));
        } catch (IndexOutOfBoundsException | MalformedURLException e) {
            throw new IllegalStateException("URI to insertion could not be parsed.\n\n" + e.getMessage());
        }
    }

    @Override
    protected int setRent() {
        final Elements priceElements = super.getElement().getElementsByClass("cost");
        String price = priceElements.text();
        try {
            price = price.substring(PRICE_PREFIX.length());
        } catch (IndexOutOfBoundsException e) {
            log.severe("Rent could not be parsed because of an incorrectly formatted string");
            log.severe("Rent string: \n\n" + price + "\n\n");
            log.severe("HTML: \n\n" + super.getElement());
            return -1;
        }
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
    protected @NotNull Date setMoveInDate() {
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
