package com.example.transactionretryreplay.service;




import com.example.transactionretryreplay.config.JobConfig;
import com.example.transactionretryreplay.job.RetryableJob;
import com.example.transactionretryreplay.notifications.EmailService;
import com.example.transactionretryreplay.notifications.EmailTemplateConfig;
import com.example.transactionretryreplay.retry.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetrySchedulerService {

    private final SchedulerFactoryBean schedulerFactoryBean;
    private final ApplicationContext applicationContext;
    private final JobConfig jobConfig;

    @PostConstruct
    public void initializeJobs() throws SchedulerException {
        if (jobConfig.getDefinitions() != null) {
            for (Map.Entry<String, JobConfig.JobDefinition> entry : jobConfig.getDefinitions().entrySet()) {
                String jobName = entry.getKey();
                JobConfig.JobDefinition definition = entry.getValue();
                scheduleJob(jobName, definition);
            }
        }
    }

    private void scheduleJob(String jobName, JobConfig.JobDefinition definition) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(RetryJobExecutor.class)
                .withIdentity(jobName, "retryGroup")
                .usingJobData("targetBeanName", definition.getTargetBeanName())
                .usingJobData("targetMethodName", definition.getTargetMethodName())
                .usingJobData("retryStrategyType", definition.getRetryStrategy())
                .usingJobData("fixedInterval", definition.getFixedInterval())
                .usingJobData("exponentialInitialInterval", definition.getExponentialInitialInterval())
                .usingJobData("exponentialMultiplier", definition.getExponentialMultiplier())
                .usingJobData("circuitBreakerFailureThreshold", definition.getCircuitBreakerFailureThreshold())
                .usingJobData("circuitBreakerResetTimeout", definition.getCircuitBreakerResetTimeout())
                .usingJobData("circuitBreakerHalfOpenAttempts", definition.getCircuitBreakerHalfOpenAttempts())
                .usingJobData("jitterFactor", definition.getJitterFactor())
                .usingJobData("maxAttempts", definition.getMaxAttempts())
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "Trigger", "retryGroup")
                .startNow() // Or define a cron schedule if needed
                .build();

        schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
        log.info("Scheduled retry job: {}", jobName);
    }

    public static class RetryJobExecutor extends org.springframework.scheduling.quartz.QuartzJobBean {
        private String targetBeanName;
        private String targetMethodName;
        private String retryStrategyType;
        private long fixedInterval;
        private long exponentialInitialInterval;
        private double exponentialMultiplier;
        private int circuitBreakerFailureThreshold;
        private long circuitBreakerResetTimeout;
        private int circuitBreakerHalfOpenAttempts;
        private long jitterFactor;
        private int maxAttempts;
        private int attemptCount = 0;
        private RetryStrategy currentRetryStrategy;
        private CircuitBreakerStrategy circuitBreaker;
        private ApplicationContext applicationContext;
        private TemplateEngine templateEngine; // Need TemplateEngine as well

        @Override
        protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
            JobDataMap dataMap = context.getMergedJobDataMap();
            targetBeanName = dataMap.getString("targetBeanName");
            targetMethodName = dataMap.getString("targetMethodName");
            retryStrategyType = dataMap.getString("retryStrategyType");
            fixedInterval = dataMap.getLong("fixedInterval");
            exponentialInitialInterval = dataMap.getLong("exponentialInitialInterval");
            exponentialMultiplier = dataMap.getDouble("exponentialMultiplier");
            circuitBreakerFailureThreshold = dataMap.getInt("circuitBreakerFailureThreshold");
            circuitBreakerResetTimeout = dataMap.getLong("circuitBreakerResetTimeout");
            circuitBreakerHalfOpenAttempts = dataMap.getInt("circuitBreakerHalfOpenAttempts");
            jitterFactor = dataMap.getLong("jitterFactor");
            maxAttempts = dataMap.getInt("maxAttempts");
            try {
                applicationContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
            templateEngine = applicationContext.getBean(TemplateEngine.class); // Get TemplateEngine

            EmailService emailService = applicationContext.getBean(EmailService.class);
            EmailTemplateConfig emailTemplateConfig = applicationContext.getBean(EmailTemplateConfig.class);

            if (currentRetryStrategy == null) {
                initializeRetryStrategy();
            }

            Exception executionException = null; // To track if an exception occurred

            try {
                attemptCount++;
                log.info("Executing job: {}, attempt: {}", context.getJobDetail().getKey().getName(), attemptCount);
                RetryableJob targetJob = (RetryableJob) applicationContext.getBean(targetBeanName);
                targetJob.execute();
                if (circuitBreaker != null) {
                    circuitBreaker.recordSuccess();
                }
                log.info("Job: {} executed successfully.", context.getJobDetail().getKey().getName());
                // Send successful retry notification if it wasn't the first attempt
                if (attemptCount > 1) {
                    Map<String, Object> model = new HashMap<>();
                    model.put("jobName", context.getJobDetail().getKey().getName());
                    model.put("attemptCount", attemptCount);
                    model.put("success", true);
                    emailService.sendEmail(
                            "admin@example.com", // Configure recipient properly
                            emailTemplateConfig.getRetrySuccessSubject(),
                            "retry-completion.html",
                            model
                    );
                }
            } catch (Exception e) {
                executionException = e;
                log.error("Job: {} failed on attempt {}: {}", context.getJobDetail().getKey().getName(), attemptCount, e.getMessage());
                if (attemptCount < maxAttempts) {
                    long delay = 0;
                    try {
                        if (circuitBreaker != null) {
                            circuitBreaker.recordFailure();
                            delay = currentRetryStrategy.getNextDelay(attemptCount);
                        } else {
                            delay = currentRetryStrategy.getNextDelay(attemptCount);
                        }
                    } catch (IllegalStateException cbException) {
                        log.warn("Circuit breaker open for job: {}", context.getJobDetail().getKey().getName());
                        return; // Don't reschedule if circuit breaker is open
                    }

                    JobDetail jobDetail = context.getJobDetail();
                    Trigger newTrigger = TriggerBuilder.newTrigger()
                            .withIdentity(jobDetail.getKey().getName() + "RetryTrigger-" + attemptCount, "retryGroup")
                            .startAt(new Date(System.currentTimeMillis() + delay))
                            .build();

                    try {
                        context.getScheduler().scheduleJob(newTrigger);
                        log.info("Rescheduled job: {} for retry in {}ms", jobDetail.getKey().getName(), delay);
                    } catch (SchedulerException ex) {
                        log.error("Error rescheduling job: {}", ex.getMessage());
                    }
                } else {
                    log.error("Max retry attempts reached for job: {}", context.getJobDetail().getKey().getName());
                    // Send notification on final failure
                    Map<String, Object> model = new HashMap<>();
                    model.put("jobName", context.getJobDetail().getKey().getName());
                    model.put("maxAttempts", maxAttempts);
                    model.put("errorMessage", e.getMessage());
                    emailService.sendEmail(
                            "admin@example.com", // Configure recipient properly
                            emailTemplateConfig.getRetryFailureSubject(),
                            "retry-completion.html",
                            model
                    );
                    // Option to create an incident (logging a specific event might be sufficient)
                    log.error("Incident: Max retry attempts reached for job: {}", context.getJobDetail().getKey().getName());
                }
            }
        }
        private void initializeRetryStrategy() {
            switch (retryStrategyType.toLowerCase()) {
                case "fixed":
                    currentRetryStrategy = new FixedIntervalStrategy(fixedInterval);
                    break;
                case "exponential":
                    currentRetryStrategy = new ExponentialBackoffStrategy(exponentialInitialInterval, exponentialMultiplier);
                    break;
                case "circuitbreaker":
                    circuitBreaker = new CircuitBreakerStrategy(circuitBreakerFailureThreshold, circuitBreakerResetTimeout, circuitBreakerHalfOpenAttempts);
                    currentRetryStrategy = circuitBreaker; // Use CB as base for potential jitter
                    break;
                default:
                    throw new IllegalArgumentException("Invalid retry strategy: " + retryStrategyType);
            }
            if (jitterFactor > 0) {
                currentRetryStrategy = new JitterStrategy(currentRetryStrategy, jitterFactor);
            }
        }
    }
}