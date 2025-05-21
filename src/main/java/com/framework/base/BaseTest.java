package com.framework.base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

public class BaseTest {
    protected WebDriver driver;
   
    @Parameters("browser")
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("chrome") String browser) {
        DriverFactory.initDriver(browser);
        driver = DriverFactory.getDriver();
}

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
       DriverFactory.quitDriver();
       
    }
}
