package saucedemo.selenium;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import listeners.TestListener;
import saucedemo.selenium.pages.CartPage;
import saucedemo.selenium.pages.CheckoutCompletePage;
import saucedemo.selenium.pages.CheckoutStepOnePage;
import saucedemo.selenium.pages.CheckoutStepTwoPage;
import saucedemo.selenium.pages.LoginPage;
import saucedemo.selenium.pages.ProductsPage;
import utils.EvidenceCaptureUtil;
import utils.ExtentManager;
import utils.ScreenshotUtil;
import utils.ScreenshotUtilTestNG;
import utils.TestLogger;

import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Listeners(TestListener.class)
public class PurchaseOrderTest {
	private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderTest.class);
	String url = "https://www.saucedemo.com/";
	WebDriver driver;

	ExtentReports extent;
	ExtentTest test;

	// Path for storing execution screenshots
	String dirEvidence = "..\\saucedemo-selenium-java\\evidence\\";

	@BeforeSuite
	public void setUp() {
		logger.info("========== Initializing Automation Suite ===========");

		// Chrome options to handle popups and incognito mode
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--incognito");
		options.addArguments("--disable-save-password-bubble");
		options.addArguments("--disable-notifications");

		driver = new ChromeDriver(options);
		extent = ExtentManager.getInstance();
		driver.get(url);

		logger.info("Navigated to URL: {}", url);
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
	}

	@Test(description = "CP01 - User Login Flow", priority = 1)
	public void userLogin() throws InterruptedException {
		LoginPage loginPage = new LoginPage(driver);
		TestLogger.logInfo("Starting Login Process");

		// Visual pause before typing
		Thread.sleep(2000);

		ScreenshotUtil.captureAndAddToReport(driver, "login_page_initial", "System state before login");

		// Login action
		loginPage.enterCredentials("standard_user", "secret_sauce");

		logger.info("Credentials submitted.");

		// Visual pause to see the successful login transition
		Thread.sleep(3000);
	}

	@Test(description = "CP02 - Product Selection and Cart", priority = 2)
	public void preOrden() throws IOException, InterruptedException {

		EvidenceCaptureUtil.getScreenshot(driver, dirEvidence, "1_inventory_overview.jpg");

		ProductsPage productListPage = new ProductsPage(driver);
		TestLogger.logInfo("Adding product to cart");

		Thread.sleep(1500);
		productListPage.clickBtnAddToCart();

		logger.info("Product added to cart.");
		Thread.sleep(2000);

		EvidenceCaptureUtil.getScreenshot(driver, dirEvidence, "2_cart_badge_updated.jpg");

		productListPage.clickLinkCart();
		logger.info("Navigating to Shopping Cart");

		Thread.sleep(2500);
		EvidenceCaptureUtil.getScreenshot(driver, dirEvidence,"3_cart_page_content.jpg");
	}

	@Test(description = "CP03 - Checkout and Final Confirmation", priority = 3)
	public void checkOut() throws IOException, InterruptedException {
		CartPage cartPage = new CartPage(driver);
		TestLogger.logInfo("Proceeding to Checkout");

		cartPage.clickBtnCheckout();
		Thread.sleep(2000);

		EvidenceCaptureUtil.getScreenshot(driver, dirEvidence, "4_checkout_information_form.jpg");

		// Fill shipping information with pauses for visual confirmation
		CheckoutStepOnePage checkoutStepOnePage = new CheckoutStepOnePage(driver);

		checkoutStepOnePage.enterFirstName("John");
		Thread.sleep(1000);

		checkoutStepOnePage.enterLastName("Doe");
		Thread.sleep(1000);

		checkoutStepOnePage.enterPostalCode("12345");
		logger.info("Shipping form completed.");
		Thread.sleep(2000);

		EvidenceCaptureUtil.getScreenshot(driver, dirEvidence, "5_form_filled.jpg");

		checkoutStepOnePage.clickBtnContinue();
		Thread.sleep(2500);

		EvidenceCaptureUtil.getScreenshot(driver, dirEvidence,"6_checkout_summary_overview.jpg");

		CheckoutStepTwoPage checkoutStepTwoPage = new CheckoutStepTwoPage(driver);
		logger.info("Confirming final order.");

		checkoutStepTwoPage.clickBtnFinish();

		// Long pause at the end to display the success message
		Thread.sleep(4000);

		EvidenceCaptureUtil.getScreenshot(driver, dirEvidence,"7_final_confirmation.jpg");

		CheckoutCompletePage checkoutCompletePage = new CheckoutCompletePage(driver);
		ScreenshotUtilTestNG.captureAndEmbedScreenshot(driver, "order_complete_screen");

		String msgFinal = checkoutCompletePage.getMsgFinalText();
		logger.info("Order message found: {}", msgFinal);

		TestLogger.log(Status.INFO, "Verification Step - Final Message: {}", msgFinal);

		// Validation of the test case
		Assert.assertEquals(msgFinal, "Checkout: Complete!");
	}

	@AfterSuite
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
		if (extent != null) {
			extent.flush();
		}
		logger.info("========== Execution Finished ===========");
	}
}