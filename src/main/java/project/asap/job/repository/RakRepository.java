package project.asap.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.asap.job.entity.Rak;

import java.util.Optional;

public interface RakRepository extends JpaRepository<Rak, Long> {
    Optional<Rak> findByCode(String code);
}
