package com.framework.tests;

import com.framework.base.BaseTest;
import com.framework.pages.NSEStockPage;
import com.framework.utils.ScreenshotUtil;

import io.github.bonigarcia.wdm.WebDriverManager;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

public class NSEStockTest extends BaseTest {
    private ExtentReports ExtReports;
    private ExtentTest ExtTest;
    private String screenshotPath;
    private Properties objProp;
    private String browser;

    @BeforeClass
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        this.browser = browser;
        System.out.println("[Setup] Initializing test configuration for browser: " + browser);
        loadProperties();
        StartExtentReport(browser);
        initializeDriver(browser);
        System.out.println("[Setup] Setup complete.\n");
    }

    public void loadProperties() {
        objProp = new Properties();
        try (FileInputStream input = new FileInputStream("src/test/resources/config.properties")) {
            objProp.load(input);
            System.out.println("[Config] Loaded properties from config.properties");
        } catch (IOException e) {
            System.err.println("[Config] Failed to load properties.");
            e.printStackTrace();
        }
    }

    public void StartExtentReport(String browser) {
        try {
            System.out.println("[Report] Initializing Extent Report for browser: " + browser);

            File outputDir = new File("test-output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            String reportPath = "test-output/ExtentReport_" + browser.toLowerCase() + ".html";
            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setDocumentTitle("NSE Stock Automation Report - " + browser);
            spark.config().setReportName("52-Week Stock Value Check - " + browser);
            spark.config().setTheme(com.aventstack.extentreports.reporter.configuration.Theme.STANDARD);

            ExtReports = new ExtentReports();
            ExtReports.attachReporter(spark);

            ExtReports.setSystemInfo("Browser", browser);
            ExtReports.setSystemInfo("Environment", objProp.getProperty("environment", "staging"));

            System.out.println("[Report] Extent Report initialized at: " + reportPath + "\n");

        } catch (Exception e) {
            System.err.println("[Report] Failed to initialize Extent Report for browser: " + browser);
            e.printStackTrace();
        }
    }

    @Test(dataProvider = "stocks")
    public void testStockInformation(String stockName) {
        System.out.println("[Test] Executing test for stock: " + stockName + " on browser: " + browser);
        ExtTest = ExtReports.createTest("Test Stock Information for: " + stockName + " [" + browser + "]");
        ExtTest.info("Test started for stock: " + stockName);

        try {
        	driver.get("https://www.nseindia.com/");
            NSEStockPage stockPage = new NSEStockPage(driver);
            System.out.println("[Test] Searching stock: " + stockName);
            stockPage.searchStock(stockName);

            String high = stockPage.get52WeekHigh();
            String low = stockPage.get52WeekLow();

            System.out.println("[Test] Retrieved values - High: " + high + ", Low: " + low);

            Assert.assertNotNull(low, "52-week low should not be null for " + stockName);
            Assert.assertNotNull(high, "52-week high should not be null for " + stockName);

            ExtTest.pass("52-week High for " + stockName + ": " + high);
            ExtTest.pass("52-week Low for " + stockName + ": " + low);

            screenshotPath = ScreenshotUtil.takeScreenshot(driver, stockName + "_" + browser + "_screenshot");
            ExtTest.info("Screenshot saved at: " + screenshotPath);
            System.out.println("[Test] Screenshot captured: " + screenshotPath + "\n");

        } catch (SkipException e) {
            ExtTest.skip("Test skipped for stock: " + stockName + " due to: " + e.getMessage());
            System.out.println("[Test] SKIPPED - " + e.getMessage());
        } catch (Exception e) {
            ExtTest.fail("Test failed for stock: " + stockName + " due to: " + e.getMessage());
            screenshotPath = ScreenshotUtil.takeScreenshot(driver, stockName + "_" + browser + "_error");
            ExtTest.addScreenCaptureFromPath(screenshotPath);
            System.err.println("[Test] FAILED - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void initializeDriver(String browser) {
        browser = browser.toLowerCase();
        System.out.println("[Driver] Launching browser: " + browser);
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
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        System.out.println("[Driver] Browser launched and maximized.\n");
    }

    @AfterClass
    public void tearDown() {
        try {
            if (driver != null) {
                driver.quit();
                System.out.println("[Teardown] Browser closed for: " + browser);
            }
        } catch (Exception e) {
            System.err.println("[Teardown] Exception closing driver: " + e.getMessage());
        }
        try {
            ExtReports.flush();
            System.out.println("[Teardown] Extent Report flushed for: " + browser);
        } catch (Exception e) {
            System.err.println("[Teardown] Exception flushing ExtentReports: " + e.getMessage());
        }
    }

    @DataProvider(name = "stocks")
    public Object[][] getStockData() {
        System.out.println("[DataProvider] Supplying stock data.");
        return new Object[][] {
            { "TATAMOTORS" },
            { "INFY" },
            { "RELIANCE" }
        };
    }
}
