## Zuap

Zuap is a web scraper used to make the apartment hunt around Zurich a bit easier 
(although it can really be used to search for apartments anywhere). Zuap combines web scraping with the discord API to quickly inform
you about any new insertions posted on any of the included websites.

## Installation

To install this app you need maven. Clone the repository onto your local machine using git. You then need to create a `.env` file in the root project folder
which should contain your _secret_ discord API token (do not publish), as well as the channel IDs for the main (used to post new insertions) and the logging 
channel (used for programm logs) of your Discord server. The format should look exactly like this:

    TOKEN=YOURTOKENHEREWITHOUTQUOTES
    MAIN_CHANNEL_ID=IDHERE
    LOG_CHANNEL_ID=IDHERE

You can then compile the project using `mvn clean install`
