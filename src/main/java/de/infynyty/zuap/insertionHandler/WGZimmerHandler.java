package de.infynyty.zuap.insertionHandler;

import de.infynyty.zuap.Zuap;
import de.infynyty.zuap.insertion.WGZimmerInsertion;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.logging.Level;

public class WGZimmerHandler extends InsertionHandler<WGZimmerInsertion> {


    public WGZimmerHandler(@NotNull String logPrefix, @NotNull InsertionAnnouncer announcer, @NotNull HttpClient httpClient) {
        super(logPrefix, announcer, httpClient);
    }

    //TODO: Make it possible to change search variables
    @Override
    protected String pullUpdatedData() {
        ChromeDriver driver = new ChromeDriver();
        driver.get("https://www.wgzimmer.ch/wgzimmer/search/mate.html");

        WebElement priceMinSelect = driver.findElement(By.name("priceMin"));
        WebElement priceMaxSelect = driver.findElement(By.name("priceMax"));
        WebElement wgStateSelect = driver.findElement(By.name("wgState"));

        priceMinSelect.sendKeys("200");
        priceMaxSelect.sendKeys("1500");
        wgStateSelect.sendKeys("all");

        WebElement searchButton = driver.findElement(By.xpath("//input[@value='Suchen']"));
        searchButton.click();

        var wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(webDriver -> webDriver.findElement(By.className("search-result-entry")));
        var html = driver.getPageSource();
        driver.quit();

        return html;
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
