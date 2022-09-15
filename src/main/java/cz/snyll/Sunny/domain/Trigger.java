package cz.snyll.Sunny.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Entity
@Setter
@Getter
@Table(name = "triggers")
public class Trigger {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToMany(mappedBy = "triggers")
    private Set<DeviceSetting> deviceSettings;
    @Size(min = 3, message = "Must have at least 3 characters.")
    private String triggerName;
    @NotBlank(message = "Cannot be empty.")
    private String infoDataKey;                     // name of the data key we are comparing
    private float infoDataValue;                   // value that is being compared to the collected data
    @Enumerated(EnumType.STRING)
    private TriggerOperation triggerOperation;      // operation to do between the collected data and trigger data value
    private int triggerDelay;                       // when trigger becomes relevant wait for specified amount of seconds
    private boolean triggerState;                   // shows if trigger conditions are met
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "status_updated_time")
    private Date statusUpdatedTime;

    public enum TriggerOperation {
        LESSTHAN, GREATERTHAN, EQUALS, LESSOREQUAL, GREATEROREQUAL }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (! (obj instanceof Trigger)) {
            return false;
        }
        return this.id == ((Trigger)obj).getId();
    }

    public boolean shouldBeOn() {
        Date minusDelay = new Date(System.currentTimeMillis() - (triggerDelay * 1000));
        if (triggerState == true && minusDelay.after(statusUpdatedTime))
            return true;
        return false;
    }
}