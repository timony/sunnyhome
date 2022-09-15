package cz.snyll.Sunny.services;

import cz.snyll.Sunny.domain.Device;

import java.util.ArrayList;

public interface DeviceManager {
    void SaveDevice(Device device);
    void DeleteDevice(Device device);
    Device LoadDevice(Long id);
    ArrayList<Device> LoadAll();
    boolean shouldDeviceTurnOn(Device device);
    boolean turnOnDevice(Device device, boolean manual);
    boolean turnOffDevice(Device device, boolean manual);
    void dailyReset(Device device);
    boolean manualToggle(Device device);
    void DeleteDeviceById(long id);
}
