package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_PAGE = "https://career.habr.com";

    private static final int PAGES = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String sourceLink) {
        List<Post> posts = new ArrayList<>();
        for (var page = 1; page <= PAGES; page++) {
            try {
                var connection = Jsoup.connect(String.format("%s%s", sourceLink, page));
                var document = connection.get();
                var rows = document.select(".vacancy-card__inner");
                for (var element : rows) {
                    posts.add(
                            parsePost(element)
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return posts;
    }

    private Post parsePost(Element element) {

        var titleElement = element.select(".vacancy-card__title").first();
        var linkElement = titleElement.child(0);
        var vacancyName = titleElement.text();
        var subLink = String.format("%s%s", SOURCE_PAGE, linkElement.attr("href"));
        var dateElement = element.select(".vacancy-card__date").first();
        var vacancyDateElement = dateElement.child(0);
        var dateTime = String.format("%s", vacancyDateElement.attr("datetime"));
        String description;
        description = retrieveDescription(subLink);
        return new Post(
                vacancyName,
                subLink,
                description,
                dateTimeParser.parse(dateTime)
        );

    }

    private static String retrieveDescription(String link) {
        var connection = Jsoup.connect(link);
        Document document;
        try {
            document = connection.get();
        } catch (IOException e) {
            throw new IllegalArgumentException("Passed arguments illegal");
        }
        var rows = document.select(".vacancy-show");
        return rows.select(".style-ugc").text();
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> posts = habrCareerParse.list(
                "https://career.habr.com/vacancies/java_developer?page="
        );
        for (Post post : posts) {
            System.out.println(post);
        }
    }

}
