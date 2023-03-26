package cz.snyll.Sunny.services.collectors;

import cz.snyll.Sunny.domain.Device;
import cz.snyll.Sunny.services.EventEntryManagerService;
import org.springframework.stereotype.Service;

@Service
public class DeviceInfoCollectorFactory {
    public static DeviceInfoCollector createDeviceInfoCollector (Device device, EventEntryManagerService eventEntryManagerService) {
        if (device.getControlDeviceName() == null)
            return null;
        if (device.getControlDeviceName().equals("ShellyPlugS"))
            return new ShellyPlugSInfoCollector(device, eventEntryManagerService);
        if (device.getControlDeviceName().equals("Shelly1"))
            return new Shelly1InfoCollector(device, eventEntryManagerService);
        if (device.getControlDeviceName().equals("Shelly1PM"))
            return new Shelly1PMInfoCollector(device, eventEntryManagerService);
        if (device.getControlDeviceName().equals("Shelly1PMPlus"))
            return new Shelly1PMPlusInfoCollector(device, eventEntryManagerService);
        return null;
    }
}
