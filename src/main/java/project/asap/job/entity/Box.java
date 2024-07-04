package project.asap.job.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "box")
@Data
public class Box extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nama")
    private String nama;
    @Column(name = "code")
    private String code;
    @Column(name = "id_rak")
    private Long idRak;
    @ManyToOne
    @JoinColumn(name = "id_rak", insertable = false, updatable = false)
    private Rak rak;
}
