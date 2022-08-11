package de.infynyty.wokoupdates.insertion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lombok.Getter;
import lombok.extern.java.Log;
import org.jsoup.nodes.Element;

/**
 * This class contains all information for an insertion on the  <a href="https://www.woko.ch">WOKO platform</a>.
 * All methods of this class will only work for this exact website and will break, if there are any significant changes
 * to the html file.
 */
@Getter
@Log
public abstract class Insertion {

    protected final Element element;
    private final int insertionNumber;
    private final Date postDate;
    private final Date moveInDate;
    private final boolean isNextTenantWanted;
    private final int rent;


    /**
     * Constructs a new insertion object from a given html string.
     *
     * @param element
     * @param html    The given html file.
     *
     * @throws NumberFormatException If the insertion number cannot be read, an object cannot be constructed
     *                               successfully.
     */
    public Insertion(final Element element) throws NumberFormatException {
        this.element = element;
        this.insertionNumber = setInsertionNumber();
        this.postDate = setDate(element.html(), 0);
        this.moveInDate = setDate(element.html(), 1);
        this.isNextTenantWanted = setIsNewTenantWanted(element.html());
        this.rent = setRent();
    }

    /**
     * Parses the rent from a given html string.
     *
     * @param html The given html.
     * @return The rent or {@value -1}, if it was not possible to parse the rent.
     */
    protected abstract int setRent();

    /**
     * Returns whether a new tenant or a subtenant is wanted.
     *
     * @param html The given html.
     * @return {@code True}, if a new tenant is wanted, otherwise {@code false}.
     */
    protected abstract boolean setIsNewTenantWanted(final String html);

    /**
     * Reads a date from the given html.
     *
     * @param html  The given html.
     * @param index The index of the date that should be returned from the html file.
     * @return A date.
     */
    protected abstract Date setDate(final String html, final int index);

    /**
     * Parses the insertion number of a given insertion.
     *
     * @param html The given html file.
     * @return The insertion number.
     * @throws NumberFormatException If the insertion number cannot be parsed from the given html, an exception is thrown.
     */
    protected abstract int setInsertionNumber() throws NumberFormatException;



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
