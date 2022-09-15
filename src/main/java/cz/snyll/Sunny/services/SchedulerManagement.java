package cz.snyll.Sunny.services;

import cz.snyll.Sunny.config.SchedulerConfig;
import cz.snyll.Sunny.domain.EventEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class SchedulerManagement {
    @Autowired
    private EventEntryManagerService eventEntryManagerService;
    private SchedulerConfig schedulerConfig;
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    public SchedulerManagement(SchedulerConfig schedulerConfig) {
        this.schedulerConfig = schedulerConfig;
    }

    @Scheduled (fixedRate = 10000)
    public void checkSchedules() {

        this.taskScheduler = schedulerConfig.getTaskScheduler();

        if (taskScheduler.getActiveCount() >= (schedulerConfig.getPOOL_SIZE() - 5)) {
            eventEntryManagerService.raiseEvent("THRES: There is too many threads running, applicaiton might get stuck soon.", EventEntry.EventType.ERROR);
        }
        System.out.println("Scheduled active tasks: " + taskScheduler.getActiveCount());
    }
}
