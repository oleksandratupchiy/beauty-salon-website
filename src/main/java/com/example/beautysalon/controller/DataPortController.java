package com.example.beautysalon.controller;

import com.example.beautysalon.entity.*;
import com.example.beautysalon.repository.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/port")
public class DataPortController {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private MasterRepository masterRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public String showPortPage() {
        return "admin/import";
    }

    // --- ЕКСПОРТ (Тільки поточний місяць) ---
    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=report_current_month.xlsx");

        String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<Appointment> allApps = appointmentRepository.findAll();
        List<Appointment> filteredApps = allApps.stream()
                .filter(app -> app.getDate() != null && app.getDate().startsWith(currentMonthYear))
                .collect(Collectors.toList());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Звіт за " + currentMonthYear);

            XSSFRow header = sheet.createRow(0);
            String[] heads = {"ID", "Клієнт", "Email", "Майстер", "Послуга", "Дата", "Час"};
            for (int i = 0; i < heads.length; i++) header.createCell(i).setCellValue(heads[i]);

            int rowIdx = 1;
            for (Appointment app : filteredApps) {
                XSSFRow row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(app.getId() != null ? app.getId().toString() : "");
                row.createCell(1).setCellValue(app.getClient() != null ? app.getClient().getName() : "Гість");
                row.createCell(2).setCellValue(app.getClient() != null ? app.getClient().getEmail() : "-");
                row.createCell(3).setCellValue(app.getMaster() != null ? app.getMaster().getName() : "Не призначено");
                row.createCell(4).setCellValue(app.getService() != null ? app.getService().getTitle() : "Видалено");
                row.createCell(5).setCellValue(app.getDate() != null ? app.getDate() : "");
                row.createCell(6).setCellValue(app.getTime() != null ? app.getTime() : "");
            }

            for (int i = 0; i < 7; i++) sheet.autoSizeColumn(i);
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- ІМПОРТ ---
    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            return "redirect:/admin/port?error=empty";
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row == null || row.getCell(1) == null) continue;

                String clientName = getCellValue(row.getCell(1));
                String email = getCellValue(row.getCell(2));
                String masterName = getCellValue(row.getCell(3));
                String serviceTitle = getCellValue(row.getCell(4));
                String dateStr = getCellValue(row.getCell(5));
                String timeStr = getCellValue(row.getCell(6));

                Service service = serviceRepository.findByTitle(serviceTitle)
                        .orElseGet(() -> serviceRepository.save(new Service(serviceTitle, 500.0, 60, "Загальне")));

                Client client = clientRepository.findByEmail(email).orElseGet(() -> {
                    Client c = new Client();
                    c.setName(clientName);
                    c.setEmail(email);
                    c.setPassword(passwordEncoder.encode("12345"));
                    c.setRole("ROLE_USER");
                    return clientRepository.save(c);
                });

                Master master = masterRepository.findByName(masterName)
                        .orElseGet(() -> masterRepository.save(new Master(masterName)));

                Appointment app = new Appointment();
                app.setClient(client);
                app.setService(service);
                app.setMaster(master);
                app.setDate(dateStr);
                app.setTime(timeStr);
                appointmentRepository.save(app);
            }
            return "redirect:/admin/dashboard?importSuccess";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/port?error=parse";
        }
    }

    // ВИПРАВЛЕНО: Більше ніякого 1899 року!
    private String getCellValue(XSSFCell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDateTime ldt = cell.getLocalDateTimeCellValue();

                    // Якщо рік < 1920, Excel вважає це просто часом
                    if (ldt.getYear() < 1920) {
                        return ldt.format(DateTimeFormatter.ofPattern("HH:mm"));
                    } else {
                        // Якщо нормальний рік - це дата
                        return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                }
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return cell.toString().trim();
        }
    }
}