package project.asap.job.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "sub_sections")
@Data
public class SubSections extends project.asap.job.entity.AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "section_id")
    private Long sectionId;
    @Column(name = "kode")
    private String kode;
    @Column(name = "sub_section_name")
    private String subSectionName;
    @ManyToOne
    @JoinColumn(name = "section_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Sections sections;

}
