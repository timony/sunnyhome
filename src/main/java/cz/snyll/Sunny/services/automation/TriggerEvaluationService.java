package cz.snyll.Sunny.services.automation;

import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.domain.InfoData;
import cz.snyll.Sunny.domain.Trigger;
import cz.snyll.Sunny.repositories.InfoDataRepository;
import cz.snyll.Sunny.services.EventEntryManagerService;
import cz.snyll.Sunny.services.TriggerManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TriggerEvaluationService implements TriggerEvaluation {
    @Autowired
    private TriggerManagerService triggerManagerService;
    @Autowired
    private InfoDataRepository infoDataRepository;
    @Autowired
    private EventEntryManagerService eventEntryManagerService;

    @Scheduled(fixedDelay = 5000)
    @Override
    public void EvaluateTriggers() {
        for (Trigger trigger: triggerManagerService.LoadAll()) {
            float val = trigger.getInfoDataValue();
            String key = trigger.getInfoDataKey();
            InfoData infoData = infoDataRepository.findByDataKey(key);
            boolean EvaluatedState = false;
            float infoDataVal = 0f;
            if (infoData != null) {
                try {
                    infoDataVal = Float.parseFloat(infoData.getDataValue());
                    //System.out.println("TRIGGER EVAL: Comparing " + infoDataVal + " " + trigger.getTriggerOperation() + " " + val);
                } catch (Exception e) {
                    eventEntryManagerService.raiseEvent("Trigger " + trigger.getTriggerName() + " with id " + trigger.getId() + ": could not parse InfoData value into float.", EventEntry.EventType.WARNING);
                    triggerManagerService.setTriggerState(false, trigger);
                }
                switch (trigger.getTriggerOperation()) {
                    case EQUALS -> {
                        if (infoDataVal == val) {
                            triggerManagerService.setTriggerState(true, trigger);
                        } else {
                            triggerManagerService.setTriggerState(false, trigger);
                        }
                    }
                    case LESSTHAN -> {
                        if (infoDataVal < val) {
                            triggerManagerService.setTriggerState(true, trigger);
                        } else {
                            triggerManagerService.setTriggerState(false, trigger);
                        }
                    }
                    case GREATERTHAN -> {
                        if (infoDataVal > val) {
                            triggerManagerService.setTriggerState(true, trigger);
                        } else {
                            triggerManagerService.setTriggerState(false, trigger);
                        }
                    }
                    case LESSOREQUAL -> {
                        if (infoDataVal <= val) {
                            triggerManagerService.setTriggerState(true, trigger);
                        } else {
                            triggerManagerService.setTriggerState(false, trigger);
                        }
                    }
                    case GREATEROREQUAL -> {
                        if (infoDataVal >= val) {
                            triggerManagerService.setTriggerState(true, trigger);
                        } else {
                            triggerManagerService.setTriggerState(false, trigger);
                        }
                    }
                }
            } else {
                eventEntryManagerService.raiseEvent("Trigger " + trigger.getTriggerName() + " with id " + trigger.getId() + " has not existing InfoData key name.", EventEntry.EventType.WARNING);
                triggerManagerService.setTriggerState(false, trigger);
            }
        }
    }
}
