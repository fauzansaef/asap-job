package project.asap.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.asap.job.entity.FileRef;

import java.util.List;

public interface FileRefRepository extends JpaRepository<FileRef, Long> {
    List<FileRef> findByFlagLoader(Integer flagLoader);
}
