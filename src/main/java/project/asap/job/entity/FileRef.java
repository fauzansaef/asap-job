package project.asap.job.entity;

import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_ref")
@SQLDelete(sql = "UPDATE file_ref SET deleted_at = current_timestamp WHERE id = ?")
@Where(clause = "deleted_at is null")
@Data
public class FileRef extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    @Column(name = "flag_loader")
    private Integer flagLoader;
}
