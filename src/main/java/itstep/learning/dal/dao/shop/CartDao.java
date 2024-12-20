package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.shop.CartItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CartDao {
    private final Logger logger;
    private final Connection connection;

    @Inject
    public CartDao(Logger logger, Connection connection) {
        this.logger = logger;
        this.connection = connection;
    }

    public List<CartItem> getCart(String userId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(userId);
        } catch (Exception ignored) {
            return null;
        }
        String sql = "SELECT * " +
                "FROM carts c " +
                "JOIN cart_items ci ON  c.cart_id = ci.cart_id " +
                "JOIN products p ON ci.product_id = p.product_id " +
                "WHERE c.user_id = ? " +
                "AND c.close_dt IS NULL " +
                "AND (c.isCanceled = 0 OR c.isCanceled IS NULL ) ";
        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, uuid.toString());
            ResultSet rs = prep.executeQuery();
            List<CartItem> cartItems = new ArrayList<>();
            while (rs.next()) {
                cartItems.add(new CartItem(rs));
            }
            return cartItems;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
        }
        return null;
    }

    public boolean installTables() {
        String sql =
                "CREATE TABLE IF NOT EXISTS carts (" +
                        "cart_id    CHAR(36) PRIMARY KEY  DEFAULT( UUID() )," +
                        "user_id    CHAR(36) NOT NULL," +
                        "open_dt    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "close_dt   DATETIME NULL," +
                        "isCanceled TINYINT  NULL" +
                        ") ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
        sql =
                "CREATE TABLE IF NOT EXISTS cart_items (" +
                        "cart_id    CHAR(36) NOT NULL DEFAULT( UUID() )," +
                        "product_id CHAR(36) NOT NULL," +
                        "cnt        INT      NOT NULL DEFAULT 1," +
                        "PRIMARY KEY (cart_id, product_id)" +
                        ") ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
    }

    public boolean add(UUID userId, UUID productId, int cnt) {
        UUID cartId = null;
        // Спочатку шукаємо чи є активний кошик користувача
        String sql = "SELECT c.cart_id FROM carts c WHERE c.user_id = ? AND c.close_dt IS NULL";
        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, userId.toString());
            ResultSet rs = prep.executeQuery();
            if (rs.next()) { // Є активний кошик
                cartId = UUID.fromString(rs.getString(1));
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
        if (cartId == null) { // Немає активного кошика

            cartId = UUID.randomUUID();
            sql = "INSERT INTO carts (cart_id, user_id) VALUES (?, ?)";
            try (PreparedStatement prep = connection.prepareStatement(sql)) {
                prep.setString(1, cartId.toString());
                prep.setString(2, userId.toString());
                prep.executeUpdate();
            } catch (SQLException ex) {
                logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
                return false;
            }
        }
        // cartId - посилається на активний кошик
        // 2. Перевіряємо чи є в кошику даний товар.
        // Якщо є - збільшуємо кількість, якщо ні - додаємо
        sql = "SELECT count(*) FROM cart_items c WHERE c.cart_id = ? AND c.product_id = ?";
        int count;
        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, cartId.toString());
            prep.setString(2, productId.toString());
            ResultSet rs = prep.executeQuery();
            rs.next();
            count = rs.getInt(1);
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
        if (count == 0) { // Немає товару в кошику
            sql = "INSERT INTO cart_items (cnt, cart_id, product_id) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE cart_items SET cnt = cnt + ? WHERE cart_id = ? AND product_id = ?";
        }
        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setInt(1, count);
            prep.setString(2, cartId.toString());
            prep.setString(3, productId.toString());
            prep.executeUpdate();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
        return true;
    }

    public boolean update(UUID cartId, UUID productId, int delta) throws Exception {
        if (cartId == null || productId == null || delta == 0) {
            return false;
        }
        String sql = "SELECT cnt FROM cart_items WHERE cart_id = ? AND product_id = ?";
        int cnt;
        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, cartId.toString());
            prep.setString(2, productId.toString());
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                cnt = rs.getInt(1);
            } else {
                return false;
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            throw new Exception();
        }
        cnt += delta;
        if (cnt < 0) {
            return false;
        }
        if (cnt == 0) {
            sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        } else {
            sql = "UPDATE  cart_items SET cnt =  ? WHERE cart_id = ? AND product_id = ?";
        }
        try (PreparedStatement prep = connection.prepareStatement(sql)) {
            if (cnt == 0) {
                prep.setString(1, cartId.toString());
                prep.setString(2, productId.toString());
            } else {
                prep.setInt(1, cnt);
                prep.setString(2, cartId.toString());
                prep.setString(3, productId.toString());

            }
            prep.executeUpdate();
            return true;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            throw new Exception();
        }
    }
    public boolean close(UUID cartId, boolean isCanceled){
        String sql = "UPDATE carts SET close_dt = CURRENT_TIMESTAMP, isCanceled = ? WHERE cart_id = ?";
        try (PreparedStatement prep = connection.prepareStatement(sql) ){
            prep.setInt(1, isCanceled ? 1 : 0);
            prep.setString(2, cartId.toString());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex){
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
    }
}
