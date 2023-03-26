package cz.snyll.Sunny.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Table(name = "devicesettings")
@Entity
public class DeviceSetting {
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    @Getter
    @ManyToOne
    @JoinColumn(name="device_id", nullable=false)
    private Device device;
    @Setter
    @Getter
    @Size(min = 3, message = "This field cannot be empty.")
    private String deviceSettingName;
    @Setter
    @Getter
    private int deviceSettingPriority;             // priority (higher number is higher prio)

    public void setTimeWindowFrom(String timeWindowFrom) {
        DateTimeFormatter formatter
                = DateTimeFormatter.ofPattern("HH:mm");
        this.timeWindowFrom = LocalTime.parse(timeWindowFrom, formatter);
    }

    @Getter
    @Setter
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime timeWindowFrom;             // from what time to run in 24 hour format (eg. 16)

    public void setTimeWindowTo(String timeWindowTo) {
        DateTimeFormatter formatter
                = DateTimeFormatter.ofPattern("HH:mm");
        this.timeWindowTo = LocalTime.parse(timeWindowTo, formatter);
    }

    @Setter
    @Getter
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime timeWindowTo;               // to what time to run
    @Setter
    @Getter
    private boolean isActive;

    public void setIsActive(String value) {
        if (value.equals("true")) {
            this.isActive = true;
        } else {
            this.isActive = false;
        }
    }

    @Getter
    @Setter
    public boolean activeInAwayMode;

    @Setter
    @Getter
    private int triggerEveryNthDay;                 // make this DeviceSetting true ever nth day (you can trigger id lets say every 3 weeks - set to 21)

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "devicesetting_trigger",
            joinColumns = @JoinColumn(name = "devicesetting_id"),
            inverseJoinColumns = @JoinColumn(name = "trigger_id"))
    @Getter
    private Set<Trigger> triggers;

    public void setTriggers(String[] triggers) {
        System.out.println("Saving triggers: " + triggers);
        this.triggers = new HashSet<Trigger>();
        for (String trigger : triggers) {
            Trigger newTrigger = new Trigger();
            newTrigger.setId(Long.parseLong(trigger));

            this.triggers.add(newTrigger);
        }
    }
    public void setTriggers(Set<Trigger> triggers) {
        this.triggers=triggers;
    }
    public void addTrigger(Trigger trigger) {
        this.triggers.add(trigger);
    }
    // TODO: add proper exception management
    public void setDevice(int id) {
        System.out.println("Setting device id..." + id);
        Device deviceNew = new Device();
        try {
            deviceNew.setId((long)id);
            this.device = deviceNew;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
    public void setDevice(Device device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "DeviceSetting{" +
                "id=" + id +
                ", device=" + device +
                ", deviceSettingName='" + deviceSettingName + '\'' +
                ", deviceSettingPriority=" + deviceSettingPriority +
                ", timeWindowFrom=" + timeWindowFrom +
                ", timeWindowTo=" + timeWindowTo +
                ", isActive=" + isActive +
                ", triggerEveryNthDay=" + triggerEveryNthDay +
                ", triggers=" + triggers +
                '}';
    }

    @Transient
    private String timeWindowFromText;
    @Transient
    private String timeWindowToText;

    public String getTimeWindowFromText() {
        if (this.timeWindowFrom == null) {
            this.timeWindowFrom = LocalTime.parse("00:00");
        }
        return this.timeWindowFrom.toString();
    }
    public String getTimeWindowToText() {
        if (this.timeWindowTo == null) {
            this.timeWindowTo = LocalTime.parse("00:00");
        }
        return this.timeWindowTo.toString();
    }


}
