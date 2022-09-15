package cz.snyll.Sunny.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    @Getter
    private final int POOL_SIZE = 50;
    @Getter
    private ThreadPoolTaskScheduler taskScheduler;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task-pool-");
        threadPoolTaskScheduler.initialize();

        scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);

        this.taskScheduler = threadPoolTaskScheduler;

    }
}