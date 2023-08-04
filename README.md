## Zuap

Zuap is a web scraper used to make the apartment hunt around Zurich a bit easier 
(although it can really be used to search for apartments anywhere). Zuap combines web scraping with the discord API to quickly inform
you about any new insertions posted on any of the included websites.

## Using Zuap

If you do not want to host your own version of Zuap, you can simply join the already existing Discord server to use all the features.

[![](https://dcbadge.vercel.app/api/server/BnWfNSzj7N)](https://discord.gg/BnWfNSzj7N)

## Hosting your own version of Zuap

### Prerequisites

Before installing the app, you will need:

- A Discord server and the channel ids for your main channel (where the insertions will be posted) and a logging channel
- A Discord application and the corresponding secret token
- Maven
- At least JDK 18

### Installation

To install this app you need maven. Clone the repository onto your local machine using git. You then need to create a `.env` file in the root project folder
which should contain your _secret_ discord API token (do not publish), as well as the channel IDs for the main (used to post new insertions) and the logging 
channel (used for program logs) of your Discord server. The format should look exactly like this, optional parameters are marked by parentheses, which you
have to leave out in the actual file:

    TOKEN=YOURTOKENHEREWITHOUTQUOTES
    MAIN_CHANNEL_ID=IDHERE
    LOG_CHANNEL_ID=IDHERE
    (ENABLE_WGZIMMER=true)

You can then compile the project using `mvn clean install`. Afterwards run `java -jar target/Zuap-jar-with-dependencies.jar` in a terminal to execute the program.

### WGZimmer

WGZimmer uses reCAPTCHA on their website which requires Zuap to use Selenium.
You need to install Firefox and the Geckodriver for Selenium to work.
Even then, reCAPTCHA gets triggered in some cases, which is why WGZimmer scraping is disabled by default.
If you want to enable it, you can add `ENABLE_WGZIMMER=true` to your `.env` file.