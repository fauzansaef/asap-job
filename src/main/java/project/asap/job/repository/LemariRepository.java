package project.asap.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.asap.job.entity.Lemari;

import java.util.Optional;

public interface LemariRepository extends JpaRepository<Lemari, Long> {
    Optional<Lemari> findByCode(String code);
}
