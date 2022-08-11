package de.infynyty.wokoupdates.insertion;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Date;

public class WGZimmerInsertion extends Insertion {
    /**
     * Constructs a new insertion object from a given html string.
     *
     * @param element The given html file.
     *
     * @throws NumberFormatException If the insertion number cannot be read, an object cannot be constructed successfully.
     */
    public WGZimmerInsertion(final Element element) throws NumberFormatException {
        super(element);
    }

    @Override
    protected int setRent() {
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
    protected int setInsertionNumber() throws NumberFormatException {
        return 0;
    }
}
