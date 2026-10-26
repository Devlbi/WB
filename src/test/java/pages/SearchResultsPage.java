package pages;

import io.qameta.allure.Step;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SearchResultsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By productCards = By.cssSelector("article[data-nm-id]");
    private final By productLinks = By.cssSelector("article[data-nm-id] a[aria-label]");

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Step("Проверить, что результаты поиска отображаются")
    public boolean areProductsDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(productCards));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Получить список названий товаров")
    public List<String> getProductTitles() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(productCards));
        wait.until(ExpectedConditions.presenceOfElementLocated(productLinks));

        return driver.findElements(productLinks).stream()
                .map(link -> link.getAttribute("aria-label"))
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @Step("Открыть первый товар из списка")
    public ProductPage openFirstProduct() {
        wait.until(ExpectedConditions.presenceOfElementLocated(productLinks));

        String originalWindow = driver.getWindowHandle();
        WebElement firstLink = driver.findElements(productLinks).get(0);
        firstLink.click();

        try {
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String h : driver.getWindowHandles()) {
                if (!h.equals(originalWindow)) {
                    driver.switchTo().window(h);
                    break;
                }
            }
        } catch (Exception ignored) {
            // Открылось в той же вкладке
        }
        return new ProductPage(driver);
    }

    @Step("Диагностика: HTML первой карточки")
    public void dumpFirstArticleHtml() {
        try {
            WebElement first = driver.findElement(productCards);
            System.out.println("=== HTML первой карточки ===");
            System.out.println(first.getAttribute("outerHTML"));
            System.out.println("=== конец ===");
        } catch (Exception e) {
            System.out.println("Не удалось выгрузить HTML: " + e.getMessage());
        }
    }
}