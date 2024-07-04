package project.asap.job.entity;

import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bmns")
@SQLDelete(sql = "UPDATE bmns SET deleted_at = current_timestamp WHERE id = ?")
@Where(clause = "deleted_at is null")
@Data
public class Bmns extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "kode")
    private String kode;
    @Column(name = "nama_bmn")
    private String namaBmn;
    @Column(name = "deskripsi")
    private String deskripsi;
    @Column(name = "tahun")
    private String tahun;
    @Column(name = "photo")
    private String photo;
    @Column(name = "status")
    private String status;
    @Column(name = "nama_penanggung_jawab")
    private String namaPenanggungJawab;
    @Column(name = "tahun_pengadaan")
    private String tahunPengadaan;
    @Column(name = "stock")
    private Integer stock;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
