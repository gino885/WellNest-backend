package com.wellnest.user.service.impl;

import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.model.Chat;
import com.wellnest.user.dao.TherapyDao;
import com.wellnest.user.dao.UserDao;
import com.wellnest.user.dto.LocationDistance;
import com.wellnest.user.model.Appointment;
import com.wellnest.user.model.Location;
import com.wellnest.user.model.User;
import com.wellnest.user.service.TherapyService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TherapyServiceImpl implements TherapyService {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private TherapyDao therapyDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ChatDao chatDao;

    @Override
    public List<LocationDistance> getCoordinates(Location userLocation) throws Exception {
        double userLat = userLocation.getLatitude();
        double userLng = userLocation.getLongitude();

        List<Object[]> nearestLocations = therapyDao.findNearestLocations(userLat, userLng);
        List<LocationDistance> results = new ArrayList<>();

        for (Object[] row : nearestLocations) {
            String name = (row[0]).toString();
            String address = (row[1]).toString();
            String phone = (row[2]).toString();
            double distance = ((Number) row[3]).doubleValue();

            results.add(new LocationDistance(name, address, phone, String.format("%.2f km", distance)));
        }

        return results;
    }

    @Override
    public void generateReport(Integer userId) throws Exception {
        User userInfo = userDao.getUserById(userId);
        List<Chat> chats = chatDao.getChatsByDate(userId, 3);
        generatePDF(userInfo, chats);
    }

    private void generatePDF(User userInfo, List<Chat> chats) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("Counseling_Report.pdf"));
            document.open();

            URL fontUrl = TherapyServiceImpl.class.getClassLoader().getResource("fonts/msjh.ttf");
            BaseFont bfChinese = BaseFont.createFont(fontUrl.toURI().getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font subTitleFont = new Font(bfChinese, 14, Font.BOLD);
            Font contentFont = new Font(bfChinese, 12, Font.NORMAL); // 內容字型

            Paragraph title = new Paragraph("個別諮商基本資料", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph subTitle = new Paragraph("初談基本資料", subTitleFont);
            subTitle.setSpacingBefore(20);
            document.add(subTitle);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{2, 5, 1, 2});

            table.addCell(createCell("姓 名", Element.ALIGN_LEFT, false, subTitleFont));
            table.addCell(createCell(userInfo.getName(), Element.ALIGN_LEFT, true, contentFont));

            table.addCell(createCell("性 別", Element.ALIGN_LEFT, false, subTitleFont));
            PdfPCell maleBox = createCheckBox("男", contentFont);
            PdfPCell femaleBox = createCheckBox("女", contentFont);
            PdfPCell ageBox = createCell("年齡: " + userInfo.getAge(), Element.ALIGN_LEFT, false, contentFont);

            table.addCell(maleBox);
            table.addCell(femaleBox);
            table.addCell(ageBox);

            document.add(table);

            document.add(new Paragraph("＊過去使用諮商資源：", contentFont));
            document.add(createCheckboxList(new String[]{"無", "本校諮商中心", "過去就讀學校", "社區諮商輔導機構", "曾就診身心科或精神科"}, contentFont));

            document.add(new Paragraph("＊來談原因：", contentFont));
            document.add(new Paragraph("__________________________________________________________________________", contentFont));
            document.add(new Paragraph("＊來談主題：", contentFont));
            document.add(new Paragraph("__________________________________________________________________________", contentFont));
            document.add(new Paragraph("＊自我心情評估：", contentFont));


            document.close();
        } catch (Exception e) {
            System.out.println(e.getMessage() + e);
        }
        System.out.println("PDF created with OpenPDF including form elements!");

    }

    private static PdfPCell createCell(String content, int alignment, boolean border, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        if (!border) cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static PdfPCell createCheckBox(String label, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase("\u2610 " + label, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static PdfPTable createCheckboxList(String[] items, Font font) {
        PdfPTable checkboxTable = new PdfPTable(5);
        checkboxTable.setWidthPercentage(100);
        for (String item : items) {
            PdfPCell cell = new PdfPCell(new Phrase("\u2610 " + item, font));
            cell.setBorder(Rectangle.NO_BORDER);
            checkboxTable.addCell(cell);
        }
        return checkboxTable;
    }

    @Override
    public List<Appointment> getAppointmentDetail() throws Exception {
        List<Appointment> appointmentList = new ArrayList<>();
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://i.nccu.edu.tw/Login.aspx?ReturnUrl=%2fsso_app%2fNewMoltkeSSO.aspx%3fsid%3d45%26lan%3dzh-TW&sid=45&lan=zh-TW");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("captcha_Login1_UserName")));
            usernameField.sendKeys(System.getenv("USER_NAME"));

            WebElement passwordField = driver.findElement(By.id("captcha_Login1_Password"));
            passwordField.sendKeys(System.getenv("PASSWORD"));

            WebElement loginButton = driver.findElement(By.id("captcha_Login1_LoginButton"));
            loginButton.click();

            boolean pageLoaded = wait.until(ExpectedConditions.urlContains("psymregister_SSO/menu.jsp"));

            if (pageLoaded) {
                System.out.println("登入成功，頁面已跳轉至目標頁面！");
                WebElement appointmentButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@onclick='goReg(); return false;']")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", appointmentButton);

                wait.until(ExpectedConditions.urlContains("psymregister_SSO/registerTable.jsp"));
                System.out.println("已成功進入初步晤談預約頁面！");
                WebElement bodyElement = driver.findElement(By.tagName("body"));
                String content = bodyElement.getText();
                Pattern pattern = Pattern.compile("(\\d{2}/\\d{2})\\s*(非上班日|已額滿|可預約\\((\\d+)\\))");
                Matcher matcher = pattern.matcher(content);

                while (matcher.find()) {
                    String date = matcher.group(1);
                    String status = matcher.group(2);
                    Integer remainingSlots = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : null;
                    Appointment appointment = new Appointment(date, status.contains("可預約") ? "可預約" : status, remainingSlots);
                    appointmentList.add(appointment);
                }
            } else {
                System.out.println("登入失敗，未跳轉到目標頁面。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("發生錯誤：" + e.getMessage());
        } finally {
            driver.quit();
        }
        return appointmentList;
    }
}