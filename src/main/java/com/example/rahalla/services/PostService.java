package com.example.rahalla.services;

import com.example.rahalla.models.Post;
import com.example.rahalla.models.User;
import com.example.rahalla.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService implements ICrud<Post> {

    private final UserService userService;

    private final Connection conn;

    public PostService() {
        this.conn = DatabaseConnection.getInstance().getConnection();
        this.userService = new UserService();
    }

    @Override
    public boolean add(Post post) {
        String query = "INSERT INTO post (user_id, title, content, image, likes, created_at, lieu) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, post.getUser().getId());
                ps.setString(2, post.getTitle());
                ps.setString(3, post.getContent());

                // Handle null image path properly
                if (post.getImage() == null || post.getImage().isEmpty()) {
                    ps.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(4, post.getImage());
                }

                ps.setInt(5, post.getLikes());
                ps.setTimestamp(6, post.getCreatedAt());
                ps.setString(7, post.getLieu());

                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        post.setId(generatedKeys.getInt(1));
                    }
                }

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error adding post: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            return false;
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM post";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Post post = mapResultSetToPost(rs);
                if (post != null) {
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving posts" + e.getMessage());
        }
        return posts;
    }

    @Override
    public Post getById(int id) {
        String query = "SELECT * FROM post WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPost(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving post with ID " + id + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(Post post) {
        String query = "UPDATE post SET title = ?, content = ?, image = ?, lieu = ? WHERE id = ?";

        try {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, post.getTitle());
                ps.setString(2, post.getContent());
                ps.setString(3, post.getImage());
                ps.setString(4, post.getLieu());
                ps.setInt(5, post.getId());

                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error updating post with ID " + post.getId() + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean remove(int id) {
        String query = "DELETE FROM post WHERE id = ?";

        try {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, id);

                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error removing post with ID " + id + e.getMessage());
            return false;
        }
    }

    public boolean incrementLikes(int postId) {
        String query = "UPDATE post SET likes = likes + 1 WHERE id = ?";

        try {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, postId);

                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error incrementing likes for post " + postId + e.getMessage());
            return false;
        }
    }

    public boolean decrementLikes(int postId) {
        String query = "UPDATE post SET likes = GREATEST(likes - 1, 0) WHERE id = ?";

        try {
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, postId);

                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    return false;
                }

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error decrementing likes for post " + postId + e.getMessage());
            return false;
        }
    }

    public int getLikeCount(int postId) {
        String query = "SELECT likes FROM post WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, postId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("likes");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting like count for post " + postId + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<Post> search(String keyword) {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM post WHERE  title LIKE ? OR content LIKE ? OR lieu LIKE ? ORDER BY created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Post post = mapResultSetToPost(rs);
                    if (post != null) {
                        posts.add(post);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching posts with keyword: " + keyword + e.getMessage());
        }
        return posts;
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        try {
            Post post = new Post();
            post.setId(rs.getInt("id"));
            post.setTitle(rs.getString("title"));
            post.setContent(rs.getString("content"));
            post.setImage(rs.getString("image"));
            post.setLikes(rs.getInt("likes"));
            post.setCreatedAt(rs.getTimestamp("created_at"));
            post.setLieu(rs.getString("lieu"));

            User user = userService.getById(rs.getInt("user_id"));
            if (user != null) {
                post.setUser(user);
                return post;
            }
        } catch (SQLException e) {
            System.out.println("Error mapping result set to post" + e.getMessage());
            throw e;
        }
        return null;
    }
}
