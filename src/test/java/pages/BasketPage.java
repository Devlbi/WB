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

public class BasketPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Контейнер товара в корзине. У WB стабильно есть атрибут data-testid="basket-item"
    // на карточке. Если вдруг нет — замени на ".basket-item" или похожее.
    private final By basketItems = By.cssSelector(
            "[data-testid='basket-item'], .basket-item, .good-info");

    // Название товара внутри карточки корзины.
    // Используем aria-label или заголовок — что найдётся.
    private final By itemTitles = By.cssSelector(
            "[data-testid='basket-item'] a[aria-label], " +
                    ".basket-item a[aria-label], " +
                    ".good-info__title, " +
                    "[data-testid='basket-item-name']");

    public BasketPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Step("Проверить, что корзина не пуста")
    public boolean isNotEmpty() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(basketItems));
            return true;
        } catch (Exception e) {
            System.out.println(">>> Корзина пуста или не открылась: "
                    + e.getClass().getSimpleName());
            return false;
        }
    }

    @Step("Получить названия товаров в корзине")
    public List<String> getItemTitles() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(basketItems));
        return driver.findElements(itemTitles).stream()
                .map(el -> {
                    String aria = el.getAttribute("aria-label");
                    return (aria != null && !aria.isBlank()) ? aria : el.getText();
                })
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    @Step("Получить текущий URL")
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Step("Диагностика: HTML корзины")
    public void dumpBasketHtml() {
        try {
            System.out.println("=== URL: " + driver.getCurrentUrl());
            System.out.println("=== Title: " + driver.getTitle());
            List<WebElement> items = driver.findElements(basketItems);
            System.out.println("=== Найдено элементов корзины: " + items.size());
            if (!items.isEmpty()) {
                String html = items.get(0).getAttribute("outerHTML");
                System.out.println(html.length() > 2000 ? html.substring(0, 2000) : html);
            }
        } catch (Exception e) {
            System.out.println("Ошибка дампа корзины: " + e.getMessage());
        }
    }
}