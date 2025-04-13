package com.example.rahalla.models;

import java.sql.Timestamp;

public class PostLike {
    private int id;

    private User user;

    private Post post;

    private Timestamp created_at;

    public PostLike(int id, User user, Post post, Timestamp created_at) {
        this.id = id;
        this.user = user;
        this.post = post;
        this.created_at = created_at;
    }

    public PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
        this.created_at = new Timestamp(System.currentTimeMillis());
    }

    public PostLike() {
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

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "PostLike{" +
                "id=" + id +
                ", user=" + user +
                ", post=" + post +
                ", created_at=" + created_at +
                '}';
    }
}
