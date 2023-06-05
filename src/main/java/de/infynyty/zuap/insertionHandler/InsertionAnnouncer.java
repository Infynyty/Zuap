package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.insertion.Insertion;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to announce a new insertion in any desired way to an arbitrary platform.
 */
public interface InsertionAnnouncer {

    /**
     * Announces an insertion.
     * @param insertion The insertion to announce, which must not be null.
     */
    void announce(@NotNull final Insertion insertion);
}
