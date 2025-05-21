package com.framework.tests;

import com.framework.base.BaseTest;
import com.framework.pages.NSEStockPage;
import com.framework.utils.ScreenshotUtil;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

public class NSEStockComparisonTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(NSEStockComparisonTest.class);
    private ExtentReports ExtReports;
    private ExtentTest ExtTest;
    private Properties objProp;
    private String browserName;

    public static class StockData {
        public String symbol;
        public double excelHigh;
        public double excelLow;

        public StockData(String symbol, double excelHigh, double excelLow) {
            this.symbol = symbol;
            this.excelHigh = excelHigh;
            this.excelLow = excelLow;
        }
    }

    private List<StockData> stockDataList;
    private List<Double> profitLossPercentageList = new ArrayList<>();

    @BeforeClass
    @Parameters({"browser"})
    public void setUp(@Optional("chrome") String browser) {
        this.browserName = browser.toLowerCase();
        logger.info("Starting setup with browser: {}", browserName);
        loadProperties();
        try {
            loadExcelData();
            logger.info("Excel data loaded successfully. Total stocks to run: {}", stockDataList.size());
        } catch (IOException e) {
            logger.error("Error loading Excel data", e);
        }
        StartExtentReport(browserName);
        initializeDriver(browserName);
    }

    public void loadProperties() {
        objProp = new Properties();
        try (FileInputStream input = new FileInputStream("src/test/resources/config.properties")) {
            objProp.load(input);
            logger.info("Configuration properties loaded.");
        } catch (IOException e) {
            logger.error("Failed to load properties.", e);
        }
    }

    public void loadExcelData() throws IOException {
        stockDataList = new ArrayList<>();
        String excelFilePath = "src/test/resources/NSEStock_TestData.xlsx";

        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int symbolCol = -1, highCol = -1, lowCol = -1, runModeCol = -1;

            Row headerRow = sheet.getRow(0);
            for (Cell cell : headerRow) {
                String cellVal = cell.getStringCellValue().trim();
                if (cellVal.equalsIgnoreCase("Symbol")) symbolCol = cell.getColumnIndex();
                else if (cellVal.equalsIgnoreCase("52 Week High")) highCol = cell.getColumnIndex();
                else if (cellVal.equalsIgnoreCase("52 Week Low")) lowCol = cell.getColumnIndex();
                else if (cellVal.equalsIgnoreCase("Run mode")) runModeCol = cell.getColumnIndex();
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell runModeCell = row.getCell(runModeCol);
                String runMode = (runModeCell != null) ? runModeCell.getStringCellValue().trim() : "N";

                if (!"Y".equalsIgnoreCase(runMode)) continue;

                String symbol = row.getCell(symbolCol).getStringCellValue().trim().toUpperCase();
                double high = row.getCell(highCol).getNumericCellValue();
                double low = row.getCell(lowCol).getNumericCellValue();

                stockDataList.add(new StockData(symbol, high, low));
            }
        }
    }

    public void StartExtentReport(String browser) {
        String reportPath = System.getProperty("user.dir") + "/test-output/ExtentReport_Excel_" + browser + ".html";
        ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
        reporter.config().setDocumentTitle("Automation Report - " + browser);
        reporter.config().setReportName("NSE Stock Validation - " + browser);
        reporter.config().setTheme(com.aventstack.extentreports.reporter.configuration.Theme.STANDARD);

        ExtReports = new ExtentReports();
        ExtReports.attachReporter(reporter);
        ExtReports.setSystemInfo("Browser", browser);
        ExtReports.setSystemInfo("Environment", objProp.getProperty("environment", "staging"));
        logger.info("Extent report initialized for browser: {}", browser);
    }

    public void initializeDriver(String browser) {
        logger.info("Launching browser: {}", browser);
        if (browser.equals("chrome")) {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
        } else if (browser.equals("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            driver = new FirefoxDriver();
        } else if (browser.equals("edge")) {
            WebDriverManager.edgedriver().setup();
            driver = new EdgeDriver();
        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
        driver.manage().window().maximize();
        logger.info("Browser launched and maximized.");
    }

    @Test(dataProvider = "stockDataProvider")
    public void compare52WeekHighLowWithExcel(StockData stock) {
        ExtTest = ExtReports.createTest("Compare Stock: " + stock.symbol);
        logger.info("Starting test for stock: {}", stock.symbol);
        try {
            driver.get("https://www.nseindia.com/");
            NSEStockPage stockPage = new NSEStockPage(driver);
            stockPage.searchStock(stock.symbol);

            double siteHigh = Double.parseDouble(stockPage.get52WeekHigh().replaceAll(",", "").trim());
            double siteLow = Double.parseDouble(stockPage.get52WeekLow().replaceAll(",", "").trim());

            ExtTest.info(String.format("Excel High: %.2f, Site High: %.2f", stock.excelHigh, siteHigh));
            ExtTest.info(String.format("Excel Low: %.2f, Site Low: %.2f", stock.excelLow, siteLow));

            double avgPurchasePrice = (stock.excelHigh + stock.excelLow) / 2.0;
            double profitLossPercentage = ((siteHigh - stock.excelHigh + siteLow - stock.excelLow) / avgPurchasePrice) * 100;
            profitLossPercentageList.add(profitLossPercentage);

            ExtTest.pass("Profit/Loss Percentage for " + stock.symbol + ": " + String.format("%.2f", profitLossPercentage));
            ScreenshotUtil.takeScreenshot(driver, stock.symbol + "_Success");

            logger.info("Test passed for {}: profitLossPercentage={}", stock.symbol, profitLossPercentage);
        } catch (Exception e) {
            ExtTest.fail("Exception for " + stock.symbol + ": " + e.getMessage());
            ScreenshotUtil.takeScreenshot(driver, stock.symbol + "_Failure");
            logger.error("Test failed for {}", stock.symbol, e);
            Assert.fail(e.getMessage());
        }
    }

    @DataProvider(name = "stockDataProvider")
    public Object[][] stockDataProvider() {
        Object[][] data = new Object[stockDataList.size()][1];
        for (int i = 0; i < stockDataList.size(); i++) {
            data[i][0] = stockDataList.get(i);
        }
        return data;
    }

    public void writeProfitLossToExcel(List<StockData> stocks, List<Double> profitLossPercentages) throws IOException {
        String excelFilePath = "src/test/resources/NSEStock_TestData.xlsx";
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            int profitLossPctCol = -1;
            short lastCellNum = headerRow.getLastCellNum();
            for (int c = 0; c < lastCellNum; c++) {
                Cell cell = headerRow.getCell(c);
                if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Profit/Loss %")) {
                    profitLossPctCol = c;
                    break;
                }
            }
            if (profitLossPctCol == -1) {
                profitLossPctCol = lastCellNum;
                headerRow.createCell(profitLossPctCol).setCellValue("Profit/Loss %");
            }
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell symbolCell = row.getCell(0);
                if (symbolCell != null && symbolCell.getCellType() == CellType.STRING) {
                    Cell plPctCell = row.getCell(profitLossPctCol);
                    if (plPctCell == null) plPctCell = row.createCell(profitLossPctCol);
                    if (i - 1 < profitLossPercentages.size()) {
                        plPctCell.setCellValue(profitLossPercentages.get(i - 1));
                    }
                }
            }
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
            }
            logger.info("Profit/Loss percentages written to Excel");
        }
    }

    @AfterClass
    public void tearDown() {
        try {
            writeProfitLossToExcel(stockDataList, profitLossPercentageList);
            if (ExtReports != null) {
               	ExtReports.flush();
                logger.info("Extent Report flushed");
            }
            if (driver != null) {
                driver.quit();
                logger.info("WebDriver quit");
            }
            logger.info("Teardown complete.");
        } catch (IOException e) {
            logger.error("Error in teardown", e);
        }
    }
}
