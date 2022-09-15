package cz.snyll.Sunny.services;

import cz.snyll.Sunny.domain.DeviceSetting;

import java.util.ArrayList;

public interface DeviceSettingManager {
    void SaveDeviceSetting(DeviceSetting deviceSetting);
    void DeleteDeviceSetting(DeviceSetting deviceSetting);
    ArrayList<DeviceSetting> LoadAll();
    boolean evaluateDeviceSetting(DeviceSetting deviceSetting);
    void DeleteDeviceSettingById(long id);
}
