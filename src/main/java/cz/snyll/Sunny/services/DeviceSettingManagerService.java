package cz.snyll.Sunny.services;

import cz.snyll.Sunny.domain.DeviceSetting;
import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.domain.Trigger;
import cz.snyll.Sunny.repositories.DeviceSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;

@Service
public class DeviceSettingManagerService implements DeviceSettingManager {
    @Autowired
    private EventEntryManagerService eventEntryManagerService;
    private DeviceSettingRepository deviceSettingRepository;
    @Autowired
    public DeviceSettingManagerService(DeviceSettingRepository deviceSettingRepository) {
        this.deviceSettingRepository = deviceSettingRepository;
    }

    @Override
    public void SaveDeviceSetting(DeviceSetting deviceSetting) {
        if (deviceSetting.getId() == null)
            this.eventEntryManagerService.raiseEvent("DEVICE SETTING: Adding new Device Setting with name: " + deviceSetting.getDeviceSettingName(), EventEntry.EventType.SUCCESS);
        this.deviceSettingRepository.save(deviceSetting);

    }

    @Override
    public void DeleteDeviceSetting(DeviceSetting deviceSetting) {
        this.deviceSettingRepository.delete(deviceSetting);
        this.eventEntryManagerService.raiseEvent("DEVICE SETTING: Device Setting deleted. Name: " + deviceSetting.getDeviceSettingName(), EventEntry.EventType.WARNING);
    }

    @Override
    public ArrayList<DeviceSetting> LoadAll() {
        return (ArrayList<DeviceSetting>)this.deviceSettingRepository.findAll();
    }

    @Override
    public boolean evaluateDeviceSetting(DeviceSetting deviceSetting) {
        LocalTime currentTime = LocalTime.now();

        // if Device Setting is not active, skip it
        if (deviceSetting.isActive() == false)
            return false;

        // check if we are in the time window of this Device Setting
        if (currentTime.isBefore(deviceSetting.getTimeWindowFrom()) || currentTime.isAfter(deviceSetting.getTimeWindowTo()))
            return false;

        for (Trigger trigger: deviceSetting.getTriggers()) {
            if (trigger.shouldBeOn() == false)
                return false;
        }
        System.out.println("Device Setting: " + deviceSetting.getDeviceSettingName() + " evaluated to true.");
        return true;
    }

    @Override
    public void DeleteDeviceSettingById(long id) {
        this.deviceSettingRepository.deleteById(id);
        System.out.println("DELETED ID: " + id);
        this.eventEntryManagerService.raiseEvent("DEVICE SETTING: Device Setting deleted. ID: " + id, EventEntry.EventType.WARNING);
    }
}
