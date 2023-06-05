package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.insertion.Insertion;
import org.jetbrains.annotations.NotNull;

public interface InsertionAnnouncer {
    void announce(@NotNull final Insertion insertion);
}
