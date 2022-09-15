package cz.snyll.Sunny.services.collectors;

public interface DeviceInfoCollector {
    float getCurrentConsumption();
    float getTotalConsumption();
    float getTodayConsumption();
    boolean getActualStatus();
}
