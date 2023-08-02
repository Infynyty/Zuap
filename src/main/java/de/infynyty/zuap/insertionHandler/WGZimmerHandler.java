package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.Zuap;
import de.infynyty.zuap.insertion.WGZimmerInsertion;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.logging.Level;

public class WGZimmerHandler extends InsertionHandler<WGZimmerInsertion> {
    
    private final int minPrice = 200;
    private final int maxPrice = 1500;
    private final String wgState = "all";

    public WGZimmerHandler(@NotNull String logPrefix, @NotNull InsertionAnnouncer announcer, @NotNull HttpClient httpClient) {
        super(logPrefix, announcer, httpClient);
    }
    
    private static WebElement condition(WebDriver driver) {
        WebElement entry = findElementSafely(driver, By.className("search-result-entry"));
        if (entry != null) {
            return entry;
        }

        final WebElement captchaFail = findElementSafely(driver, By.xpath("//div[@class='text no-link']/h1"));
        return captchaFail;
    }

    //TODO: Make it possible to change search variables
    @Override
    protected String pullUpdatedData() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        FirefoxDriver driver = new FirefoxDriver(options);

        driver.get("https://www.wgzimmer.ch/wgzimmer/search/mate.html");

        final WebElement priceMinSelect = driver.findElement(By.name("priceMin"));
        final WebElement priceMaxSelect = driver.findElement(By.name("priceMax"));
        final WebElement wgStateSelect = driver.findElement(By.name("wgState"));

        try {
            priceMinSelect.sendKeys(String.valueOf(minPrice));
            priceMaxSelect.sendKeys(String.valueOf(maxPrice));
            wgStateSelect.sendKeys(wgState);
        } catch (IllegalArgumentException e) {
            driver.quit();
            throw new RuntimeException("Website layout changed!");
        }

        final WebElement searchButton = driver.findElement(By.xpath("//input[@value='Suchen']"));
        searchButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        final WebElement element = wait.until(WGZimmerHandler::condition);
        if (element.getTagName().equals("h1")) {
            driver.quit();
            throw new RuntimeException("Captcha failed!");
        }
        String html = driver.getPageSource();
        driver.quit();

        return html;
    }

    private static WebElement findElementSafely(WebDriver driver, By by) {
        try {
            return driver.findElement(by);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    protected ArrayList<WGZimmerInsertion> getInsertionsFromData(final String data) {
        final Document document = Jsoup.parse(data);
        final Elements elements = document.getElementsByClass("search-result-entry search-mate-entry");
        final ArrayList<WGZimmerInsertion> insertions = new ArrayList<>();
        elements.forEach(element -> {
            try {
                insertions.add(new WGZimmerInsertion(element));
            } catch (NumberFormatException e) {
                Zuap.log(Level.WARNING,getHandlerName(),"Insertion could not be included because of a missing insertion number!");
            }
        });
        return insertions;
    }
}
