package project.asap.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import javax.persistence.*;

@Entity
@Table(name = "arsip")
@SQLDelete(sql = "UPDATE arsip SET deleted_at = current_timestamp WHERE id = ?")
@Where(clause = "deleted_at is null")
@Data
public class Arsip extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "id_tipe_arsip")
    private Long idTipeArsip;
    @Column(name = "kode")
    private String kode;
    @Column(name = "nama")
    private String nama;
    @Column(name = "tahun")
    private String tahun;
    @Column(name = "deskripsi")
    private String deskripsi;
    @Column(name = "jumlah_lembar")
    private Integer jumlahLembar;
    @Column(name = "file")
    private String file;
    @Column(name = "id_gudang")
    private Long idGudang;
    @Column(name = "id_lemari")
    private Long idLemari;
    @Column(name = "id_rak")
    private Long idRak;
    @Column(name = "id_box")
    private Long idBox;
    @Column(name = "kode_lokasi")
    private String kodeLokasi;
    @Column(name = "deleted_at")
    private String deletedAt;
    @Column(name = "status")
    private Integer status;
    @Column(name = "nip_petugas")
    private String nipPetugas;
    @JsonIgnore
    @JoinColumn(name = "id_tipe_arsip", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne
    private RefTipeArsip refTipeArsip;
    @JsonIgnore
    @JoinColumn(name = "id_gudang", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne
    private Gudang gudang;
    @JsonIgnore
    @JoinColumn(name = "id_lemari", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne
    private Lemari lemari;
    @JsonIgnore
    @JoinColumn(name = "id_rak", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne
    private Rak rak;
    @JsonIgnore
    @JoinColumn(name = "id_box", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne
    private Box box;


}
