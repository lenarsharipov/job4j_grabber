package ru.job4j.grabber.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Post {

    private int id;
    private String title;
    private String link;
    private String description;
    private LocalDateTime created;

    @Override
    public String toString() {
        return String.format("Post{id=%s, title=%s, link=%s, description=%s, created=%s}",
                id, title, link, description, created);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Post post = (Post) o;
        return id == post.id
                && Objects.equals(title, post.title)
                && Objects.equals(link, post.link)
                && Objects.equals(description, post.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, link, description);
    }

}