package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.models.form.ShopCategoryFormModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryDao {
    private final Logger logger;
    private final Connection connection;

    @Inject
    public CategoryDao(Logger logger, Connection connection) {
        this.logger = logger;
        this.connection = connection;
    }

    public boolean isSlugFree(String slug) {
        String sql = "SELECT COUNT(*) FROM categories c WHERE c.category_slug = ?";
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, slug);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getInt(1) == 0;
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
        }
        return false;
    }

    public boolean installTables() {
        String sql =
                "CREATE TABLE IF NOT EXISTS categories (" +
                        "category_id   CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                        "name          VARCHAR(128) NOT NULL," +
                        "image_url     VARCHAR(512) NOT NULL," +
                        "description   TEXT         NULL, " +
                        "category_slug VARCHAR(64)  NULL, " +
                        "delete_dt     DATETIME     NULL  " +
                        ") ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
    }

    public Category add(ShopCategoryFormModel model) {
        Category category = new Category()
                .setId(UUID.randomUUID())
                .setName(model.getName())
                .setDescription(model.getDescription())
                .setImageUrl(model.getSavedFilename())
                .setSlug(model.getSlug());
        String sql = "INSERT INTO categories (category_id, name, image_url, description, category_slug) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category.getId().toString());
            stmt.setString(2, category.getName());
            stmt.setString(3, category.getImageUrl());
            stmt.setString(4, category.getDescription());
            stmt.setString(5, category.getSlug());
            stmt.executeUpdate();
            return category;

        } catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories c WHERE c.delete_dt IS NULL";
        try (Statement statement = connection.createStatement()){
            ResultSet rs =  statement.executeQuery(sql);
            while (rs.next()) {
                categories.add(new Category(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage() + sql, e);
            return null;
        }
        return categories;
    }
}
