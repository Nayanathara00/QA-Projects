package com.automation.product;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.List;
import java.util.HashMap;

import java.lang.Thread;

public class CheckoutTest {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeMethod
    public void setUp() throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Paththaya\\Downloads\\chromedriver-win64\\chromedriver.exe");


        // Chrome options: incognito + disable password manager popups
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--disable-features=PasswordSaving,PasswordManagerOnboarding,PasswordLeakDetection,AutofillServerCommunication,AutofillEnableAccountWalletStorage");

        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("autofill.profile_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, 10);

        driver.get("https://www.saucedemo.com/");
        Thread.sleep(1500); // pause to see page load

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys("standard_user");
        Thread.sleep(800);
        driver.findElement(By.id("password")).sendKeys("secret_sauce");
        Thread.sleep(800);
        driver.findElement(By.id("login-button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Thread.sleep(1500);
    }

    @AfterMethod
    public void tearDown() throws InterruptedException {
        Thread.sleep(2000); // wait to see final page before closing
        driver.quit();
    }

    private void addTwoItemsAndVerifyBadge() throws InterruptedException {
        By addBackpack  = By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']");
        By addBikeLight = By.cssSelector("button[data-test='add-to-cart-sauce-labs-bike-light']");
        By badgeSel     = By.className("shopping_cart_badge");

        WebElement btn1 = wait.until(ExpectedConditions.elementToBeClickable(addBackpack));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn1);
        btn1.click();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(badgeSel, "1"));

        WebElement btn2 = wait.until(ExpectedConditions.elementToBeClickable(addBikeLight));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn2);
        btn2.click();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(badgeSel, "2"));

        Thread.sleep(1000);
    }

    private void goToCartReliably() throws InterruptedException {
        WebElement cart = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.shopping_cart_link")));
        cart.click();
        Thread.sleep(1000);
        try {
            new WebDriverWait(driver, 3).until(ExpectedConditions.urlContains("cart.html"));
        } catch (TimeoutException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cart);
        }
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("cart.html"),
                ExpectedConditions.visibilityOfElementLocated(By.id("cart_contents_container"))
        ));
        Thread.sleep(1000);
    }

    @Test(priority = 1)
    public void checkoutHappyPath_andVerifyTotals() throws InterruptedException {
        addTwoItemsAndVerifyBadge();
        goToCartReliably();

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='checkout']"))).click();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout_info_container")));

        driver.findElement(By.id("first-name")).sendKeys("Nayanathara");
        Thread.sleep(500);
        driver.findElement(By.id("last-name")).sendKeys("Samarakkody");
        Thread.sleep(500);
        driver.findElement(By.id("postal-code")).sendKeys("10200");
        Thread.sleep(1000);
        driver.findElement(By.cssSelector("input[data-test='continue']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout_summary_container")));
        Thread.sleep(1000);

        List<WebElement> priceEls = driver.findElements(By.cssSelector(".cart_item .inventory_item_price"));
        double sum = 0.0;
        for (WebElement el : priceEls) {
            sum += Double.parseDouble(el.getText().replace("$", "").trim());
        }

        String itemTotalText = driver.findElement(By.cssSelector(".summary_subtotal_label")).getText();
        double itemTotal = Double.parseDouble(itemTotalText.replace("Item total: $", "").trim());
        Assert.assertEquals(itemTotal, sum, 0.001);

        Assert.assertTrue(driver.findElement(By.cssSelector(".summary_tax_label")).isDisplayed());
        Assert.assertTrue(driver.findElement(By.cssSelector(".summary_total_label")).isDisplayed());

        Thread.sleep(1000);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='finish']"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout_complete_container")));
        Thread.sleep(1000);
        String header = driver.findElement(By.cssSelector(".complete-header")).getText();
        Assert.assertTrue(header.toUpperCase().contains("THANK YOU"));
    }

    @Test(priority = 2)
    public void checkoutShowsValidation_forMissingFirstName() throws InterruptedException {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']"))).click();
        Thread.sleep(1000);
        goToCartReliably();

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='checkout']"))).click();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout_info_container")));

        driver.findElement(By.id("last-name")).sendKeys("Samarakkody");
        Thread.sleep(500);
        driver.findElement(By.id("postal-code")).sendKeys("10200");
        Thread.sleep(500);
        driver.findElement(By.cssSelector("input[data-test='continue']")).click();

        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Thread.sleep(1000);
        Assert.assertTrue(err.getText().contains("First Name is required"));
    }
}
