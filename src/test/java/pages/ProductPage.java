package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ProductPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ИСПРАВЛЕНИЕ: Ищем по точному data-testid.
    // Это исключит скрытую мобильную кнопку (у которой testid заканчивается на -compact)
    private final By addToCartButton = By.cssSelector(
            "button[data-testid='add-to-basket-button']");

    private final By goToBasketLink = By.xpath(
            "//a[contains(normalize-space(.), 'Перейти в корзину')] " +
                    "| //button[contains(normalize-space(.), 'Перейти в корзину')]");

    private final By headerBasketLink = By.cssSelector("a[href*='/lk/basket']");

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Step("Добавить товар в корзину")
    public void addToCart() {
        // Ждем именно видимую кнопку
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(addToCartButton));

        // Прокручиваем к кнопке
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);

        // Кликаем, если не выходит - через JS
        try {
            btn.click();
        } catch (Exception e) {
            System.out.println(">>> Обычный click() не сработал, пробуем JS: "
                    + e.getClass().getSimpleName());
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        // Ждем появления мини-корзины или изменения кнопки
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(goToBasketLink),
                    ExpectedConditions.attributeContains(
                            addToCartButton, "aria-label", "корзине")
            ));
        } catch (Exception ignored) {
        }
    }

    @Step("Открыть корзину")
    public BasketPage openBasket() {
        List<WebElement> goLinks = driver.findElements(goToBasketLink);
        if (!goLinks.isEmpty()) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(goToBasketLink)).click();
                switchToNewWindowIfAny();
                return new BasketPage(driver);
            } catch (Exception ignored) {
            }
        }

        try {
            wait.until(ExpectedConditions.elementToBeClickable(headerBasketLink)).click();
        } catch (Exception e) {
            driver.get("https://www.wildberries.by/lk/basket");
        }

        switchToNewWindowIfAny();
        return new BasketPage(driver);
    }

    private void switchToNewWindowIfAny() {
        try {
            String original = driver.getWindowHandle();
            for (String h : driver.getWindowHandles()) {
                if (!h.equals(original)) {
                    driver.switchTo().window(h);
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Step("Получить название товара на открытой странице")
    public String getCurrentProductTitle() {
        wait.until(d -> d.getTitle() != null
                && !d.getTitle().equalsIgnoreCase("Wildberries")
                && d.getTitle().length() > 20);

        return driver.getTitle()
                .replaceFirst("\\s+\\d{7,10}\\s+.*$", "")
                .trim();
    }

    @Step("Получить текущий URL")
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Step("Диагностика: заголовок страницы товара")
    public void dumpProductPageHeader() {
        try {
            System.out.println("=== URL страницы: " + driver.getCurrentUrl());
            System.out.println("=== Title: " + driver.getTitle());

            List<WebElement> h1s = driver.findElements(By.tagName("h1"));
            System.out.println("=== Найдено <h1>: " + h1s.size());
            for (WebElement h : h1s) {
                System.out.println("h1 HTML: " + h.getAttribute("outerHTML"));
                System.out.println("h1 text: " + h.getText());
            }
        } catch (Exception e) {
            System.out.println("Ошибка дампа: " + e.getMessage());
        }
    }

    @Step("Диагностика: HTML кнопок 'корзину'")
    public void dumpAddToCartButton() {
        try {
            List<WebElement> btns = driver.findElements(
                    By.cssSelector("button[aria-label*='корзину']"));
            System.out.println("=== Найдено кнопок с 'корзину' в aria-label: "
                    + btns.size());
            for (WebElement b : btns) {
                System.out.println("----");
                System.out.println("aria-label: " + b.getAttribute("aria-label"));
                System.out.println("data-testid: " + b.getAttribute("data-testid"));
                System.out.println("displayed: " + b.isDisplayed());
                System.out.println("HTML: " + b.getAttribute("outerHTML"));
            }
        } catch (Exception e) {
            System.out.println("Ошибка дампа: " + e.getMessage());
        }
    }
}