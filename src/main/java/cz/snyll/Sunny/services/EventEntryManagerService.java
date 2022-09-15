package cz.snyll.Sunny.services;

import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

@Service
public class EventEntryManagerService implements EventEntryManager {

    EventRepository eventRepository;
    @Autowired
    public EventEntryManagerService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void raiseEvent(String message, EventEntry.EventType eventType) {
        EventEntry eventEntry = new EventEntry();
        eventEntry.setEventType(eventType);
        eventEntry.setEventMessage(message);
        eventEntry.setUpdatedTimestamp(LocalDateTime.now());
        eventRepository.save(eventEntry);
    }
    @Override
    public void raiseEvent(String message, EventEntry.EventType eventType, int everyXminutes) {
        EventEntry withMessage = findLastEventEntryWithMessage(message);
        if (withMessage != null) {
            if (LocalDateTime.now().isAfter(withMessage.getUpdatedTimestamp().plusMinutes(everyXminutes)) == true) {
                raiseEvent(message, eventType);
            } else {
                withMessage.setUpdatedTimestamp(LocalDateTime.now());
                eventRepository.save(withMessage);
            }
        } else {
            raiseEvent(message, eventType);
        }
    }
    @Override
    public Set<EventEntry> getAllEventsOfType(EventEntry.EventType eventType) {
        return eventRepository.findByEventType(eventType);
    }

    @Override
    public ArrayList<EventEntry> getLastEvents(int x) {
        return eventRepository.getLastEvents(x);
    }

    @Override
    public EventEntry findLastEventEntryWithMessage(String message) {
        EventEntry withMessage = eventRepository.getEventEntryWithMessage(message);
        return withMessage;
    }


}
