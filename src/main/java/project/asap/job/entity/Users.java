package project.asap.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = current_timestamp WHERE id = ?")
@Where(clause = "deleted_at is null")
@Data
public class Users extends project.asap.job.entity.AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "kode_kantor")
    private String kodeKantor;
    @Column(name = "section_id")
    private Long sectionId;
    @Column(name = "sub_section_id")
    private Long subSectionId;
    @Column(name = "name")
    private String name;
    @Column(name = "ip")
    private String ip;
    @Column(name = "email")
    private String email;
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;
    @Column(name = "password")
    private String password;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "role")
    private Integer role;
    @Column(name = "device_token")
    private String deviceToken;
    @Column(name = "photo")
    private String photo;
    @Column(name = "remember_token")
    private String rememberToken;
    @Column(name = "unit_kerja")
    private String unitKerja;
    @Column(name = "jabatan")
    private String jabatan;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "section_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Sections sections;
    @ManyToOne
    @JoinColumn(name = "sub_section_id", referencedColumnName = "id", insertable = false, updatable = false)
    private SubSections subSections;


}
