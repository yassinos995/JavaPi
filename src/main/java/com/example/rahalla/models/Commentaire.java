package com.example.rahalla.models;

import java.util.Date;


public class Commentaire {
    private int id;

    private User user;

    private Post post;

    private String content;

    private Date created_at;

    public Commentaire(int id, User user, Post post, String content, Date created_at) {
        this.id = id;
        this.user = user;
        this.post = post;
        this.content = content;
        this.created_at = created_at;
    }

    public Commentaire(User user, Post post, String content, Date created_at) {
        this.user = user;
        this.post = post;
        this.content = content;
        this.created_at = created_at;
    }

    public Commentaire() {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", user=" + user +
                ", post=" + post +
                ", content='" + content + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}
