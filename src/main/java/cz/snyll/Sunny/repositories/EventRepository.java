package cz.snyll.Sunny.repositories;

import cz.snyll.Sunny.domain.EventEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Set;

@Repository
public interface EventRepository extends CrudRepository<EventEntry, Long> {
    Set<EventEntry> findByEventType(EventEntry.EventType eventType);
    @Query(value = "select * from event_entries order by updated_timestamp desc limit ?1", nativeQuery = true)
    ArrayList<EventEntry> getLastEvents(int count);
    @Query(value = "select * from event_entries where event_message = ?1 order by updated_timestamp desc limit 1", nativeQuery = true)
    EventEntry getEventEntryWithMessage(String message);
}
