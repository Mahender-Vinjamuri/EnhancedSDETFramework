# NSE Stock Automation Framework

## 🔍 Overview
This project is a TestNG-based Selenium automation framework that fetches stock data (52-week high and low) from the NSE website. It is structured using best practices for maintainability and extensibility.

## 📁 Project Structure
```
EnhancedSDETFramework/
│
├── src/
│   ├── main/
│   │   ├── java/com/framework/base/         # Base classes for driver management
│   │   ├── java/com/framework/pages/        # Page classes (e.g., NSEStockPage)
│   │   └── java/com/framework/utils/        # Utilities like screenshots and reporting
│   └── test/
│       └── java/com/framework/tests/        # TestNG test classes
│
├── testng.xml                               # TestNG suite definition
├── pom.xml                                  # Maven dependencies
├── config.properties                        # Configuration file
├── README.md                                # Project documentation
```

## ⚙️ Prerequisites
- Java 11 or higher
- Maven 3.6+
- ChromeDriver (Ensure it's compatible with your browser version)
- IntelliJ IDEA / Eclipse

## 🔧 Setup Instructions
1. Clone or extract this project into your working directory.
2. Open it in your preferred IDE.
3. Run `mvn clean install` to install dependencies.
4. Update `config.properties` if needed (e.g., base URL or driver path).

## ▶️ Running the Tests
You can run the tests in two ways:

### Option 1: From IDE
- Right-click `testng.xml` → Run as TestNG Suite.

### Option 2: From Terminal
```bash
mvn clean test
```

## ✅ Enhanced Logic for Timeout Handling
In `NSEStockPage.java`, both `get52WeekHigh()` and `get52WeekLow()`:
- Use WebDriverWait to fetch elements.
- Gracefully handle `TimeoutException` and return `null` if the element is not found.
- The test passes only if stock values are successfully captured.

```java
try {
    WebElement high = new WebDriverWait(driver, Duration.ofSeconds(30))
        .until(ExpectedConditions.visibilityOfElementLocated(By.id("week52highVal")));
    return high.getText();
} catch (TimeoutException e) {
    logger.warn("Timeout while fetching stock value");
    return null;
}
```

## 📌 Author
Mahender Vinjamuri
