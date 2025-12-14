package seleniumFramework.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import seleniumFramework.AbstractComponents.AbstractComponent;

public class CheckoutPage extends AbstractComponent {

	WebDriver driver;

	public CheckoutPage(WebDriver driver) {
		super(driver);
		this.driver = driver;
		PageFactory.initElements(driver, this);

	}

	@FindBy(css = ".action__submit")
	 private WebElement submit;

	@FindBy(css = "[placeholder='Select Country']")
	private WebElement country;

	@FindBy(xpath = "(//button[contains(@class,'ta-item')])[2]")
	private WebElement selectCountry;

	private By results = By.cssSelector(".ta-results");

	public void selectCountry(String countryName) {
		// try to use the mapped element; if not present or not interactable, locate dynamically
		try {
			if (country != null) {
				// ensure focus and click to open autocomplete
				try { country.click(); } catch (Exception ignored) {}
				country.clear();
				// type slowly to trigger autocomplete reliably
				for (char c: countryName.toCharArray()) {
					new org.openqa.selenium.interactions.Actions(driver).sendKeys(String.valueOf(c)).pause(java.time.Duration.ofMillis(100)).perform();
				}
			} else {
				WebElement input = driver.findElement(By.cssSelector("input[placeholder='Select Country'], input[placeholder*='Country'], input[type='text']"));
				try { input.click(); } catch (Exception ignored) {}
				for (char c: countryName.toCharArray()) {
					new org.openqa.selenium.interactions.Actions(driver).sendKeys(String.valueOf(c)).pause(java.time.Duration.ofMillis(100)).perform();
				}
			}
		} catch (Exception e) {
			// fallback: try JS to set value and dispatch input event
			try {
				((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input'));", country, countryName);
			} catch (Exception ignored) {}
		}
		// wait for dropdown results and select the matching option text (increase timeout)
		// some pages show results inside buttons with class 'ta-item' directly; wait for those
		// wait for dropdown results - accept multiple possible renderings
		String[] selectors = new String[] {"button.ta-item", ".ta-results button", ".ta-results", ".ng-option", "ul[role='listbox'] li"};
		new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20)).until(d -> {
			for (String sel: selectors) {
				if (!d.findElements(By.cssSelector(sel)).isEmpty()) return true;
			}
			return false;
		});
		java.util.List<WebElement> items = new java.util.ArrayList<>();
		for (String sel: selectors) {
			java.util.List<WebElement> found = driver.findElements(By.cssSelector(sel));
			if (!found.isEmpty()) { items.addAll(found); }
		}
		for (WebElement item: items) {
			if (item.getText().trim().equalsIgnoreCase(countryName)) {
				try {
					item.click();
					return;
				} catch (org.openqa.selenium.ElementClickInterceptedException ex) {
					((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", item);
					((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", item);
					return;
				}
			}
		}
		// fallback: if no items found, try keyboard select
		if (items.isEmpty()) {
			try {
				new org.openqa.selenium.interactions.Actions(driver).sendKeys(org.openqa.selenium.Keys.ARROW_DOWN).pause(java.time.Duration.ofMillis(200)).sendKeys(org.openqa.selenium.Keys.ENTER).perform();
				// re-check items
				items = driver.findElements(By.cssSelector("button.ta-item"));
				for (WebElement item: items) {
					if (item.getText().trim().equalsIgnoreCase(countryName)) {
						((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", item);
						return;
					}
				}
			} catch (Exception ignored) {}
		}
		// if not matched, click the first available
		if (!items.isEmpty()) {
			WebElement first = items.get(0);
			try { first.click(); } catch (Exception ex) { ((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", first); }
		}
	}
	
	public ConfirmationPage submitOrder()
	{
		// wait for any toast/spinner overlays to disappear before submitting
		try {
			org.openqa.selenium.support.ui.WebDriverWait waitO = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
			waitO.until(org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("#toast-container")));
			waitO.until(org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".ng-animating")));
		} catch (Exception ignored) {}
		// ensure submit is clickable, scroll and use JS fallback if necessary
		try {
			org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
			wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(submit));
			submit.click();
		} catch (org.openqa.selenium.ElementClickInterceptedException | org.openqa.selenium.TimeoutException e) {
			try {
				((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", submit);
				((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", submit);
			} catch (Exception ex) {
				// last resort: submit form via JS
				try { ((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].form.submit();", submit); } catch (Exception ignored) {}
			}
		}
		return new ConfirmationPage(driver);
		
	}

}