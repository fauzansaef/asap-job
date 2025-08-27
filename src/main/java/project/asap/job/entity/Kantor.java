package project.asap.job.entity;

import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "kantor")
@Data
public class Kantor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode_kantor", unique = true, nullable = false)
    private String kodeKantor;

    @Column(name = "nama_kantor")
    private String namaKantor;

    @Column(name = "alamat_kantor")
    private String alamat;

    // Tambahkan field lain sesuai kebutuhan, misal: jenis kantor, dsb
}
