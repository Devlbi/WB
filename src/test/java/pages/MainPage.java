package pages;

import io.qameta.allure.Step;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;



public class MainPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Селектор подтверждён скриншотом — он правильный
    private final By searchInput = By.cssSelector("input#searchInput");

    public MainPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Step("Открыть главную страницу Wildberries")
    public void open() {
        driver.get("https://www.wildberries.by/");

        // Проверяем, что окно вообще живо
        if (driver.getWindowHandles().isEmpty()) {
            throw new IllegalStateException("Окно браузера закрылось сразу после запуска. " +
                    "Проверь, что нет других процессов chrome.exe с этим профилем.");
        }

        String current = driver.getWindowHandle();
        for (String h : driver.getWindowHandles()) {
            if (!h.equals(current)) {
                driver.switchTo().window(h);
            }
        }
    }

    @Step("Ввести поисковый запрос: {query}")
    public void typeSearchQuery(String query) {
        WebElement input = wait.until(
                ExpectedConditions.visibilityOfElementLocated(searchInput)
        );
        input.clear();
        input.sendKeys(query);
    }

    @Step("Нажать Enter для поиска")
    public void submitSearch() {
        driver.findElement(searchInput).sendKeys(Keys.ENTER);
    }

    @Step("Проверить, что поле поиска отображается")
    public boolean isSearchInputDisplayed() {
        try {
            if (driver.getWindowHandles().isEmpty()) {
                System.out.println(">>> Окно браузера уже закрыто");
                return false;
            }
            wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
            return true;
        } catch (Exception e) {
            System.out.println(">>> Поле поиска не появилось: "
                    + e.getClass().getSimpleName() + " / " + e.getMessage());
            return false;
        }
    }

    @Step("Сделать диагностический скриншот: {tag}")
    public void dumpDebug(String tag) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dst = Path.of("target", "debug-" + tag + ".png");
            Files.createDirectories(dst.getParent());
            Files.copy(src.toPath(), dst, StandardCopyOption.REPLACE_EXISTING);
            System.out.println(">>> Скриншот: " + dst.toAbsolutePath());
            System.out.println(">>> URL: " + driver.getCurrentUrl());
            System.out.println(">>> Title: " + driver.getTitle());
            System.out.println(">>> Открытых окон: " + driver.getWindowHandles().size());
        } catch (Exception e) {
            System.out.println(">>> Не удалось снять debug: " + e.getMessage());
        }
    }
}