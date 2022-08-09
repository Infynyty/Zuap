package de.infynyty.wokoupdates.insertion;

import java.util.ArrayList;
import java.util.Date;

public class WGZimmerInsertion extends Insertion {
    /**
     * Constructs a new insertion object from a given html string.
     *
     * @param html The given html file.
     *
     * @throws NumberFormatException If the insertion number cannot be read, an object cannot be constructed successfully.
     */
    public WGZimmerInsertion(final String html) throws NumberFormatException {
        super(html);
    }

    @Override
    protected int setRent(final String html) {
        return 0;
    }

    @Override
    protected boolean setIsNewTenantWanted(final String html) {
        return false;
    }

    @Override
    protected Date setDate(final String html, final int index) {
        return null;
    }

    @Override
    protected int setInsertionNumber(final String html) throws NumberFormatException {
        return 0;
    }

    @Override
    public ArrayList<Insertion> getAllInsertions(final String html) {
        return null;
    }
}
