package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DriverManager {

    private static WebDriver driver;

    public static WebDriver getDriver() {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();

            //Заглушит не только CDP-warning, но и весь остальной шум от Selenium(будет чистая консоль)
            java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(java.util.logging.Level.OFF);

            // 1. Убираем автоматизационные флаги Chrome
            options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);
            options.addArguments("--disable-blink-features=AutomationControlled");

            // 2. Используем новый, менее заметный headless-режим
            // options.addArguments("--headless=new"); // Раскомментируйте, если нужен headless

            // 3. Устанавливаем реалистичный User-Agent
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/155.0.0.0 Safari/537.36");
            options.addArguments("--user-data-dir=C:\\selenium-profile\\wb"); //грузит через папку диска С (т.к антибот Wildberries не дает вход на страницу)
            options.addArguments("--start-maximized");

            driver = new ChromeDriver(options);

            // 4. КЛЮЧЕВОЙ МОМЕНТ: Удаляем след webdriver через CDP
            // Эта команда выполняется для каждой новой страницы и делает navigator.webdriver = undefined
            Map<String, Object> params = new HashMap<>();
            params.put("source", "Object.defineProperty(navigator, 'webdriver', { get: () => undefined })");
            ((ChromeDriver) driver).executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
            }
            driver = null;
        }
    }
}