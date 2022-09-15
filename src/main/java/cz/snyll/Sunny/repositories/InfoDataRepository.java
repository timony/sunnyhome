package cz.snyll.Sunny.repositories;

import cz.snyll.Sunny.domain.InfoData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface InfoDataRepository extends CrudRepository<InfoData, Long> {
    InfoData findByDataKey(String dataKey);
    @Query(value = "select * from info_data where data_key like :prefix%", nativeQuery = true)
    Set<InfoData> findByPrefix(@Param("prefix") String prefix);
}
