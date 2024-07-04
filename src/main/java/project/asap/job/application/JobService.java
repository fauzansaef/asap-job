package project.asap.job.application;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.asap.job.repository.FileRefRepository;

@Component
public class JobService {
    private static final Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JobService.class);
    private final FileRefRepository fileRefRepository;

    public JobService(FileRefRepository fileRefRepository) {
        this.fileRefRepository = fileRefRepository;
    }


    //    @Scheduled(cron = "0 */5 * * * ?") // This will run every 5 minutes
    @Scheduled(cron = "0 * * * * ?") // This will run every minute
    @Async
    @Transactional
    public void runJob() {
        logger.info("Job is running");
    }
}
