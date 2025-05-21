# Enhanced SDET Automation Framework

This is a robust and modular Test Automation Framework built with Java, Selenium WebDriver, TestNG, Maven, and ExtentReports. It is designed to test stock data from the NSE website with functionalities for comparison and analysis.

## 📁 Project Structure

```
EnhancedSDETFramework/
│
├── pom.xml                         # Maven project file with all dependencies
├── testng_*.xml                    # TestNG suite XMLs
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com.framework.base/          # Core framework setup
│   │   │   ├── com.framework.pages/         # Page Object Model classes
│   │   │   └── com.framework.utils/         # Utility classes (e.g., ExtentReports, Screenshot)
│   │   └── resources/
│   │       ├── config.properties            # Configurations (browser, baseURL, etc.)
│   │       └── log4j2.xml                   # Logging setup
│   └── test/
│       ├── java/com.framework.tests/        # Test classes
│       └── resources/config.properties      # Test-specific configs
```

## ✅ Prerequisites

- Java 11 or above
- Maven 3.6+
- Chrome browser
- IDE (Eclipse/IntelliJ)
- Internet access to download Maven dependencies

## 🚀 Setup Instructions

1. **Clone or Download the Repo**

   ```
   git clone <repo-url>
   ```

2. **Import into IDE**

   - Open Eclipse or IntelliJ.
   - Choose `Import Project` → `Maven` → `Existing Maven Project`.
   - Select the root folder: `EnhancedSDETFramework`.

3. **Install Dependencies**

   Maven will automatically resolve dependencies. You can also run:

   ```
   mvn clean install
   ```

## ⚙️ Configuration

Edit the following config file for custom settings:

`src/main/resources/config.properties`

```properties
browser=chrome
baseUrl=https://www.nseindia.com
implicitWait=10
```

## 🧪 Test Execution

### 1. Using TestNG XML files

Run via terminal:

```
mvn clean test -DsuiteXmlFile=testng_StockComparison.xml
```

Or from your IDE by right-clicking the XML file and selecting **Run as → TestNG Suite**.

### 2. Supported TestNG Suites

- `testng_StockComparison.xml` – Compares multiple stock data.
- `testng_52WeekHighLow.xml` – Validates 52-week high/low stock values.

## 📊 Reports

After execution, reports are generated at:

```
/test-output/ExtentReports/
```

You can open the `.html` report file in a browser to view the results.

## 🛠️ Logging

Logging is configured using `log4j2.xml`. You can update this file to modify the logging behavior.

## 🧹 Clean Up

To clean build files and reports:

```
mvn clean
```

## 📞 Support

For queries or issues, please reach out to the framework maintainer.

## 🔒 License

This project is open for educational and internal enterprise automation purposes. Please consult with the author for commercial use.