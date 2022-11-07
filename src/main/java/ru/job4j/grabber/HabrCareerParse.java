package ru.job4j.grabber;

import org.jsoup.Jsoup;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final int PAGES = 5;

    private static final String DELIMITER = "?page=";

    private static final List<Post> POSTS = new ArrayList<>();

    private static HabrCareerDateTimeParser dateTimeParser;

    public HabrCareerParse(HabrCareerDateTimeParser habrCareerDateTimeParser) {
        dateTimeParser = habrCareerDateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        try {
            parsePosts();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return POSTS;
    }

    private static void parsePosts() throws IOException {
        for (var page = 1; page <= PAGES; page++) {
            var connection = Jsoup.connect(String.format("%s%s%s", PAGE_LINK, DELIMITER, page));
            var document = connection.get();
            var rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                var titleElement = row.select(".vacancy-card__title").first();
                var linkElement = titleElement.child(0);
                var vacancyName = titleElement.text();
                var link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                var dateElement = row.select(".vacancy-card__date").first();
                var vacancyDateElement = dateElement.child(0);
                var dateTime = String.format("%s", vacancyDateElement.attr("datetime"));
                String description;
                try {
                    description = retrieveDescription(link);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                POSTS.add(new Post(
                        vacancyName,
                        link,
                        description,
                        dateTimeParser.parse(dateTime)
                ));
            });
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        var connection = Jsoup.connect(link);
        var document = connection.get();
        var rows = document.select(".vacancy-show");
        return rows.select(".style-ugc").text();
    }


    public static void main(String[] args) throws IOException {
        var rowSeparator = "*".repeat(60);
        for (var page = 1; page <= PAGES; page++) {
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
                var dateTime = String.format("%s", vacancyDateElement.attr("datetime"));

                try {
                    var description = retrieveDescription(link);
                    System.out.printf("%s %s %s%n%s%n", vacancyName, link, dateTime, description);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}