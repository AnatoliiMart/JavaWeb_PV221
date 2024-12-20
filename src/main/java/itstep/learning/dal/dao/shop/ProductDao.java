package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.shop.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ProductDao {
    private final Logger logger;
    private final Connection connection;

    @Inject
    public ProductDao(Logger logger, Connection connection) {
        this.logger = logger;
        this.connection = connection;
    }

    public boolean isSlugFree(String slug) {
        String sql = "SELECT COUNT(*) FROM products p WHERE p.product_slug = ?";
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

    public List<Product> allFromCategory(UUID categoryId ) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ? AND delete_dt IS NULL";
        try( PreparedStatement prep = connection.prepareStatement( sql ) ) {
            prep.setString( 1 , categoryId.toString() );
            ResultSet rs = prep.executeQuery();
            while( rs.next() ) {
                products.add( new Product( rs ) ) ;
            }
            rs.close();
        }
        catch( SQLException ex ) {
            logger.log( Level.WARNING, ex.getMessage() + " -- " + sql, ex );
        }
        return products;
    }

    public Product getProductByIdOrSlug( String id ) {
        // id - або slug, або id. Перевіряємо шляхом перетворення до UUID
        if (id == null || id.isEmpty())
            return null;

        String sql = "SELECT * FROM products WHERE ";
        try {
            UUID.fromString( id );
            // шукаємо як id
            sql += "product_id = ?";
        }
        catch( IllegalArgumentException ignored ) {
            // шукаємо як slug
            sql += "product_slug = ?";
        }
        try( PreparedStatement prep = connection.prepareStatement( sql ) ) {
            prep.setString( 1, id );
            ResultSet rs = prep.executeQuery();
            if( rs.next() ) {
                return new Product( rs ) ;
            }
        }
        catch( SQLException ex ) {
            logger.log( Level.WARNING, ex.getMessage() + " -- " + sql, ex );
        }
        return null;
    }

    public Product add(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }
        String sql = "INSERT INTO products( " +
                "product_id, " +
                "category_id, " +
                "product_name, " +
                "product_image_url, " +
                "product_description, " +
                "product_slug, " +
                "product_price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getId().toString());
            ps.setString(2, product.getCategoryId().toString());
            ps.setString(3, product.getName());
            ps.setString(4, product.getImageUrl());
            ps.setString(5, product.getDescription());
            ps.setString(6, product.getSlug());
            ps.setDouble(7, product.getPrice());
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return null;
        }
        return product;
    }

    public boolean installTables() {
        String sql =
                "CREATE TABLE IF NOT EXISTS products (" +
                        "product_id          CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                        "category_id         CHAR(36)     NOT NULL ," +
                        "product_name        VARCHAR(128) NOT NULL," +
                        "product_image_url   VARCHAR(512) NOT NULL," +
                        "product_slug        VARCHAR(128) NULL," +
                        "product_description TEXT         NULL, " +
                        "product_price       FLOAT        NOT NULL," +
                        "delete_dt           DATETIME     NULL  " +
                        ") ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
    }
}
