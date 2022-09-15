package cz.snyll.Sunny.services.automation;

import cz.snyll.Sunny.config.MainConfiguration;
import cz.snyll.Sunny.domain.Device;
import cz.snyll.Sunny.domain.DeviceSetting;
import cz.snyll.Sunny.domain.DeviceStatus;
import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.services.DeviceManagerService;
import cz.snyll.Sunny.services.EventEntryManagerService;
import cz.snyll.Sunny.services.collectors.DeviceInfoCollector;
import cz.snyll.Sunny.services.collectors.DeviceInfoCollectorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class AutomationService {
    @Autowired
    private MainConfiguration mainConfiguration;
    @Autowired
    private DeviceManagerService deviceManagerService;
    @Autowired
    private EventEntryManagerService eventEntryManagerService;

    @Scheduled(fixedDelay = 10000)
    public void AutomateDevices() {
        if (mainConfiguration.isAutomation() == false)
            return;
        ArrayList<Device> devices = deviceManagerService.LoadAll();
        ArrayList<Device> turnOnDevices = new ArrayList<>();
        ArrayList<Device> turnOffDevices = new ArrayList<>();

        for (Device device: devices) {
            // first collect all data for the device
            DeviceInfoCollector deviceInfoCollector = DeviceInfoCollectorFactory.createDeviceInfoCollector(device, eventEntryManagerService);
            if (deviceInfoCollector != null) {
                if (device.getDeviceStatus().getLastDayTotalConsumption() == 0)
                    device.getDeviceStatus().setLastDayTotalConsumption(deviceInfoCollector.getTotalConsumption());
                device.getDeviceStatus().setCurrentConsumption(deviceInfoCollector.getCurrentConsumption());
                device.getDeviceStatus().setTotalConsumption(deviceInfoCollector.getTotalConsumption());
                device.getDeviceStatus().setTodayConsumption(deviceInfoCollector.getTodayConsumption());
            }
            if (device.getDeviceStatus().getCurrentState() == DeviceStatus.OperationStatus.ON) {
                Duration runningTime = Duration.between(device.getDeviceStatus().getLastStateChange(), LocalDateTime.now());
                deviceManagerService.setRunningTime(device, runningTime);
            } else {
                deviceManagerService.setRunningTime(device, Duration.ZERO);
            }
            // check if there is a brand-new day and if so, reset the daily counters
            deviceManagerService.dailyReset(device);
            deviceManagerService.SaveDevice(device);

            if (device.getDeviceSettings() == null) {
                eventEntryManagerService.raiseEvent("DEVICE AUTOMATION: Automation not happening as device " + device.getDeviceName() + " does not have any Device Settings active.", EventEntry.EventType.SUCCESS, 60);
                continue;
            } else {
                boolean anyActive = false;
                for (DeviceSetting deviceSettings: device.getDeviceSettings()) {
                    if (deviceSettings.isActive() == true) {
                        anyActive = true;
                    }
                }
                if (anyActive == false) {
                    eventEntryManagerService.raiseEvent("DEVICE AUTOMATION: Automation not happening as device " + device.getDeviceName() + " does not have any Device Settings active.", EventEntry.EventType.SUCCESS, 60);
                    continue;
                }
            }

            if (device.getDeviceStatus().isManualOverride() == true && LocalDateTime.now().isAfter(device.getDeviceStatus().getOverriddenUntil())) {
                //System.out.println("AUTOMATION: Device - Manual override ended. Resuming automation.");
                deviceManagerService.resetOverride(device);
            }
            if (device.getDeviceStatus().isManualOverride() == true && LocalDateTime.now().isBefore(device.getDeviceStatus().getOverriddenUntil())) {
                //System.out.println("AUTOMATION: Override in place for device " + device.getDeviceName() + ", don't do anything.");
                continue;
            }

            if (deviceManagerService.shouldDeviceTurnOn(device) == true && device.getDeviceStatus().getCurrentState() == DeviceStatus.OperationStatus.OFF) {
                turnOnDevices.add(device);
            }
            if (deviceManagerService.shouldDeviceTurnOn(device) == false && device.getDeviceStatus().getCurrentState() == DeviceStatus.OperationStatus.ON) {
                turnOffDevices.add(device);
            }


            boolean actualStatus = deviceInfoCollector.getActualStatus();
            if (actualStatus == false && device.getDeviceStatus().getCurrentState() == DeviceStatus.OperationStatus.ON) {
                if (device.isForceActualStatus() == true) {
                    eventEntryManagerService.raiseEvent("DEVICE ISSUE: " + device.getDeviceName() + " - There is a mismatch between actual status of the device and database record! Force actual status is ON. Trying to force the database state.", EventEntry.EventType.WARNING, 30);
                    if (deviceManagerService.turnOnDevice(device, false) == true) {
                        eventEntryManagerService.raiseEvent("DEVICE " + device.getDeviceName() + " forced to turned on state.", EventEntry.EventType.WARNING);
                        continue;
                    } else {
                        eventEntryManagerService.raiseEvent("DEVICE " + device.getDeviceName() + "could not be force to turned on state.", EventEntry.EventType.WARNING);
                    }
                } else {
                    eventEntryManagerService.raiseEvent("DEVICE ISSUE: " + device.getDeviceName() + " - There is a mismatch between actual status of the device and database record! Force actual status is not turned on on this device. Not forcing.", EventEntry.EventType.WARNING, 30);
                }
            }
            if (actualStatus == true && device.getDeviceStatus().getCurrentState() == DeviceStatus.OperationStatus.OFF) {
                if (device.isForceActualStatus() == true) {
                    eventEntryManagerService.raiseEvent("DEVICE ISSUE: " + device.getDeviceName() + " - There is a mismatch between actual status of the device and database record! Force actual status is ON. Trying to force the database state.", EventEntry.EventType.WARNING, 30);
                    if (deviceManagerService.turnOffDevice(device, false) == true) {
                        eventEntryManagerService.raiseEvent("DEVICE " + device.getDeviceName() + " forced to turned off state.", EventEntry.EventType.WARNING);
                        continue;
                    } else {
                        eventEntryManagerService.raiseEvent("DEVICE " + device.getDeviceName() + " could not be force to turned off state.", EventEntry.EventType.WARNING);
                    }
                } else {
                    eventEntryManagerService.raiseEvent("DEVICE ISSUE: " + device.getDeviceName() + " - There is a mismatch between actual status of the device and database record! Force actual status is not active on this device. Not forcing.", EventEntry.EventType.WARNING, 30);
                }
            }
        }

        // if there are some devices that needs to be turned off, lets turn off the first one
        if (turnOffDevices.size() > 0) {
            // get the device with the lowest priority to turn off first
            turnOffDevices.sort(Comparator.comparing(Device::getCurrentDevicePriority));
            Device deviceToTurnOff = turnOffDevices.get(0);
            if (deviceManagerService.turnOffDevice(deviceToTurnOff, false) == true) {
                eventEntryManagerService.raiseEvent("Device " + deviceToTurnOff.getDeviceName() + " turned OFF successfully.", EventEntry.EventType.SUCCESS);
            } else {
                eventEntryManagerService.raiseEvent("Device " + deviceToTurnOff.getDeviceName() + " - turn OFF was not confirmed!", EventEntry.EventType.ERROR);
            }
            turnOffDevices = new ArrayList<>();
            return;
        }

        // if some devices needs to be turned on, lets turn on one with highest priority
        if (turnOnDevices.size() > 0) {
            // get the device with the highest priority to turn on first
            turnOnDevices.sort(Comparator.comparing(Device::getCurrentDevicePriority).reversed());
            Device deviceToTurnOn = turnOnDevices.get(0);
            if (deviceManagerService.turnOnDevice(deviceToTurnOn, false) == true) {
                eventEntryManagerService.raiseEvent("Device " + deviceToTurnOn.getDeviceName() + " turned ON successfully.", EventEntry.EventType.SUCCESS);
            } else {
                eventEntryManagerService.raiseEvent("Device " + deviceToTurnOn.getDeviceName() + " - turn ON was not confirmed!", EventEntry.EventType.ERROR);
            }
            turnOnDevices = new ArrayList<>();
        }
    }
}
