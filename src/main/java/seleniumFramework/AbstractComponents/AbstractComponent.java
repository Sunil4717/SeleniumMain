package seleniumFramework.AbstractComponents;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import seleniumFramework.pageobjects.CartPage;
import seleniumFramework.pageobjects.OrderPage;

public class AbstractComponent {
	
	WebDriver driver;

	public AbstractComponent(WebDriver driver) {
		
		this.driver = driver;
		PageFactory.initElements(driver, this);
		
	}
	
	@FindBy(css = "[routerlink*='cart']")
	WebElement cartHeader;
	
	@FindBy(css = "[routerlink*='myorders']")
	WebElement orderHeader;


	public void waitForElementToAppear(By findBy) {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		wait.until(ExpectedConditions.visibilityOfElementLocated(findBy));

	}
	
	public void waitForWebElementToAppear(WebElement findBy) {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		wait.until(ExpectedConditions.visibilityOf(findBy));

	}
	
	public boolean waitForElementToAppearOptional(By findBy, int seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
            wait.until(ExpectedConditions.visibilityOfElementLocated(findBy));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
	
	public CartPage goToCartPage()
	{
		// ensure header visible and try safe click; fallback to JS click if intercepted
		waitForWebElementToAppear(cartHeader);
		// wait for any toast overlay to disappear (toast may block clicks)
		try {
			WebDriverWait waitOverlay = new WebDriverWait(driver, java.time.Duration.ofSeconds(5));
			waitOverlay.until(org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#toast-container")));
		} catch (Exception e) {
			// ignore if not present
		}
		try {
			org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5));
			wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(cartHeader));
			cartHeader.click();
		} catch (org.openqa.selenium.ElementClickInterceptedException e) {
			((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", cartHeader);
			((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", cartHeader);
		}
		CartPage cartPage = new CartPage(driver);
		return cartPage;
	}
	
	public OrderPage goToOrdersPage()
	{
		waitForWebElementToAppear(orderHeader);
		try {
			org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5));
			wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(orderHeader));
			orderHeader.click();
		} catch (org.openqa.selenium.ElementClickInterceptedException e) {
			((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", orderHeader);
			((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", orderHeader);
		}
		OrderPage orderPage = new OrderPage(driver);
		return orderPage;
	}
	public void waitForElementToDisappear(WebElement ele) throws InterruptedException
	{
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
			wait.until(ExpectedConditions.invisibilityOf(ele));
		} catch (Exception e) {
			// fallback small sleep if invisibility wait fails
			Thread.sleep(500);
		}

	}
	
	public void dismissOverlays() {
        try {
            ((JavascriptExecutor)driver).executeScript("Array.from(document.querySelectorAll('#toast-container, .ng-animating, .blockUI, .overlay, .modal-backdrop')).forEach(e=>e.style.display='none');");
        } catch (Exception e) {
            // ignore
        }
    }

}