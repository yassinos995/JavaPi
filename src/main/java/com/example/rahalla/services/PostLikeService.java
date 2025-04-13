package com.example.rahalla.services;

import com.example.rahalla.models.PostLike;
import com.example.rahalla.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostLikeService implements ICrud<PostLike> {
    private final UserService userService;
    private final PostService postService;
    private Connection conn;

    public PostLikeService() {
        this.conn = DatabaseConnection.getInstance().getConnection();
        this.userService = new UserService();
        this.postService = new PostService();
    }

    @Override
    public List<PostLike> getAll() {
        List<PostLike> likes = new ArrayList<>();
        String query = "SELECT * FROM post_likes ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PostLike like = mapResultSetToPostLike(rs);
                if (like != null) {
                    likes.add(like);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get all post likes" + e.getMessage());
        }
        return likes;
    }

    @Override
    public PostLike getById(int id) {
        String query = "SELECT * FROM post_likes WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPostLike(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get post like by ID: " + id + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean add(PostLike like) {
        String query = "INSERT INTO post_likes (user_id, post_id, created_at) VALUES (?, ?, ?)";
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, like.getUser().getId());
                stmt.setInt(2, like.getPost().getId());
                stmt.setTimestamp(3, like.getCreated_at());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        like.setId(generatedKeys.getInt(1));
                    }
                }

                // Increment the post's like count
                if (!postService.incrementLikes(like.getPost().getId())) {
                    return false;
                }

                System.out.println("Post like added successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to add post like" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(PostLike like) {
        String query = "UPDATE post_likes SET user_id = ?, post_id = ?, created_at = ? WHERE id = ?";
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, like.getUser().getId());
                stmt.setInt(2, like.getPost().getId());
                stmt.setTimestamp(3, like.getCreated_at());
                stmt.setInt(4, like.getId());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                System.out.println("Post like updated successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to update post like" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean remove(int id) {
        // First, get the post ID to decrement its like count
        PostLike like = getById(id);
        if (like == null) {
            return false;
        }

        String query = "DELETE FROM post_likes WHERE id = ?";
        try {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                // Decrement the post's like count using a new method in PostService
                // You'll need to add this method to PostService
                if (!postService.decrementLikes(like.getPost().getId())) {
                    return false;
                }

                System.out.println("Post like deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Failed to delete post like with ID: " + id + e.getMessage());
        }
        return false;
    }

    @Override
    public List<PostLike> search(String keyword) {
        // Post likes typically don't need text search, but implemented for interface compliance
        return new ArrayList<>();
    }

    public List<PostLike> getLikesByPostId(int postId) {
        List<PostLike> likes = new ArrayList<>();
        String query = "SELECT * FROM post_likes WHERE post_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, postId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PostLike like = mapResultSetToPostLike(rs);
                    if (like != null) {
                        likes.add(like);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get likes for post: " + postId + e.getMessage());
        }
        return likes;
    }

    public boolean hasUserLikedPost(int userId, int postId) {
        String query = "SELECT COUNT(*) FROM post_likes WHERE user_id = ? AND post_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if user " + userId + " liked post " + postId + e.getMessage());
        }
        return false;
    }

    private PostLike mapResultSetToPostLike(ResultSet rs) throws SQLException {
        try {
            return new PostLike(
                    rs.getInt("id"),
                    userService.getById(rs.getInt("user_id")),
                    postService.getById(rs.getInt("post_id")),
                    rs.getTimestamp("created_at")
            );
        } catch (SQLException e) {
            System.out.println("Error mapping result set to post like" + e.getMessage());
            throw e;
        }
    }
}
