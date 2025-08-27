package project.asap.job.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "sections")
@Data
public class Sections extends project.asap.job.entity.AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "kode_kantor")
    private String kodeKantor;
    @Column(name = "kode")
    private String kode;
    @Column(name = "section_name")
    private String sectionName;
}
