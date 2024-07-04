package project.asap.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "gudang")
@Data
public class Gudang extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nama")
    private String nama;
    @Column(name = "code")
    private String code;
    @JsonIgnore
    @OneToMany(mappedBy = "gudang", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Lemari> lemariList;
}
