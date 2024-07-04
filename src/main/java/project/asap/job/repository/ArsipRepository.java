package project.asap.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.asap.job.entity.Arsip;

public interface ArsipRepository extends JpaRepository<Arsip, Long> {
}
