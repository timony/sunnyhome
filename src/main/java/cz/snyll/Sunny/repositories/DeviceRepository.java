package cz.snyll.Sunny.repositories;

import cz.snyll.Sunny.domain.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Long> {
    Set<Device> findByDeviceName(String deviceName);
}
