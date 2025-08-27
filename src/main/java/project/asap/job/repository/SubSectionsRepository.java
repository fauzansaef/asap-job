package project.asap.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.asap.job.entity.SubSections;

import java.util.List;

public interface SubSectionsRepository extends JpaRepository<SubSections, Long> {
    List<SubSections> findAllBySectionId(Long id);
}
