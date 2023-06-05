package de.infynyty.zuap.insertion;

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Log
public class MeinWGZimmerInsertion extends Insertion {

    private static final String PROTOCOL = "https://";
    private static final String DOMAIN = "www.meinwgzimmer.ch";


    public MeinWGZimmerInsertion(final @NotNull JSONObject jsonObject) {
        super(jsonObject);
    }

    @Override
    protected @NotNull URI setInsertionURI() throws IllegalStateException {
        try {
            return new URI(PROTOCOL + DOMAIN + "/zimmer/" + super.getJsonObject().get("RoomNr"));
        } catch (NullPointerException | URISyntaxException e) {
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

    @Override
    protected @NotNull Date setMoveInDate() {
        final String date = super.getJsonObject().getJSONObject("ValidFrom").getString("iso");
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(date);
        } catch (ParseException e) {
            log.severe("Date could not be parsed from html!");
            log.severe("Tried parsing: " + date);
            e.printStackTrace();
            return new Date(0);
        }
    }

    @Override
    protected boolean setIsNewTenantWanted() {
        return !(super.getJsonObject().has("ValidUntil")) || super.getJsonObject().isNull("ValidUntil");
    }

    @Override
    protected @Range(from = RENT_UNDEFINED, to = Integer.MAX_VALUE) int setRent() {
        return (int) super.getJsonObject().getDouble("Price");
    }

    @Override
    protected @Nullable Date setPostDate() {
        final String date = super.getJsonObject().getString("createdAt");
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(date);
        } catch (ParseException e) {
            log.severe("Date could not be parsed from html!");
            log.severe("Tried parsing: " + date);
            e.printStackTrace();
            return new Date(0);
        }
    }
}
