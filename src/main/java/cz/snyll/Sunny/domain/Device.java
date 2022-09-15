package cz.snyll.Sunny.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "devices")
@NoArgsConstructor
public class Device {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL)
    private DeviceStatus deviceStatus;
    @Size(min = 3, message = "Please enter some Device name at least 3 characters long.")
    private String deviceName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device", fetch = FetchType.LAZY)
    private Set<DeviceSetting> deviceSettings;

    private int deviceConsumption;          // stated consumption in Watts

    private int minRuntime;                 // minimum runtime after turning on
    private int maxRuntime;                 // maximum runtime after turning on
    private int maxRuntimeDaily;            // maximum daily running time
    private boolean forceActualStatus;      // if actual on/off status is different than status in databse, force the status also into database
    private String deviceIP;                // IP address of this device (or IP address of the smart plug etc.)
    private float maxDailyPowerConsumption; // stop the device when this is reached (in Watthours)
    @NotBlank(message = "Please enter turn on API url.")
    private String turnOnApiUrl;            // URL to the api command to turn on this device
    private String turnOnPostData;          // what post data to send with the turn on url request
    @NotBlank(message = "Please enter turn off API url.")
    private String turnOffApiUrl;           // URL to the api command to turn off this device
    private String turnOffPostData;         // what post data to send with the turn off url request
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_timestamp")
    private Date updatedTimestamp;
    @Transient
    private int currentDevicePriority;
    private String controlDeviceName;
}