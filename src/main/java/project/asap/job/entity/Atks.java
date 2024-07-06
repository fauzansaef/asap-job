package project.asap.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "atks")
@Data
public class Atks extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "kode")
    private String kode;
    @Column(name = "nama_atk")
    private String namaAtk;
    @Column(name = "deskripsi")
    private String deskripsi;
    @Column(name = "photo")
    private String photo;
    @Column(name = "stock")
    private Integer stock;
    @Column(name = "harga")
    private String harga;
    @Column(name = "kode_lokasi")
    private String kodeLokasi;
    @Column(name = "tahun")
    private String tahun;
    @JsonIgnore
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
