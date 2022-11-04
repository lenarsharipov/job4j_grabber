package ru.job4j.grabber;

import org.jsoup.Jsoup;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String DELIMITER = "?page=";

    public static void main(String[] args) throws IOException {
        var page = 1;
        var rowSeparator = "*".repeat(60);
        while (page <= 5) {
            var connection = Jsoup.connect(String.format("%s%s%s", PAGE_LINK, DELIMITER, page));
            var document = connection.get();
            var rows = document.select(".vacancy-card__inner");
            System.out.printf("%n%s Page: %s %s%n%n", rowSeparator, page, rowSeparator);
            rows.forEach(row -> {
                var titleElement = row.select(".vacancy-card__title").first();
                var linkElement = titleElement.child(0);
                var vacancyName = titleElement.text();
                var link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));

                var dateElement = row.select(".vacancy-card__date").first();
                var vacancyDateElement = dateElement.child(0);
                String dateTime = String.format("%s", vacancyDateElement.attr("datetime"));
                System.out.printf("%s %s %s%n", vacancyName, link, dateTime);
            });
            page++;
        }
    }
}
