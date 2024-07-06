package project.asap.job.application;

import ch.qos.logback.classic.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.asap.job.entity.*;
import project.asap.job.repository.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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

    public JobService(FileRefRepository fileRefRepository, GudangRepository gudangRepository, LemariRepository lemariRepository,
                      RakRepository rakRepository, BoxRepository boxRepository, ArsipRepository arsipRepository, AtksRepository atksRepository,
                      BmnsRepository bmnsRepository, PenyimpananMappingRepository penyimpananMappingRepository, FailedJobsExcelRepository failedJobsExcelRepository) {
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
    }


    @Scheduled(cron = "0 */5 * * * ?") // This will run every 5 minutes
    @Async
    @Transactional
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
                Sheet sheet = workbook.getSheet("Kompilasi");
                Iterator<Row> rows = sheet.iterator();

                int rowNumber = 0;

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

                                    break;

                                case 1:
                                    kodeIsiBatch = currentCell.getStringCellValue();
                                    break;

                                case 2:
                                    keterangan = currentCell.getStringCellValue();
                                    break;

                                case 3:

                                    if (currentCell.getCellType() == CellType.STRING) {
                                        tahun = String.valueOf(currentCell.getStringCellValue());
                                    } else if (currentCell.getCellType() == CellType.NUMERIC) {
                                        tahun = String.valueOf((int) currentCell.getNumericCellValue());
                                    }
                                    break;

                                case 4:
                                    jumlah = (int) currentCell.getNumericCellValue();
                                    break;

                                default:
                                    break;
                            }
                            cellIdx++;
                        }

                        LocalDate now = LocalDate.now();
                        String kode = "";
                        switch (kodeIsiBatch) {
                            case "BERKAS":
                                arsip.setKodeLokasi(kodeBatch);
                                arsip.setTahun(tahun);
                                arsip.setNama(keterangan);
                                arsip.setJumlahLembar(jumlah);
                                arsip.setNipPetugas(fileRef.getNipPetugas());
                                arsipRepository.save(arsip);

                                penyimpananMapping.setIdArsip(arsip.getId());
                                penyimpananMapping.setIdGudang(gudang.getId());
                                penyimpananMapping.setIdLemari(lemari.getId());
                                penyimpananMapping.setIdRak(rak.getId());
                                penyimpananMapping.setIdBox(box.getId());
                                penyimpananMapping.setKodeBatch(kodeBatch);
                                penyimpananMappingRepository.save(penyimpananMapping);
                                logger.info("load file : " + fileRef.getFileName() + " row : " + rowNumber + " success");
                                break;
                            case "ATK":
                                kode = "ATK-" + now.getYear() + now.getMonthValue() + now.getDayOfMonth() + "-" + UUID.randomUUID();
                                atks.setNamaAtk(keterangan);
                                atks.setTahun(tahun);
                                atks.setStock(jumlah);
                                atks.setKodeLokasi(kodeBatch);
                                atks.setHarga("0");
                                atks.setKode(kode);
                                atksRepository.save(atks);

                                penyimpananMapping.setIdAtk(atks.getId());
                                penyimpananMapping.setIdGudang(gudang.getId());
                                penyimpananMapping.setIdLemari(lemari.getId());
                                penyimpananMapping.setIdRak(rak.getId());
                                penyimpananMapping.setIdBox(box.getId());
                                penyimpananMapping.setKodeBatch(kodeBatch);
                                penyimpananMappingRepository.save(penyimpananMapping);
                                logger.info("load file : " + fileRef.getFileName() + " row : " + rowNumber + " success");
                                break;
                            case "BMN":
                                kode = "BMN-" + now.getYear() + now.getMonthValue() + now.getDayOfMonth() + "-" + UUID.randomUUID();
                                bmns.setNamaBmn(keterangan);
                                bmns.setTahun(tahun);
                                bmns.setStock(jumlah);
                                bmns.setKodeLokasi(kodeBatch);
                                bmns.setKode(kode);
                                bmns.setDeskripsi("-");
                                bmnsRepository.save(bmns);

                                penyimpananMapping.setIdBmn(bmns.getId());
                                penyimpananMapping.setIdGudang(gudang.getId());
                                penyimpananMapping.setIdLemari(lemari.getId());
                                penyimpananMapping.setIdRak(rak.getId());
                                penyimpananMapping.setIdBox(box.getId());
                                penyimpananMapping.setKodeBatch(kodeBatch);
                                penyimpananMappingRepository.save(penyimpananMapping);
                                logger.info("load file : " + fileRef.getFileName() + " row : " + rowNumber + " success");
                                break;
                            default:
                                FailedJobsExcel failedJobsExcel = new FailedJobsExcel();
                                failedJobsExcel.setKodeBatch(kodeBatch);
                                failedJobsExcel.setKeterangan(keterangan);
                                failedJobsExcel.setErrorMessage("Kode Isi Batch tidak dikenali");
                                failedJobsExcel.setFailedAt(LocalDateTime.now());
                                failedJobsExcel.setFileName(fileRef.getFileName());
                                failedJobsExcel.setRow(rowNumber);
                                failedJobsExcelRepository.save(failedJobsExcel);
                                logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error");
                                break;
                        }


                    } catch (Exception e) {
                        logger.error("load file : " + fileRef.getFileName() + " row : " + rowNumber + " error", e);
                        FailedJobsExcel failedJobsExcel = new FailedJobsExcel();
                        failedJobsExcel.setKodeBatch(kodeBatch);
                        failedJobsExcel.setKeterangan(keterangan);
                        failedJobsExcel.setErrorMessage(e.getMessage());
                        failedJobsExcel.setFailedAt(LocalDateTime.now());
                        failedJobsExcel.setFileName(fileRef.getFileName());
                        failedJobsExcel.setRow(rowNumber);
                        failedJobsExcelRepository.save(failedJobsExcel);

                    }


                }

                workbook.close();

                fileRef.setFlagLoader(2);
                fileRefRepository.save(fileRef);
                logger.info("load file : " + fileRef.getFileName() + " finished");

            } catch (Exception e) {
                logger.error("error reading excel file : " + fileRef.getFileName(), e);
            }

        }

    }
}
