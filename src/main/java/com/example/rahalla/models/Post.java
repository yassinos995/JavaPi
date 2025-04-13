package com.example.rahalla.models;

import java.sql.Timestamp;

public class Post {
    private int id;

    private User user;

    private String title;

    private String content;

    private String image;

    private int likes;

    private Timestamp createdAt;

    private String lieu;


    // Constructor
    public Post(int id, User user, String title, String content,
                String image, int likes, Timestamp createdAt, String lieu) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.content = content;
        this.image = image;
        this.likes = likes;
        this.createdAt = createdAt;
        this.lieu = lieu;
    }

    public Post() {

    }

    public static Post fromString(String str) {
        Post post = new Post();
        try {
            // Supprimer "Post{" au début et "}" à la fin
            str = str.substring(5, str.length() - 1);

            // Diviser la chaîne en paires clé-valeur
            String[] pairs = str.split(",\\s*");

            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                String key = keyValue[0];
                String value = keyValue[1].replace("'", "");

                switch (key) {
                    case "id":
                        post.setId(Integer.parseInt(value));
                        break;
                    case "user":
                        // Supposant que User a aussi une méthode fromString
                        post.setUser(User.fromString(value));
                        break;
                    case "title":
                        post.setTitle(value);
                        break;
                    case "content":
                        post.setContent(value);
                        break;
                    case "image":
                        post.setImage(value);
                        break;
                    case "likes":
                        post.setLikes(Integer.parseInt(value));
                        break;
                    case "createdAt":
                        post.setCreatedAt(Timestamp.valueOf(value));
                        break;
                    case "lieu":
                        post.setLieu(value);
                        break;
                }
            }
            return post;
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de chaîne invalide : " + str);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", user=" + user +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", image='" + image + '\'' +
                ", likes=" + likes +
                ", createdAt=" + createdAt +
                ", lieu='" + lieu + '\'' +
                '}';
    }
}