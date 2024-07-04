package project.asap.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "penyimpanan_mapping")
@Data
public class PenyimpananMapping extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "id_bmn")
    private Long idBmn;
    @Column(name = "id_atk")
    private Long idAtk;
    @Column(name = "id_arsip")
    private Long idArsip;
    @Column(name = "id_gudang")
    private Long idGudang;
    @Column(name = "id_lemari")
    private Long idLemari;
    @Column(name = "id_rak")
    private Long idRak;
    @Column(name = "id_box")
    private Long idBox;
    @Column(name = "kode_batch")
    private String kodeBatch;
    @JsonIgnore
    @JoinColumn(name = "id_bmn", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne
    Bmns bmns;
    @JsonIgnore
    @JoinColumn(name = "id_atk", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne
    Atks atks;
    @JsonIgnore
    @JoinColumn(name = "id_arsip", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne
    Arsip arsip;
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
