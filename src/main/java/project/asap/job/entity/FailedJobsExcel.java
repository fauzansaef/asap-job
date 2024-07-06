package project.asap.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "failed_jobs_excel")
@Data
public class FailedJobsExcel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "kode_batch")
    private String kodeBatch;
    @Column(name = "keterangan")
    private String keterangan;
    @Column(name = "error_message")
    private String errorMessage;
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "row")
    private Integer row;
    @Column(name = "id_file_ref")
    private Long idFileRef;
    @ManyToOne
    @JoinColumn(name = "id_file_ref", insertable = false, updatable = false)
    @JsonIgnore
    private FileRef fileRef;
}
