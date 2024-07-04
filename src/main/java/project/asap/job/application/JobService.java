package project.asap.job.application;

import ch.qos.logback.classic.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.asap.job.entity.FileRef;
import project.asap.job.repository.FileRefRepository;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class JobService {
    @Value("${file.directory}")
    private String fileDirectory;
    private static final Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JobService.class);
    private final FileRefRepository fileRefRepository;
    static String[] HEADERS = { "KODE_BATCH", "KODE_ISI_BATCH", "KETERANGAN", "Published" };
    static String SHEET = "Tutorials";

    public JobService(FileRefRepository fileRefRepository) {
        this.fileRefRepository = fileRefRepository;
    }


    //    @Scheduled(cron = "0 */5 * * * ?") // This will run every 5 minutes
    @Scheduled(cron = "0 * * * * ?") // This will run every minute
    @Async
    @Transactional
    public void runJob() {

        List<FileRef> fileRefs = fileRefRepository.findByFlagLoader(0);

        for (FileRef fileRef : fileRefs) {
            logger.info("Processing file: " + fileRef.getFileName());
            try {
                Path path = Paths.get(fileDirectory + fileRef.getFileName());
                InputStream inputStream = Files.newInputStream(path);
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);


                fileRef.setFlagLoader(1);
                fileRefRepository.save(fileRef);
                logger.info("load file : " + fileRef.getFileName() + " processing");


            } catch (Exception e) {
                logger.error("error reading excel file : " + fileRef.getFileName(), e);
            }


            fileRef.setFlagLoader(2);
            fileRefRepository.save(fileRef);
            logger.info("load file : " + fileRef.getFileName() + " success");

        }


    }
}
