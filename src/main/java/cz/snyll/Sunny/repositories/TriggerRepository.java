package cz.snyll.Sunny.repositories;

import cz.snyll.Sunny.domain.Trigger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TriggerRepository extends CrudRepository<Trigger, Long> {
    Set<Trigger> findByTriggerName(String triggerName);
}
