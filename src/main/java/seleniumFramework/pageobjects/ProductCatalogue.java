package seleniumFramework.pageobjects;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import seleniumFramework.AbstractComponents.AbstractComponent;

public class ProductCatalogue extends AbstractComponent {

	WebDriver driver;

	public ProductCatalogue(WebDriver driver) {
		// initialization
		super(driver);
		this.driver = driver;
		PageFactory.initElements(driver, this);

	}

	@FindBy(css = ".mb-3")
	List<WebElement> products;
	
	@FindBy(css = ".ng-animating")
	WebElement spinner;

	By productsBy = By.cssSelector(".mb-3");
	By addToCart = By.cssSelector(".card-body button:last-of-type");
	By toastMessage = By.cssSelector("#toast-container");

	public List<WebElement> getProductList() {
		waitForElementToAppear(productsBy);
		return products;
	}
	
	public WebElement getProductByName(String productName)
    {
        // ensure product list is present
        waitForElementToAppear(productsBy);
        WebElement prod = getProductList().stream().filter(product->
        {
            List<WebElement> nameEls = product.findElements(By.cssSelector("b"));
            if(nameEls.isEmpty()) return false;
            String name = nameEls.get(0).getText().trim();
            return name.equalsIgnoreCase(productName);
        }).findFirst().orElse(null);
        return prod;
    }
	
	
	public void addProductToCart(String productName) throws InterruptedException
	{
		WebElement prod = getProductByName(productName);
		if(prod==null) {
			// provide clearer error with available product names
			StringBuilder available = new StringBuilder();
			for(WebElement p: getProductList()){
				List<WebElement> nameEls = p.findElements(By.cssSelector("b"));
				if(!nameEls.isEmpty()) available.append(nameEls.get(0).getText().trim()).append(", ");
			}
			throw new RuntimeException("Product '"+productName+"' not found. Available: "+available.toString());
		}
		// wait for the add-to-cart button to be clickable, scroll into view and click; fallback to JS click
		WebElement addBtn = prod.findElement(addToCart);
		waitForWebElementToAppear(addBtn);
		// dismiss any overlays/toasts that might intercept clicks
		dismissOverlays();
		try {
			org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
			wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(addBtn));
			addBtn.click();
		} catch (org.openqa.selenium.ElementClickInterceptedException e) {
			// scroll and use JS click as fallback
			((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", addBtn);
			((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", addBtn);
		}
		// wait for toast if it appears; don't fail the test if toast isn't present
		try {
			if (waitForElementToAppearOptional(toastMessage, 5)) {
				waitForElementToDisappear(spinner);
			}
		} catch (Exception ignored) {
			// ignore - toast may not appear in some flows
		}


	}
	
	
	
	
	

}