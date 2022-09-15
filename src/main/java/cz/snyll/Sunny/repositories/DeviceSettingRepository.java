package cz.snyll.Sunny.repositories;

import cz.snyll.Sunny.domain.DeviceSetting;
import org.springframework.data.repository.CrudRepository;

public interface DeviceSettingRepository extends CrudRepository<DeviceSetting, Long> {
}
