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
import java.util.Iterator;
import java.util.List;

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
    static String[] HEADERS = {"KODE_BATCH", "KODE_ISI_BATCH", "KETERANGAN", "TAHUN", "JUMLAH"};
    static String SHEET = "Kompilasi";

    public JobService(FileRefRepository fileRefRepository, GudangRepository gudangRepository, LemariRepository lemariRepository,
                      RakRepository rakRepository, BoxRepository boxRepository, ArsipRepository arsipRepository, AtksRepository atksRepository,
                      BmnsRepository bmnsRepository, PenyimpananMappingRepository penyimpananMappingRepository) {
        this.fileRefRepository = fileRefRepository;
        this.gudangRepository = gudangRepository;
        this.lemariRepository = lemariRepository;
        this.rakRepository = rakRepository;
        this.boxRepository = boxRepository;
        this.arsipRepository = arsipRepository;
        this.atksRepository = atksRepository;
        this.bmnsRepository = bmnsRepository;
        this.penyimpananMappingRepository = penyimpananMappingRepository;
    }


    //    @Scheduled(cron = "0 */5 * * * ?") // This will run every 5 minutes
    @Scheduled(cron = "0 * * * * ?") // This will run every minute
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
                Sheet sheet = workbook.getSheet(SHEET);
                Iterator<Row> rows = sheet.iterator();

                int rowNumber = 0;

                while (rows.hasNext()) {
                    Row currentRow = rows.next();

                    // skip header
                    if (rowNumber == 0) {
                        rowNumber++;
                        continue;
                    }

                    Iterator<Cell> cellsInRow = currentRow.iterator();

                    int cellIdx = 0;

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

                    if (kodeIsiBatch.equals("BERKAS")) {
                        arsip.setKodeLokasi(kodeBatch);
                        arsip.setTahun(tahun);
                        arsip.setNoDokumen(keterangan);
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
                    }

                    if (kodeIsiBatch.equals("ATK")) {
                        //atk
                    }

                    if (kodeIsiBatch.equals("BMN")) {
                        //bmn
                    }
                }

                workbook.close();

                fileRef.setFlagLoader(2);
                fileRefRepository.save(fileRef);
                logger.info("load file : " + fileRef.getFileName() + " success");


            } catch (Exception e) {
                logger.error("error reading excel file : " + fileRef.getFileName(), e);
            }

        }

    }
}
