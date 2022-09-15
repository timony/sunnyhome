package cz.snyll.Sunny.services;

import cz.snyll.Sunny.domain.Device;
import cz.snyll.Sunny.domain.DeviceSetting;
import cz.snyll.Sunny.domain.DeviceStatus;
import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.repositories.DeviceRepository;
import cz.snyll.Sunny.services.automation.DeviceAPIService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

@Slf4j
@Data
@Service
public class DeviceManagerService implements DeviceManager {
    @Autowired
    private DeviceSettingManagerService deviceSettingManagerService;
    @Autowired
    private EventEntryManagerService eventEntryManagerService;
    private DeviceRepository deviceRepository;
    @Autowired
    public DeviceManagerService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    @Autowired
    private DeviceAPIService deviceAPIService;

    @Override
    public void SaveDevice(Device device) {
        if (device.getDeviceStatus() == null) {
            DeviceStatus deviceStatus = new DeviceStatus();
            deviceStatus.setCurrentState(DeviceStatus.OperationStatus.OFF);
            deviceStatus.setLastStateChange(LocalDateTime.now());
            deviceStatus.setDailyResetHappened(LocalDate.now().minusDays(1));
            deviceStatus.setDevice(device);
            device.setDeviceStatus(deviceStatus);
        }
        if (device.getId() == null) {
            eventEntryManagerService.raiseEvent("DEVICE: Adding new device with name: " + device.getDeviceName(), EventEntry.EventType.SUCCESS);
        } else {

        }
        this.deviceRepository.save(device);
    }

    @Override
    public void DeleteDevice(Device device) {
        this.deviceRepository.deleteById(device.getId());
        eventEntryManagerService.raiseEvent("DEVICE: Device deleted. Name: " + device.getDeviceName(), EventEntry.EventType.WARNING);
    }

    @Override
    public void DeleteDeviceById(long id) {
        this.deviceRepository.deleteById(id);
        eventEntryManagerService.raiseEvent("DEVICE: Device deleted. ID: " + id, EventEntry.EventType.WARNING);
    }

    @Override
    public Device LoadDevice(Long id) {
        Device device = deviceRepository.findById(id).get();
        device.setCurrentDevicePriority(0);
        return device;
    }

    @Override
    public ArrayList<Device> LoadAll() {
        ArrayList<Device> allDevicesToReturn = new ArrayList<>();
        ArrayList<Device> allDevices = (ArrayList<Device>) deviceRepository.findAll();
        for (Device device: allDevices) {
            allDevicesToReturn.add(LoadDevice(device.getId()));
        }
        return allDevicesToReturn;
    }

