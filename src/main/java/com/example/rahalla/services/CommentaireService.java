package com.example.rahalla.services;

import com.example.rahalla.models.Commentaire;
import com.example.rahalla.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements ICrud<Commentaire> {
    private final UserService userService;
    private final PostService postService;
    private Connection conn;

    public CommentaireService() {
        this.conn = DatabaseConnection.getInstance().getConnection();
        this.userService = new UserService();
        this.postService = new PostService();
    }

    @Override
    public List<Commentaire> getAll() {
        List<Commentaire> comments = new ArrayList<>();
        String query = "SELECT * FROM comment ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Commentaire comment = mapResultSetToComment(rs);
                if (comment != null) {
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get all comments" + e.getMessage());
        }
        return comments;
    }

    @Override
    public Commentaire getById(int id) {
        String query = "SELECT * FROM comment WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToComment(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get comment by ID: " + id + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean add(Commentaire comment) {
        String query = "INSERT INTO comment (user_id, post_id, content, created_at) VALUES (?, ?, ?, ?)";
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, comment.getUser().getId());
                stmt.setInt(2, comment.getPost().getId());
                stmt.setString(3, comment.getContent());
                stmt.setTimestamp(4, new Timestamp(comment.getCreated_at().getTime()));

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        comment.setId(generatedKeys.getInt(1));
                    }
                }

                System.out.println("Comment added successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to add comment" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Commentaire comment) {
        String query = "UPDATE comment SET content = ? WHERE id = ? AND user_id = ?";
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, comment.getContent());
                stmt.setInt(2, comment.getId());
                stmt.setInt(3, comment.getUser().getId());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                System.out.println("Comment updated successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to update comment" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean remove(int id) {
        String query = "DELETE FROM comment WHERE id = ?";
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                System.out.println("Comment deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to delete comment with ID: " + id + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Commentaire> search(String keyword) {
        List<Commentaire> comments = new ArrayList<>();
        String query = "SELECT * FROM comment WHERE content LIKE ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Commentaire comment = mapResultSetToComment(rs);
                    if (comment != null) {
                        comments.add(comment);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to search comments with keyword: " + keyword + e.getMessage());
        }
        return comments;
    }

    public List<Commentaire> getCommentsByPostId(int postId) {
        List<Commentaire> comments = new ArrayList<>();
        String query = "SELECT * FROM comment WHERE post_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, postId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Commentaire comment = mapResultSetToComment(rs);
                    if (comment != null) {
                        comments.add(comment);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get comments for post: " + postId + e.getMessage());
        }
        return comments;
    }

    private Commentaire mapResultSetToComment(ResultSet rs) throws SQLException {
        try {
            return new Commentaire(
                    rs.getInt("id"),
                    userService.getById(rs.getInt("user_id")),
                    postService.getById(rs.getInt("post_id")),
                    rs.getString("content"),
                    rs.getTimestamp("created_at")
            );
        } catch (SQLException e) {
            System.out.println("Error mapping result set to comment" + e.getMessage());
            throw e;
        }
    }
}
