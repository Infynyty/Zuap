package de.infynyty.zuap.insertion;

import de.infynyty.zuap.Zuap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

public class FlatfoxInsertion extends Insertion {

    private static final String PROTOCOL = "https://";
    private static final String DOMAIN = "flatfox.ch";

    public FlatfoxInsertion(@NotNull JSONObject jsonObject) throws IllegalStateException {
        super(jsonObject);
    }

    @Override
    protected @NotNull URI setInsertionURI() throws IllegalStateException {
        try {
            return new URI(PROTOCOL + DOMAIN + super.getJsonObject().getString("url"));
        } catch (NullPointerException | URISyntaxException e) {
            throw new IllegalStateException("URI to insertion could not be parsed.\n" + e.getMessage());
        }
    }

    @Override
    protected SortedMap<String, Optional<String>> setProperties() {
        SortedMap<String, Optional<String>> map = new TreeMap<>();
        map.put("Rent", setRent() == RENT_UNDEFINED ? Optional.empty() : Optional.of("CHF " + setRent()));
        map.put("Move-in Date", setMoveInDate() == null ? Optional.empty() : Optional.of(new SimpleDateFormat("dd.MM.yyyy").format(setMoveInDate())));
        map.put("Next Tenant Wanted", Optional.of(setIsNewTenantWanted() ? "Yes" : "No"));
        map.put("Address", setAddress().isBlank() ? Optional.empty() : Optional.of(setAddress()));
        map.put("Living space", setLivingSpace() == RENT_UNDEFINED ? Optional.empty() : Optional.of(setLivingSpace() + "m²"));
        return map;
    }

    @Override
    protected @Nullable Date setMoveInDate() {
        if (!super.getJsonObject().has("moving_date") || super.getJsonObject().isNull("moving_date")) {
            return null;
        }
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(super.getJsonObject().getString("moving_date"));
        } catch (ParseException e) {
            Zuap.log(Level.WARNING, "Cannot parse the following moving date for Flatfox insertion: " + super.getJsonObject().getString("moving_date"));
        }
        return new Date(0);
    }

    @Override
    protected boolean setIsNewTenantWanted() {
        return !super.getJsonObject().getBoolean("is_temporary");
    }

    @Override
    protected @Nullable Date setPostDate() {
        final OffsetDateTime offsetDateTime = OffsetDateTime.parse(super.getJsonObject().getString("created"));
        final Instant instant = offsetDateTime.toInstant();
        return Date.from(instant);
    }

    @Override
    protected @Range(from = RENT_UNDEFINED, to = Integer.MAX_VALUE) int setRent() {
        if (!super.getJsonObject().has("rent_gross") || super.getJsonObject().isNull("rent_gross")) {
            return RENT_UNDEFINED;
        }
        return super.getJsonObject().getInt("rent_gross");
    }

    private String setAddress() {
        return super.getJsonObject().getString("public_address");
    }

    private int setLivingSpace() {
        if (!super.getJsonObject().has("surface_living") || super.getJsonObject().isNull("surface_living")) {
            return RENT_UNDEFINED;
        }
        return super.getJsonObject().getInt("surface_living");
    }
}