    @Override
    public boolean shouldDeviceTurnOn(Device device) {
        Duration runningTime = Duration.ZERO;
        if (device.getDeviceStatus().getCurrentState() == DeviceStatus.OperationStatus.ON)
            runningTime = Duration.between(device.getDeviceStatus().getLastStateChange(), LocalDateTime.now());

        if (device.getDeviceStatus() == null) {
            System.out.println("Device has no DeviceStatus. That is an inconsistent state.");
            eventEntryManagerService.raiseEvent("Inconsistent state of device " + device.getDeviceName() + ". It seems not to have any Device Status assigned.", EventEntry.EventType.ERROR);
            return false;
        }
        if (device.getDeviceSettings() == null) {
            System.out.println("Device has no DeviceSetting. Cannot turn on without it.");
            return false;
        }

        if (device.getDeviceStatus().getTodayConsumption() >= device.getMaxDailyPowerConsumption() && device.getMaxDailyPowerConsumption() > 0) {
            System.out.println("AUTOMATION: Device - Today max consumption reached. Turning OFF.");
            return false;
        }
        if ((device.getDeviceStatus().getTodayRuntime() + device.getDeviceStatus().getCurrentRuntime()) >= device.getMaxRuntimeDaily() && device.getMaxRuntimeDaily() > 0) {
            System.out.println("AUTOMATION: Device - Today's max runtime reached. Turning OFF.");
            return false;
        }
        System.out.println("Device Status: " + device.getDeviceStatus().toString());


        System.out.println("Device: " + device.getDeviceName() + "; running time: " + runningTime.toMinutes() + "; maxRuntime: " + device.getMaxRuntime());
        if (runningTime.toMinutes() >= device.getMaxRuntime() && device.getMaxRuntime() > 0) {
            System.out.println("AUTOMATION: Device - max runtime After Turn on reached. Turning OFF.");
            return false;
        }
        if (runningTime.toMinutes() < device.getMinRuntime() && device.getMinRuntime() > 0 && device.getDeviceStatus().getCurrentState() == DeviceStatus.OperationStatus.ON) {
            System.out.println("AUTOMATION: Device - runtime after turn on not yet reached. Should be ON.");
            return true;
        }
        for (DeviceSetting deviceSetting: device.getDeviceSettings()) {
            System.out.println("AUTOMATION: Checking Device Setting - " + deviceSetting.getDeviceSettingName());
            if (deviceSettingManagerService.evaluateDeviceSetting(deviceSetting) == true) {
                if (device.getCurrentDevicePriority() < deviceSetting.getDeviceSettingPriority())
                    device.setCurrentDevicePriority(deviceSetting.getDeviceSettingPriority());
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean manualToggle(Device device) {
        if (device.getDeviceStatus().isManualOverride() == true) {
            resetOverride(device);
            return true;
        }
        if (device.getDeviceStatus().getCurrentState() == DeviceStatus.OperationStatus.ON) {
            return turnOffDevice(device, true);
        }
        if (device.getDeviceStatus().getCurrentState() == DeviceStatus.OperationStatus.OFF) {
            return turnOnDevice(device, true);
        }
        return false;
    }

    public void resetOverride(Device device) {
        device.getDeviceStatus().setOverriddenUntil(LocalDateTime.now().minusHours(1));
        device.getDeviceStatus().setManualOverride(false);
        this.SaveDevice(device);
    }

    @Override
    public boolean turnOnDevice(Device device, boolean manual) {
        if (deviceAPIService.TurnOnDevice(device) == true) {
            device.getDeviceStatus().setLastStateChange(LocalDateTime.now());
            device.getDeviceStatus().setCurrentState(DeviceStatus.OperationStatus.ON);
            device.getDeviceStatus().setCurrentRuntime(0);
            if (manual == true) {
                device.getDeviceStatus().setManualOverride(true);
                device.getDeviceStatus().setOverriddenUntil(LocalDateTime.now().plusHours(1));
            }
            this.SaveDevice(device);
            return true;
        }
        return false;
    }

    @Override
    public boolean turnOffDevice(Device device, boolean manual) {
        if (deviceAPIService.TurnOffDevice(device) == true) {
            device.getDeviceStatus().setLastStateChange(LocalDateTime.now());
            setTodayRuntime(device);
            device.getDeviceStatus().setCurrentRuntime(0);
            if (manual == true) {
                device.getDeviceStatus().setManualOverride(true);
                device.getDeviceStatus().setOverriddenUntil(LocalDateTime.now().plusHours(1));
            }
            device.getDeviceStatus().setCurrentState(DeviceStatus.OperationStatus.OFF);
            this.SaveDevice(device);
            return true;
        }
        return false;
    }

    @Override
    public void dailyReset(Device device) {
        if (device.getDeviceStatus().getDailyResetHappened().isBefore(LocalDate.now())) {
            device.getDeviceStatus().setLastDayTotalConsumption(device.getDeviceStatus().getTotalConsumption());
            device.getDeviceStatus().setTodayConsumption(0);
            device.getDeviceStatus().setTodayRuntime(0);
            device.getDeviceStatus().setDailyResetHappened(LocalDate.now());
        }
    }

    public void setRunningTime(Device device, Duration runningTime) {
        device.getDeviceStatus().setCurrentRuntime(runningTime.toMinutes());
        SaveDevice(device);
    }

    public void setTodayRuntime(Device device) {
        if (device.getDeviceStatus().getCurrentState() != DeviceStatus.OperationStatus.ON)
            return;
        // check if the running time is longer than count of minutes since midnight
        long minuteCount = 0;
        if (device.getDeviceStatus().getCurrentRuntime() >= LocalTime.now().get(ChronoField.MINUTE_OF_DAY)) {
            minuteCount = LocalTime.now().get(ChronoField.MINUTE_OF_DAY);
        } else {
            minuteCount = device.getDeviceStatus().getCurrentRuntime() + device.getDeviceStatus().getTodayRuntime();
        }
        device.getDeviceStatus().setTodayRuntime(minuteCount);
    }

}