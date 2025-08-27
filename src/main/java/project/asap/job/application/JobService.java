package project.asap.job.application;

import ch.qos.logback.classic.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.asap.job.entity.*;
import project.asap.job.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Component
public class JobService {
    @Value("${file.directory}")
    private String fileDirectory;
    private static final Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(JobService.class);
    private final FileRefRepository fileRefRepository;
    private final GudangRepository gudangRepository;
    private final LemariRepository lemariRepository;
    private final RakRepository rakRepository;
    private final BoxRepository boxRepository;
    private final ArsipRepository arsipRepository;
    private final AtksRepository atksRepository;
    private final BmnsRepository bmnsRepository;
    private final PenyimpananMappingRepository penyimpananMappingRepository;
    private final FailedJobsExcelRepository failedJobsExcelRepository;
    private final UsersRepository userRepository;

    public JobService(FileRefRepository fileRefRepository, GudangRepository gudangRepository, LemariRepository lemariRepository,
                      RakRepository rakRepository, BoxRepository boxRepository, ArsipRepository arsipRepository, AtksRepository atksRepository,
                      BmnsRepository bmnsRepository, PenyimpananMappingRepository penyimpananMappingRepository, FailedJobsExcelRepository failedJobsExcelRepository,  UsersRepository userRepository) {
        this.fileRefRepository = fileRefRepository;
        this.gudangRepository = gudangRepository;
        this.lemariRepository = lemariRepository;
        this.rakRepository = rakRepository;
        this.boxRepository = boxRepository;
        this.arsipRepository = arsipRepository;
        this.atksRepository = atksRepository;
        this.bmnsRepository = bmnsRepository;
        this.penyimpananMappingRepository = penyimpananMappingRepository;
        this.failedJobsExcelRepository = failedJobsExcelRepository;
        this.userRepository = userRepository;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Scheduled(cron = "${trigger.job.cron}")
    @Async
    // @Transactional
    public void runJob() {

        List<FileRef> fileRefs = fileRefRepository.findByFlagLoader(0);

        if (fileRefs.isEmpty()) {
            logger.info("No file to process");
        }

        for (FileRef fileRef : fileRefs) {
            logger.info("Processing file: " + fileRef.getFileName());
            fileRef.setFlagLoader(1);
            fileRefRepository.save(fileRef);
            logger.info("load file : " + fileRef.getFileName() + " processing");


            try {
                Path path = Paths.get(fileDirectory + fileRef.getFileName());
                InputStream inputStream = Files.newInputStream(path);
                Workbook workbook = new XSSFWorkbook(inputStream);

                Sheet sheet = null;
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    String sheetName = workbook.getSheetName(i);
                    if ("Kompilasi".equals(sheetName) || "User".equals(sheetName)) {
                        sheet = workbook.getSheet(sheetName);
                        break;
                    }
                }

                if (sheet == null) {
                    throw new IllegalArgumentException("Sheet with name 'Kompilasi' or 'User' not found");
                }

                Iterator<Row> rows = sheet.iterator();

                int rowNumber = 0;
                int totalRowBerhasil = 0;
                int totalRowGagal = 0;
                System.out.println(sheet.getSheetName());
                if ("Kompilasi".equals(sheet.getSheetName())) {

                    System.out.println("MASUK KOMPILASI");
                    while (rows.hasNext()) {
                        Gudang gudang = new Gudang();
                        Lemari lemari = new Lemari();
                        Rak rak = new Rak();
                        Box box = new Box();
                        Arsip arsip = new Arsip();
                        Atks atks = new Atks();
                        Bmns bmns = new Bmns();
                        PenyimpananMapping penyimpananMapping = new PenyimpananMapping();
                        String kodeBatch = "";
                        String kodeIsiBatch = "";
                        String keterangan = "";
                        String tahun = "";
                        String kodeKantor = "";
                        int jumlah = 0;

                        Row currentRow = rows.next();

                        /** skip header **/
                        if (rowNumber == 0) {
                            rowNumber++;
                            continue;
                        }

                        rowNumber++;

                        /**
                         * cek jika cell pertama kosong, break the loop
                         * jika cell pertama kosong, proses looping load selesai, pastikan file excel tidak ada row kosong
                         **/
                        Cell firstCell = currentRow.getCell(0);
                        if (firstCell == null || firstCell.getCellType() == CellType.BLANK) {

                            break;
                        }

                        Iterator<Cell> cellsInRow = currentRow.iterator();

                        int cellIdx = 0;

                        try {
                            while (cellsInRow.hasNext()) {

                                Cell currentCell = cellsInRow.next();
                                switch (cellIdx) {
                                    case 0:
                                        if (currentCell.getCellType() != CellType.STRING) {
                                            saveFailedJobsExcel(kodeBatch, keterangan, "Kode Batch harus berupa string", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                            logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error");
                                        }

                                        try {
                                            kodeBatch = currentCell.getStringCellValue();
                                            String[] parts = kodeBatch.split("\\.");
                                            String codeGudang = parts[0];
                                            String codeLemari = parts[1];
                                            String codeRak = parts[2];
                                            String codeBox = parts[3];

                                            if (!gudangRepository.findByCode(codeGudang).isPresent()) {
                                                gudang.setCode(codeGudang);
                                                gudang.setNama(codeGudang);

                                                gudangRepository.save(gudang);
                                            } else {
                                                gudang = gudangRepository.findByCode(codeGudang).get();
                                            }

                                            if (!lemariRepository.findByCode(codeLemari).isPresent()) {
                                                lemari.setCode(codeLemari);
                                                lemari.setNama(codeLemari);
                                                lemari.setIdGudang(gudang.getId());
                                                lemariRepository.save(lemari);
                                            } else {
                                                lemari = lemariRepository.findByCode(codeLemari).get();
                                            }

                                            if (!rakRepository.findByCode(codeRak).isPresent()) {
                                                rak.setCode(codeRak);
                                                rak.setIdLemari(lemari.getId());
                                                rak.setNama(codeRak);
                                                rakRepository.save(rak);
                                            } else {
                                                rak = rakRepository.findByCode(codeRak).get();
                                            }

                                            if (!boxRepository.findByCode(codeBox).isPresent()) {
                                                box.setCode(codeBox);
                                                box.setIdRak(rak.getId());
                                                box.setNama(codeBox);
                                                boxRepository.save(box);
                                            } else {
                                                box = boxRepository.findByCode(codeBox).get();
                                            }
                                        } catch (Exception e) {
                                            saveFailedJobsExcel(kodeBatch, keterangan, e.getMessage(), LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                            logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error : " + e.getMessage());
                                        }

                                        break;

                                    case 1:
                                        if (currentCell.getCellType() == CellType.STRING) {
                                            kodeIsiBatch = currentCell.getStringCellValue();
                                        } else {
                                            saveFailedJobsExcel(kodeBatch, keterangan, "Kode Isi Batch harus berupa string", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                            logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error");
                                        }

                                        break;

                                    case 2:
                                        if (currentCell.getCellType() == CellType.STRING) {
                                            keterangan = currentCell.getStringCellValue();
                                        } else {
                                            saveFailedJobsExcel(kodeBatch, keterangan, "Keterangan harus berupa string", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                            logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error");
                                        }

                                        break;

                                    case 3:
                                        if (currentCell.getCellType() == CellType.STRING) {
                                            tahun = String.valueOf(currentCell.getStringCellValue());
                                        } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                            tahun = String.valueOf((int) currentCell.getNumericCellValue());
                                        } else {
                                            saveFailedJobsExcel(kodeBatch, keterangan, "Tahun bukan berupa angka", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                            logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error");
                                        }
                                        break;

                                    case 4:
                                        if (currentCell.getCellType() == CellType.NUMERIC) {
                                            jumlah = (int) currentCell.getNumericCellValue();
                                        } else {
                                            saveFailedJobsExcel(kodeBatch, keterangan, "Jumlah bukan berupa angka", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                            logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error");
                                        }
                                        break;
                                    case 5:
                                        if (currentCell.getCellType() == CellType.STRING) {
                                            kodeKantor = currentCell.getStringCellValue();
                                        } else {
                                            saveFailedJobsExcel(kodeBatch, keterangan, "Kode Kantor harus berupa string", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                            logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error");
                                        }
                                        break;

                                    default:
                                        break;
                                }
                                cellIdx++;
                            }

                            LocalDate now = LocalDate.now();
                            String kode = "";
                            Random random = new Random();
                            int randomNumber = random.nextInt(9000) + 1000;
                            switch (kodeIsiBatch) {
                                case "BERKAS":
                                    kode = "ARSIP-" + now.getYear() + now.getMonthValue() + now.getDayOfMonth() + "-" + randomNumber;
                                    arsip.setKodeLokasi(kodeBatch);
                                    arsip.setTahun(tahun);
                                    arsip.setNama(keterangan);
                                    arsip.setJumlahLembar(jumlah);
                                    arsip.setNipPetugas(fileRef.getNipPetugas());
                                    arsip.setStatus(1); //disimpan
                                    arsip.setKode(kode);
                                    arsip.setKodeKantor(kodeKantor);
                                    arsipRepository.save(arsip);

                                    penyimpananMapping.setIdArsip(arsip.getId());
                                    penyimpananMapping.setIdGudang(gudang.getId());
                                    penyimpananMapping.setIdLemari(lemari.getId());
                                    penyimpananMapping.setIdRak(rak.getId());
                                    penyimpananMapping.setIdBox(box.getId());
                                    penyimpananMapping.setKodeBatch(kodeBatch);
                                    penyimpananMapping.setKodeKantor(kodeKantor);
                                    penyimpananMappingRepository.save(penyimpananMapping);
                                    totalRowBerhasil++;
                                    logger.info("load file : " + fileRef.getFileName() + " row : " + rowNumber + " success");
                                    break;
                                case "ATK":
                                    kode = "ATK-" + now.getYear() + now.getMonthValue() + now.getDayOfMonth() + "-" + randomNumber;
                                    atks.setNamaAtk(keterangan);
                                    atks.setTahun(tahun);
                                    atks.setStock(jumlah);
                                    atks.setKodeLokasi(kodeBatch);
                                    atks.setHarga("0");
                                    atks.setKode(kode);
                                    atks.setKodeKantor(kodeKantor);
                                    atksRepository.save(atks);

                                    penyimpananMapping.setIdAtk(atks.getId());
                                    penyimpananMapping.setIdGudang(gudang.getId());
                                    penyimpananMapping.setIdLemari(lemari.getId());
                                    penyimpananMapping.setIdRak(rak.getId());
                                    penyimpananMapping.setIdBox(box.getId());
                                    penyimpananMapping.setKodeBatch(kodeBatch);
                                    penyimpananMapping.setKodeKantor(kodeKantor);
                                    penyimpananMappingRepository.save(penyimpananMapping);
                                    totalRowBerhasil++;
                                    logger.info("load file : " + fileRef.getFileName() + " row : " + rowNumber + " success");
                                    break;
                                case "BMN":
                                    kode = "BMN-" + now.getYear() + now.getMonthValue() + now.getDayOfMonth() + "-" + randomNumber;
                                    bmns.setNamaBmn(keterangan);
                                    bmns.setTahun(tahun);
                                    bmns.setStock(jumlah);
                                    bmns.setKodeLokasi(kodeBatch);
                                    bmns.setKode(kode);
                                    bmns.setDeskripsi("-");
                                    bmns.setKodeKantor(kodeKantor);
                                    bmnsRepository.save(bmns);

                                    penyimpananMapping.setIdBmn(bmns.getId());
                                    penyimpananMapping.setIdGudang(gudang.getId());
                                    penyimpananMapping.setIdLemari(lemari.getId());
                                    penyimpananMapping.setIdRak(rak.getId());
                                    penyimpananMapping.setIdBox(box.getId());
                                    penyimpananMapping.setKodeBatch(kodeBatch);
                                    penyimpananMapping.setKodeKantor(kodeKantor);
                                    penyimpananMappingRepository.save(penyimpananMapping);
                                    totalRowBerhasil++;
                                    logger.info("load file : " + fileRef.getFileName() + " row : " + rowNumber + " success");
                                    break;
                                default:
                                    saveFailedJobsExcel(kodeBatch, keterangan, "Kode Isi Batch tidak dikenali", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                    totalRowGagal++;
                                    logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error");
                                    break;
                            }


                        } catch (Exception e) {
                            saveFailedJobsExcel(kodeBatch, keterangan, e.getMessage(), LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                            totalRowGagal++;
                            logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error", e);

                        }


                    }

                }else if ("User".equals(sheet.getSheetName())) {
                    System.out.println("MASUK USER");
                    while (rows.hasNext()) {
                        Row currentRow = rows.next();

                        // Skip header
                        if (rowNumber == 0) {
                            rowNumber++;
                            continue;
                        }

                        rowNumber++;

                        String kodeKantor = "";
                        Cell firstCell = currentRow.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (firstCell == null) {
                            break; // stop kalau baris kosong
                        }

                        Users user = new Users();
                        DataFormatter formatter = new DataFormatter(); // untuk format cell jadi String

                        try {
                            short lastCellNum = currentRow.getLastCellNum();

                            for (int cellIdx = 0; cellIdx < lastCellNum; cellIdx++) {
                                Cell currentCell = currentRow.getCell(cellIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                                String cellValue = (currentCell == null) ? "" : formatter.formatCellValue(currentCell).trim();

                                switch (cellIdx) {
                                    case 0: // NAMA
                                        if (!cellValue.isEmpty()) {
                                            user.setName(cellValue);
                                        } else {
                                            saveFailedJobsExcel("", "", "nama tidak boleh kosong", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                            logger.error("Row " + rowNumber + " error: nama kosong");
                                        }
                                        break;

                                    case 1: // NIP
                                        if (!cellValue.isEmpty()) {
                                            user.setIp(cellValue);
                                        } else {
                                            saveFailedJobsExcel("", "", "nip tidak boleh kosong", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                        }
                                        break;

                                    case 2: // EMAIL
                                        if (cellValue.isEmpty()) {
                                            user.setEmail(user.getIp() + "@pajak.go.id"); // auto generate
                                        } else {
                                            user.setEmail(cellValue);
                                        }
                                        break;

                                    case 3: // ROLE
                                        if (!cellValue.isEmpty()) {
                                            user.setRole(Integer.valueOf(cellValue));
                                        } else {
                                            saveFailedJobsExcel("", "", "role kosong", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                        }
                                        break;

                                    case 4: // UNITKERJA
                                        user.setUnitKerja(cellValue); // boleh kosong
                                        break;

                                    case 5: // JABATAN
                                        user.setJabatan(cellValue); // boleh kosong
                                        break;

                                    case 6: // SECTIONID
                                        if (cellValue.isEmpty()) {
                                            user.setSectionId(1L);
                                        } else {
                                            user.setSectionId(Long.valueOf(cellValue));
                                        }
                                        break;

                                    case 7: // SUBSECTIONID
                                        if (cellValue.isEmpty()) {
                                            user.setSubSectionId(1L);
                                        } else {
                                            user.setSubSectionId(Long.valueOf(cellValue));
                                        }
                                        break;

                                    case 8: // KODEKANTOR
                                        if (cellValue.isEmpty()) {
                                            saveFailedJobsExcel("", "", "Kode Kantor tidak boleh kosong", LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                                            totalRowGagal++;
                                        } else {
                                            kodeKantor = cellValue;
                                        }
                                        break;

                                    default:
                                        break;
                                }
                            }

                            user.setPassword(passwordEncoder.encode(user.getIp()));
                            user.setKodeKantor(kodeKantor);
                            userRepository.save(user);
                            totalRowBerhasil++;
                            logger.info("load file : " + fileRef.getFileName() + " row : " + rowNumber + " success");

                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            saveFailedJobsExcel("userInput", "Error", e.getMessage(), LocalDateTime.now(), fileRef.getFileName(), rowNumber, fileRef.getId(), fileRef.getKodeKantor());
                            totalRowGagal++;
                            logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error", e);
                        }
                    }
                }


                workbook.close();

                fileRef.setFlagLoader(2);
                fileRef.setTotalRowBerhasil(totalRowBerhasil);
                fileRef.setTotalRowGagal(totalRowGagal);
                fileRefRepository.save(fileRef);
                logger.info("load file : " + fileRef.getFileName() + " finished");

            } catch (Exception e) {
                fileRef.setFlagLoader(3);
                fileRefRepository.save(fileRef);
                logger.error("error reading excel file : " + fileRef.getFileName(), e);
            }

        }

    }

    private void saveFailedJobsExcel(String kodeBatch, String keterangan, String errorMessage, LocalDateTime failedAt, String fileName, int rowNumber, Long idFileRef, String kodeKantor) {
        FailedJobsExcel failedJobsExcel = new FailedJobsExcel();
        failedJobsExcel.setKodeBatch(kodeBatch);
        failedJobsExcel.setKeterangan(keterangan);
        failedJobsExcel.setErrorMessage(errorMessage);
        failedJobsExcel.setFailedAt(failedAt);
        failedJobsExcel.setFileName(fileName);
        failedJobsExcel.setRow(rowNumber);
        failedJobsExcel.setIdFileRef(idFileRef);
        failedJobsExcel.setKodeKantor(kodeKantor);
        failedJobsExcelRepository.save(failedJobsExcel);
    }


}
