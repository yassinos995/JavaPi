package com.example.rahalla.services;

import com.example.rahalla.models.User;
import com.example.rahalla.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements ICrud<User> {
    private final Connection conn;

    public UserService() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE is_active = true ORDER BY nom, prenom";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get all users" + e.getMessage());
        }
        return users;
    }

    @Override
    public User getById(int id) {
        String query = "SELECT * FROM user WHERE id = ? AND is_active = true";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get user by ID: " + id + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean add(User user) {
        String query = "INSERT INTO user (email, password, nom, prenom, telephone, is_man, date_de_naissance, image, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getNom());
                stmt.setString(4, user.getPrenom());
                stmt.setString(5, user.getTelephone());
                stmt.setBoolean(6, user.isIs_man());
                stmt.setDate(7, new java.sql.Date(user.getDate_de_naissance().getTime()));
                stmt.setString(8, user.getImage());
                stmt.setBoolean(9, user.isIs_active());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }

                System.out.println("User added successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to add user" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(User user) {
        String query = "UPDATE user SET email = ?, password = ?, nom = ?, prenom = ?, telephone = ?, " +
                "is_man = ?, date_de_naissance = ?, image = ?, is_active = ? WHERE id = ?";
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getNom());
                stmt.setString(4, user.getPrenom());
                stmt.setString(5, user.getTelephone());
                stmt.setBoolean(6, user.isIs_man());
                stmt.setDate(7, new java.sql.Date(user.getDate_de_naissance().getTime()));
                stmt.setString(8, user.getImage());
                stmt.setBoolean(9, user.isIs_active());
                stmt.setInt(10, user.getId());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                System.out.println("User updated successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to update user" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean remove(int id) {
        // Soft delete - set is_active to false instead of actually deleting
        String query = "UPDATE user SET is_active = false WHERE id = ?";
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                System.out.println("User deactivated successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to deactivate user with ID: " + id + e.getMessage());
        }
        return false;
    }

    @Override
    public List<User> search(String keyword) {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE is_active = true AND (nom LIKE ? OR prenom LIKE ? OR email LIKE ?) " +
                "ORDER BY nom, prenom";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to search users with keyword: " + keyword + e.getMessage());
        }
        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        try {
            return new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("telephone"),
                    rs.getBoolean("is_man"),
                    rs.getDate("date_de_naissance"),
                    rs.getString("image"),
                    rs.getBoolean("is_active")
            );
        } catch (SQLException e) {
            System.out.println("Error mapping result set to user" + e.getMessage());
            throw e;
        }
    }

    // Additional user-specific methods
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ? AND is_active = true";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get user by email: " + email + e.getMessage());
        }
        return null;
    }

    public boolean isEmailTaken(String email, Integer excludeUserId) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ? AND is_active = true" +
                (excludeUserId != null ? " AND id != ?" : "");

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            if (excludeUserId != null) {
                stmt.setInt(2, excludeUserId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if email is taken: " + email + e.getMessage());
        }
        return false;
    }
}
