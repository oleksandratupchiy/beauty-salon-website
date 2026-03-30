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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    // --- ЕКСПОРТ (8 колонок) ---
    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=report_salon.xlsx");

        String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Appointment> allApps = appointmentRepository.findAll();
        List<Appointment> filteredApps = allApps.stream()
                .filter(app -> app.getDate() != null && app.getDate().startsWith(currentMonthYear))
                .collect(Collectors.toList());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Звіт за " + currentMonthYear);
            XSSFRow header = sheet.createRow(0);
            String[] heads = {"Ім'я клієнта", "Email", "Телефон", "Послуга", "Тег послуги", "Майстер", "Дата", "Час"};
            for (int i = 0; i < heads.length; i++) header.createCell(i).setCellValue(heads[i]);

            int rowIdx = 1;
            for (Appointment app : filteredApps) {
                XSSFRow row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(app.getClient() != null ? app.getClient().getName() : "Гість");
                row.createCell(1).setCellValue(app.getClient() != null ? app.getClient().getEmail() : "");
                row.createCell(2).setCellValue(app.getClient() != null ? app.getClient().getPhone() : "");
                row.createCell(3).setCellValue(app.getService() != null ? app.getService().getTitle() : "Видалено");
                row.createCell(4).setCellValue(app.getService() != null ? app.getService().getCategory() : "");
                row.createCell(5).setCellValue(app.getMaster() != null ? app.getMaster().getName() : "Не призначено");
                row.createCell(6).setCellValue(app.getDate() != null ? app.getDate() : "");
                row.createCell(7).setCellValue(app.getTime() != null ? app.getTime() : "");
            }

            for (int i = 0; i < 8; i++) sheet.autoSizeColumn(i);
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    // --- ІМПОРТ З ПОВНОЮ ВАЛІДАЦІЄЮ ---
    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            return "redirect:/admin/port?error=empty";
        }

        int addedCount = 0;
        List<String> errorLog = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row == null) continue;

                int rowNum = i + 1;

                try {
                    String clientName = getCellValue(row.getCell(0));
                    String email = getCellValue(row.getCell(1));
                    String phone = getCellValue(row.getCell(2));
                    String serviceTitle = getCellValue(row.getCell(3));
                    String masterName = getCellValue(row.getCell(5));
                    String dateStr = getCellValue(row.getCell(6));
                    String timeStr = getCellValue(row.getCell(7));

                    // 1. ПЕРЕВІРКА НА ПУСТИЙ РЯДОК
                    if (clientName.isBlank() && serviceTitle.isBlank() && masterName.isBlank()) continue;

                    // 2. ВАЛІДАЦІЯ ІМЕНІ (захист від кринжових знаків та довжини)
                    if (!clientName.matches("^[a-zA-Zа-яА-ЯіІїЇєЄґҐ\\s-]+$") || clientName.length() < 2) {
                        errorLog.add("Рядок " + rowNum + ": Некоректне ім'я клієнта (лише літери, мін. 2 символи).");
                        continue;
                    }

                    // 3. ВАЛІДАЦІЯ ТЕЛЕФОНУ (380... та ніяких від'ємних чисел)
                    if (!phone.matches("^380\\d{9}$")) {
                        errorLog.add("Рядок " + rowNum + ": Невірний формат телефону (має бути 380XXXXXXXXX).");
                        continue;
                    }

                    // 4. ВАЛІДАЦІЯ ПОШТИ
                    if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                        errorLog.add("Рядок " + rowNum + ": Некоректний формат Email.");
                        continue;
                    }

                    // 5. ПЕРЕВІРКА ОБОВ'ЯЗКОВИХ ПОЛІВ БАЗИ
                    if (serviceTitle.isEmpty() || masterName.isEmpty() || dateStr.isEmpty() || timeStr.isEmpty()) {
                        errorLog.add("Рядок " + rowNum + ": Відсутня послуга, майстер або дата/час.");
                        continue;
                    }

                    // 6. ПЕРЕВІРКА ІСНУВАННЯ ПОСЛУГИ ТА МАЙСТРА
                    Service service = serviceRepository.findByTitle(serviceTitle).orElse(null);
                    Master master = masterRepository.findByName(masterName).orElse(null);

                    if (service == null || master == null) {
                        String msg = (service == null) ? "Послугу '" + serviceTitle + "'" : "Майстра '" + masterName + "'";
                        errorLog.add("Рядок " + rowNum + ": " + msg + " не знайдено в базі.");
                        continue;
                    }

                    // 7. КЛІЄНТ (Пошук або створення)
                    Client client = null;
                    if (!email.isEmpty()) client = clientRepository.findByEmail(email).orElse(null);

                    if (client == null) {
                        client = new Client();
                        client.setName(clientName);
                        client.setEmail(email.isEmpty() ? "guest-" + UUID.randomUUID().toString().substring(0,8) + "@salon.com" : email);
                        client.setPhone(phone);
                        client.setPassword(passwordEncoder.encode("12345"));
                        client.setRole("ROLE_USER");
                        client = clientRepository.save(client);
                    }

                    // 8. РОЗУМНИЙ ФІЛЬТР ДУБЛІКАТІВ (дозволяє накладки різних клієнтів, але блокує повні копії)
                    if (appointmentRepository.existsByClientIdAndMasterIdAndServiceIdAndDateAndTime(
                            client.getId(), master.getId(), service.getId(), dateStr, timeStr)) {
                        continue; // Мовчки ігноруємо точну копію запису
                    }

                    // 9. ЗБЕРЕЖЕННЯ ЗАПИСУ
                    Appointment app = new Appointment();
                    app.setClient(client);
                    app.setService(service);
                    app.setMaster(master);
                    app.setDate(dateStr);
                    app.setTime(timeStr);
                    appointmentRepository.save(app);
                    addedCount++;

                } catch (Exception e) {
                    errorLog.add("Рядок " + rowNum + ": Помилка читання даних (перевірте формат комірок).");
                }
            }

            redirectAttributes.addFlashAttribute("successCount", addedCount);
            if (!errorLog.isEmpty()) redirectAttributes.addFlashAttribute("importErrors", errorLog);
            return "redirect:/admin/port";

        } catch (Exception e) {
            return "redirect:/admin/port?error=parse";
        }
    }

    // БЕЗПЕЧНЕ ЧИТАННЯ КОМІРОК (з trim)
    private String getCellValue(XSSFCell cell) {
        if (cell == null) return "";
        String value = "";
        switch (cell.getCellType()) {
            case STRING: value = cell.getStringCellValue(); break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDateTime ldt = cell.getLocalDateTimeCellValue();
                    value = (ldt.getYear() < 1920)
                            ? ldt.format(DateTimeFormatter.ofPattern("HH:mm"))
                            : ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else {
                    value = String.format("%.0f", cell.getNumericCellValue());
                }
                break;
            default: value = "";
        }
        return value.trim();
    }
}