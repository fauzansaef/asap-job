package project.asap.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.asap.job.entity.FileRef;

public interface FileRefRepository extends JpaRepository<FileRef, Long> {
}
