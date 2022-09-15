package cz.snyll.Sunny.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "event_entries")
public class EventEntry {
    @Id
    @GeneratedValue
    private long Id;
    @Column(columnDefinition = "TEXT")
    private String EventMessage;
    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    @Column(name = "updated_timestamp")
    private LocalDateTime updatedTimestamp;
    public enum EventType {
        INFO, WARNING, ERROR, SUCCESS
    }
}
