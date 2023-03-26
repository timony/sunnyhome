package cz.snyll.Sunny.repositories;

import cz.snyll.Sunny.domain.DeviceSetting;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface DeviceSettingRepository extends CrudRepository<DeviceSetting, Long> {
    @Modifying
    @Query(value = "DELETE FROM devicesettings c where c.id = :id", nativeQuery = true)
    public void deleteById(@Param("id") long id);
}
