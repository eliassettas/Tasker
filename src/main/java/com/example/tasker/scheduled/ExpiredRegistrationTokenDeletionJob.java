package com.example.tasker.scheduled;

import java.util.List;
import java.util.stream.Collectors;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.tasker.model.persistence.RegistrationToken;
import com.example.tasker.service.RegistrationTokenService;

@Component
@DisallowConcurrentExecution
public class ExpiredRegistrationTokenDeletionJob extends QuartzScheduledJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredRegistrationTokenDeletionJob.class);

    @Value("${jobs.expired-registration-token-deletion.name}")
    private String jobName;

    @Value("${jobs.expired-registration-token-deletion.cron-expression}")
    private String cronExpression;

    private final RegistrationTokenService registrationTokenService;

    public ExpiredRegistrationTokenDeletionJob(RegistrationTokenService registrationTokenService) {
        this.registrationTokenService = registrationTokenService;
    }

    @Override
    public void executeInternal(JobExecutionContext jec) {
        List<Long> expiredTokenIds = registrationTokenService.getExpiredRegistrationTokens().stream()
                .map(RegistrationToken::getId)
                .collect(Collectors.toList());
        LOGGER.info("Deleting a total of {} expired registration tokens", expiredTokenIds.size());
        expiredTokenIds.forEach(registrationTokenService::deleteById);
    }

    public String getJobName() {
        return jobName;
    }

    public String triggerCron() {
        return cronExpression;
    }
}
