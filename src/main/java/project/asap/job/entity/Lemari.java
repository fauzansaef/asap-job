package project.asap.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "lemari")
@Data
public class Lemari extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nama")
    private String nama;
    @Column(name = "code")
    private String code;
    @Column(name = "id_gudang")
    private Long idGudang;
    @Column(name = "pic")
    private Long pic;
    @ManyToOne
    @JoinColumn(name = "id_gudang", insertable = false, updatable = false)
    private Gudang gudang;

    @JsonIgnore
    @OneToMany(mappedBy = "lemari", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Rak> rakList;
}
