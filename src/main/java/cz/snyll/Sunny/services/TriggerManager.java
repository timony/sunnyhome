package cz.snyll.Sunny.services;

import cz.snyll.Sunny.domain.Trigger;

import java.util.ArrayList;

public interface TriggerManager {
    void SaveTrigger(Trigger trigger);
    void DeleteTrigger(Trigger trigger);
    Trigger LoadTrigger(Long id);
    ArrayList<Trigger> LoadAll();
    void setTriggerState(boolean state, Trigger trigger);
    void DeleteTriggerById(long id);
}
