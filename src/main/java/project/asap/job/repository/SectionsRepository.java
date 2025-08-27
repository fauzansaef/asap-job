package project.asap.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.asap.job.entity.Sections;

public interface SectionsRepository extends JpaRepository<Sections, Long> {
}
