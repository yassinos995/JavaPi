package com.example.rahalla.models;

import java.util.Arrays;
import java.util.Date;

public class User {
    private int id;
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private String telephone;
    private boolean is_man;
    private Date date_de_naissance;
    private String image;
    private boolean is_active;
    private String[] roles;

    public User(int id, String email, String password, String nom, String prenom, String telephone, boolean is_man, Date date_de_naissance, String image, boolean is_active, String[] roles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.is_man = is_man;
        this.date_de_naissance = date_de_naissance;
        this.image = image;
        this.is_active = is_active;
        this.roles = roles;
    }

    public User(int id, String email, String password, String nom, String prenom, String telephone, boolean is_man, Date date_de_naissance, String image, boolean is_active) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.is_man = is_man;
        this.date_de_naissance = date_de_naissance;
        this.image = image;
        this.is_active = is_active;
    }

    public User(String email, String password, String nom, String prenom, String telephone, boolean is_man, Date date_de_naissance, String image, boolean is_active, String[] roles) {
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.is_man = is_man;
        this.date_de_naissance = date_de_naissance;
        this.image = image;
        this.is_active = is_active;
        this.roles = roles;
    }

    public User() {
    }

    public static User fromString(String str) {
        User user = new User();
        try {
            // Enlever "User{" et "}"
            str = str.substring(5, str.length() - 1);

            // Séparer les paires clé-valeur
            String[] pairs = str.split(",\\s*");

            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1].replace("'", "") : "";

                switch (key) {
                    case "id":
                        user.setId(Integer.parseInt(value));
                        break;
                    case "email":
                        user.setEmail(value);
                        break;
                    case "password":
                        user.setPassword(value);
                        break;
                    case "nom":
                        user.setNom(value);
                        break;
                    case "prenom":
                        user.setPrenom(value);
                        break;
                    case "telephone":
                        user.setTelephone(value);
                        break;
                    case "is_man":
                        user.setIs_man(Boolean.parseBoolean(value));
                        break;
                    case "date_de_naissance":
                        if (!value.equals("null")) {
                            user.setDate_de_naissance(new Date(Long.parseLong(value)));
                        }
                        break;
                    case "image":
                        user.setImage(value);
                        break;
                    case "is_active":
                        user.setIs_active(Boolean.parseBoolean(value));
                        break;
                    case "roles":
                        String rolesStr = value.replace("[", "").replace("]", "");
                        if (!rolesStr.isEmpty()) {
                            user.setRoles(rolesStr.split(", "));
                        }
                        break;
                }
            }
            return user;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isIs_man() {
        return is_man;
    }

    public void setIs_man(boolean is_man) {
        this.is_man = is_man;
    }

    public Date getDate_de_naissance() {
        return date_de_naissance;
    }

    public void setDate_de_naissance(Date date_de_naissance) {
        this.date_de_naissance = date_de_naissance;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", is_man=" + is_man +
                ", date_de_naissance=" + date_de_naissance +
                ", image='" + image + '\'' +
                ", is_active=" + is_active +
                ", roles=" + Arrays.toString(roles) +
                '}';
    }
}
