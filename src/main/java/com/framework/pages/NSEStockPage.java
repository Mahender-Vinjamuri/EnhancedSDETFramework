package com.framework.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class NSEStockPage {
    private WebDriver driver;
    private final Logger logger = LogManager.getLogger(NSEStockPage.class);

    public NSEStockPage(WebDriver driver) {
        this.driver = driver;
    }

	public void searchStock(String stockName) {
        try {
            System.out.println("[searchStock] Waiting for search box to be visible...");
            WebElement searchBox = new WebDriverWait(driver, Duration.ofSeconds(25))
                .until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@class,'form-control rbt-input')]")));

            System.out.println("[searchStock] Typing stock name: " + stockName);
            searchBox.clear();
            searchBox.sendKeys(stockName);
            waitForPageLoad(driver, 30);
            // Wait for autocomplete suggestions to appear
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
             System.out.println("[searchStock] Waiting for autocomplete suggestions...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("(//div[contains(@class,'rbt-menu')]/a)[1]"))).click();;

		   // Wait for page load after selection
            System.out.println("[searchStock] Waiting for page load after selecting stock...");
            waitForPageLoad(driver, 30);

        } catch (Exception e) {
            System.err.println("[searchStock] Error searching for stock: " + stockName);
            e.printStackTrace();
            throw new RuntimeException("Failed to search stock '" + stockName + "'", e);
        }
    }

    public String get52WeekHigh() {
		  try {
	    		 waitForPageLoad(driver, 30);
	             // Using WebDriverWait for element visibility and interaction
	             WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(90));
	             WebElement highElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@id='week52highVal']")));
	             
	             // Once element is visible, return the text
	             return highElement.getText();
	         } catch (TimeoutException e) {
	             logger.error("52 Week High element not found within timeout", e);
	             return "N/A"; // Return default value in case of failure
	         } catch (Exception e) {
	             logger.error("An error occurred while fetching 52 Week High", e);
	             return "N/A"; // Return default value for any other exceptions
	         }
	     }
    
    


    public String get52WeekLow() {
    	 try {
    		 waitForPageLoad(driver, 30);
             // Using WebDriverWait for element visibility and interaction
             WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(90));
             WebElement lowElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@id='week52lowVal']")));
             
             // Once element is visible, return the text
             return lowElement.getText();
         } catch (TimeoutException e) {
             logger.error("52 Week Low element not found within timeout", e);
             return "N/A"; // Return default value in case of failure
         } catch (Exception e) {
             logger.error("An error occurred while fetching 52 Week Low", e);
             return "N/A"; // Return default value for any other exceptions
         }
     }
    
    public void waitForPageLoad(WebDriver driver, int timeoutInSeconds) throws InterruptedException {
    	Thread.sleep(3000);
        new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds)).until(
            webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
    
    } 
}
