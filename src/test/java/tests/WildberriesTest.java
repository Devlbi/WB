package tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import pages.BasketPage;
import pages.MainPage;
import pages.ProductPage;
import pages.SearchResultsPage;
import utils.DriverManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Epic("UI Wildberries")
@Feature("Search")
@Story("Search results")
@Tag("Smoke")
@DisplayName("Поиск товаров на wildberries.by")
@Owner("Петрович Денис")
// 1. Включаем сортировку тестов по аннотации @Order
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WildberriesTest {

    private MainPage mainPage;

    @BeforeEach
    public void setUp() {
        mainPage = new MainPage(DriverManager.getDriver());
        mainPage.open();
    }

    @AfterEach
    public void tearDown() {
        DriverManager.quitDriver();
    }

    @Test
    @Order(1) // 2. Задаем порядковый номер
    @DisplayName("Главная страница открывается и поле поиска видно")
    public void testMainPageLoads() {
        boolean ok = mainPage.isSearchInputDisplayed();
        if (!ok) {
            mainPage.dumpDebug("main-page");
        }
        assertTrue(ok, "Поле поиска не отображается на главной");
    }

    @Test
    @Order(2)
    @DisplayName("Поиск 'ноутбук' возвращает непустой список")
    public void testSearchReturnsResults() {
        mainPage.typeSearchQuery("ноутбук");
        mainPage.submitSearch();

        SearchResultsPage results = new SearchResultsPage(DriverManager.getDriver());
        assertTrue(results.areProductsDisplayed(),
                "Результаты поиска не отображаются");

        List<String> titles = results.getProductTitles();
        assertFalse(titles.isEmpty(), "Список товаров пуст");
        System.out.println("Найдено товаров: " + titles.size());
        System.out.println("Первый товар: " + titles.get(0));
    }

    @Test
    @Order(3)
    @DisplayName("Клик по первой карточке открывает страницу товара")
    public void testOpenFirstProduct() {
        mainPage.typeSearchQuery("ноутбук");
        mainPage.submitSearch();

        SearchResultsPage results = new SearchResultsPage(DriverManager.getDriver());

        List<String> titles = results.getProductTitles();
        assertFalse(titles.isEmpty(), "Список товаров пуст");
        String expectedTitle = titles.get(0);
        System.out.println("Ожидаемое название: " + expectedTitle);

        ProductPage product = results.openFirstProduct();

        String url = product.getCurrentUrl();
        System.out.println("URL после клика: " + url);
        assertTrue(url.contains("/catalog/") || url.contains("/detail.aspx"),
                "Не открылась страница товара. URL: " + url);

        String actualTitle;
        try {
            actualTitle = product.getCurrentProductTitle();
            System.out.println("Фактическое название: " + actualTitle);
        } catch (Exception e) {
            System.out.println("!!! Не удалось получить название, дамп страницы:");
            product.dumpProductPageHeader();
            throw e;
        }

        assertFalse(actualTitle.isBlank(), "Название товара на странице пустое");
        assertTrue(actualTitle.toLowerCase().contains(expectedTitle.toLowerCase())
                        || expectedTitle.toLowerCase().contains(actualTitle.toLowerCase()),
                "Название на странице товара не совпадает.\n" +
                        "Ожидалось: " + expectedTitle + "\n" +
                        "Получено:  " + actualTitle);
    }

    @Test
    @Order(4)
    @DisplayName("Добавление первого товара в корзину")
    @Story("Basket")
    public void testAddProductToBasket() {
        mainPage.typeSearchQuery("ноутбук");
        mainPage.submitSearch();

        SearchResultsPage results = new SearchResultsPage(DriverManager.getDriver());
        List<String> titles = results.getProductTitles();
        assertFalse(titles.isEmpty(), "Список товаров пуст");
        String expectedTitle = titles.get(0);
        System.out.println("Ожидаемый товар: " + expectedTitle);

        ProductPage product = results.openFirstProduct();

        try {
            product.addToCart();
        } catch (Exception e) {
            System.out.println("!!! Не удалось нажать 'В корзину', дамп страницы:");
            product.dumpAddToCartButton();
            throw e;
        }

        BasketPage basket = product.openBasket();

        if (!basket.isNotEmpty()) {
            basket.dumpBasketHtml();
        }
        assertTrue(basket.isNotEmpty(), "Корзина пуста — товар не добавился");

        List<String> basketTitles = basket.getItemTitles();
        System.out.println("В корзине: " + basketTitles);

        boolean found = basketTitles.stream().anyMatch(t ->
                t.toLowerCase().contains(expectedTitle.toLowerCase())
                        || expectedTitle.toLowerCase().contains(t.toLowerCase()));
        assertTrue(found,
                "Добавленный товар не найден в корзине.\nОжидали: " + expectedTitle
                        + "\nВ корзине: " + basketTitles);
    }
}