package project.asap.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.asap.job.entity.Gudang;

import java.util.Optional;

public interface GudangRepository extends JpaRepository<Gudang, Long> {
    Optional<Gudang> findByCode(String code);
}
