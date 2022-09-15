package cz.snyll.Sunny.services;

import cz.snyll.Sunny.domain.EventEntry;

import java.util.ArrayList;
import java.util.Set;

public interface EventEntryManager {
    void raiseEvent(String message, EventEntry.EventType eventType, int everyXminutes);
    void raiseEvent(String message, EventEntry.EventType eventType);
    Set<EventEntry> getAllEventsOfType(EventEntry.EventType eventType);
    ArrayList<EventEntry> getLastEvents(int x);
    EventEntry findLastEventEntryWithMessage(String message);
}
