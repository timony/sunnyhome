package cz.snyll.Sunny.services;

import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.domain.Trigger;
import cz.snyll.Sunny.repositories.TriggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

@Service
public class TriggerManagerService implements TriggerManager {
    private TriggerRepository triggerRepository;
    @Autowired
    public TriggerManagerService(TriggerRepository triggerRepository) {
        this.triggerRepository = triggerRepository;
    }
    @Autowired
    private EventEntryManagerService eventEntryManagerService;
    @Override
    public void SaveTrigger(Trigger trigger) {
        if (trigger.getId() == null) {
            trigger.setStatusUpdatedTime(new Date(System.currentTimeMillis()));
            trigger.setTriggerState(false);
            eventEntryManagerService.raiseEvent("TRIGGER: Adding Trigger with name: " + trigger.getTriggerName(), EventEntry.EventType.SUCCESS);
        }
        if (trigger.getStatusUpdatedTime() == null)
            trigger.setStatusUpdatedTime(new Date(System.currentTimeMillis()));
        this.triggerRepository.save(trigger);

    }
    @Override
    public void DeleteTrigger(Trigger trigger) {
        this.triggerRepository.delete(trigger);
        eventEntryManagerService.raiseEvent("TRIGGER: Trigger deleted. Name: " + trigger.getTriggerName(), EventEntry.EventType.WARNING);
    }

    @Override
    public Trigger LoadTrigger(Long id) {
        return this.triggerRepository.findById(id).get();
    }

    @Override
    public ArrayList<Trigger> LoadAll() {
        return (ArrayList<Trigger>) this.triggerRepository.findAll();
    }

    @Override
    public void setTriggerState(boolean state, Trigger trigger) {
        if (trigger.getStatusUpdatedTime() == null)
            trigger.setStatusUpdatedTime(new Date(System.currentTimeMillis()));
        if (trigger.isTriggerState() == state)
            return;
        trigger.setStatusUpdatedTime(new Date(System.currentTimeMillis()));
        trigger.setTriggerState(state);
        this.triggerRepository.save(trigger);
    }
    @Override
    public void DeleteTriggerById(long id) {
        this.triggerRepository.deleteById(id);
        eventEntryManagerService.raiseEvent("TRIGGER: Trigger deleted. ID: " + id, EventEntry.EventType.WARNING);
    }
}