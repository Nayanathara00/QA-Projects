package com.automation.product;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

import java.lang.Thread;

import java.util.List;

public class ProductTest {

    WebDriver driver;

    @BeforeMethod
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Paththaya\\Downloads\\chromedriver-win64\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://www.saucedemo.com/");

        // Login before tests
        driver.findElement(By.id("user-name")).sendKeys("standard_user");
        driver.findElement(By.id("password")).sendKeys("secret_sauce");
        driver.findElement(By.id("login-button")).click();
    }

    @Test(priority = 1)
    public void verifyAllProductsAreDisplayed() throws InterruptedException {
        Thread.sleep(3000);
        List<WebElement> products = driver.findElements(By.className("inventory_item"));
        Assert.assertEquals(products.size(), 6, "All 6 products should be displayed");
    }

    @Test(priority = 2)
    public void sortProductsByPriceLowToHigh() throws InterruptedException {
        Thread.sleep(3000);
        WebElement dropdown = driver.findElement(By.className("product_sort_container"));
        Select select = new Select(dropdown);
        select.selectByValue("lohi");

        // Validate sorting by comparing first and last prices
        Thread.sleep(3000);
        List<WebElement> prices = driver.findElements(By.className("inventory_item_price"));
        double firstPrice = Double.parseDouble(prices.get(0).getText().replace("$", ""));
        double lastPrice = Double.parseDouble(prices.get(prices.size() - 1).getText().replace("$", ""));

        Assert.assertTrue(firstPrice <= lastPrice, "Products are sorted by price low to high");
    }

    @Test(priority = 3)
    public void openProductDetailPage() {
        WebElement firstProduct = driver.findElement(By.className("inventory_item_name"));
        String expectedProductName = firstProduct.getText();
        firstProduct.click();

        // FIX: Wait for the product detail name to appear
        WebDriverWait wait = new WebDriverWait(driver, 10); // 10 seconds
        WebElement detailName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_details_name")));

        Assert.assertEquals(detailName.getText(), expectedProductName, "Product detail page opened correctly");
    }

    @Test(priority = 4)
    public void addToCartTest() {
        // Wait to make sure the page has loaded
        WebDriverWait wait = new WebDriverWait(driver, 10);

        // Click "Add to cart" button of the first product
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//button[contains(text(),'Add to cart')])[1]")));
        addToCartButton.click();

        // Click the cart icon
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.className("shopping_cart_link")));
        cartIcon.click();

        // Wait for cart page to load and verify the item is present
        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cart_item")));
        Assert.assertTrue(cartItem.isDisplayed(), "Product was successfully added to the cart");
    }


    @AfterMethod
    public void tearDown() {
        driver.quit();
    }
}
