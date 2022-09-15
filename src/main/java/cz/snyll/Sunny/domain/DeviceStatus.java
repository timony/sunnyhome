package cz.snyll.Sunny.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Table(name = "device_statuses")
public class DeviceStatus {
    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private OperationStatus currentState;   // current state of the device
    private float currentConsumption;         // last gathered device actual consumption (watthours)
    private float totalConsumption;         // total consumption
    private float todayConsumption;         // consumption of this day
    private float lastDayTotalConsumption;  // total consumption state on midnight - to count daily consumption
    private long todayRuntime;               // daily runtime of the device (minutes)
    private long currentRuntime;
    private Date dataFreshness;
    private boolean manualOverride;         // if device is manually turned on or off
    private LocalDateTime overriddenUntil;  // when this override ends
    private LocalDateTime lastStateChange;  // time of the last state change (on/off/error)
    private LocalDate dailyResetHappened;     // if daily reset already happened today
    @OneToOne
    @JoinColumn(name = "device_id", referencedColumnName = "id", unique=true)
    private Device device;
    public enum OperationStatus {
        ON, OFF, ERROR
    }
}

