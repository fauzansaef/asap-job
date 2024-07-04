package project.asap.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "rak")
@Data
public class Rak extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nama")
    private String nama;
    @Column(name = "code")
    private String code;
    @Column(name = "id_lemari")
    private Long idLemari;
    @ManyToOne
    @JoinColumn(name = "id_lemari", insertable = false, updatable = false)
    private Lemari lemari;
    @JsonIgnore
    @OneToMany(mappedBy = "rak", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Box> boxList;

}
