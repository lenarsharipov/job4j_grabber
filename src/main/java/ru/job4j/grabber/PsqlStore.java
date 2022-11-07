package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private final Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = DriverManager.getConnection(
                cfg.getProperty("url"),
                cfg.getProperty("username"),
                cfg.getProperty("password")
        );
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                cnn.prepareStatement(
                        "insert into post(title, link, description, created) values(?, ?, ?, ?) on conflict(link) do nothing;",
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setString(3, post.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Post createPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("link"),
                resultSet.getString("description"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement
                     = cnn.prepareStatement("select * from post;")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(createPost(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement
                = cnn.prepareStatement("select * from post where id = ?;")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    post = createPost(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        Properties cfg = new Properties();
        try (InputStream in =
                     PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            cfg.load(in);
            try (PsqlStore psqlStore = new PsqlStore(cfg)) {
                HabrCareerDateTimeParser habrDateTimeParser = new HabrCareerDateTimeParser();
                String link = "https://career.habr.com/vacancies/1000112784";
                String desc = "Компания «ЕМЕ» является одним из лидером России по разработке ПО для автоматизации логистики.";
                Post post1 = new Post(
                    "Андроид разработчик",
                    link, desc, habrDateTimeParser.parse("2022-10-31T13:18:50+03:00")
                );

                link = "https://career.habr.com/vacancies/1000110386";
                desc = "СберОбразование — компания экосистемы Сбер, основанная в марте 2021 года.";
                Post post2 = new Post(
                    "Backend developer (Java)",
                    link, desc, habrDateTimeParser.parse("2022-11-07T12:00:09+03:00")
                );

            psqlStore.save(post1);
            System.out.println(psqlStore.getAll());
            psqlStore.save(post2);
            System.out.println("------------------------------------------------");
            System.out.println(psqlStore.findById(2));
            System.out.println("------------------------------------------------");
            System.out.println(psqlStore.getAll());
            System.out.println("------------------------------------------------");
            System.out.println(psqlStore.findById(100));
        }
    } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
