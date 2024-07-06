package project.asap.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.asap.job.entity.FailedJobsExcel;

public interface FailedJobsExcelRepository extends JpaRepository<FailedJobsExcel, Long> {
}
