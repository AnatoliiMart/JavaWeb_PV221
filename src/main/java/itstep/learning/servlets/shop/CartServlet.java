package itstep.learning.servlets.shop;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.stream.StringReader;
import jdk.nashorn.internal.codegen.types.NumericType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

@Singleton
public class CartServlet extends RestServlet {
    private final CartDao cartDao;
    private final StringReader stringReader;
    private final Logger logger;

    @Inject
    public CartServlet(CartDao cartDao, StringReader stringReader, Logger logger) {
        this.cartDao = cartDao;
        this.stringReader = stringReader;
        this.logger = logger;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse().setMeta(
                new RestMetaData()
                        .setUri("/shop/cart")
                        .setMethod(req.getMethod())
                        .setLocale("uk-UA")
                        .setServerTime(new Date())
                        .setName("Shop Cart API")
                        .setAcceptedMethods(new String[]{"GET", "POST", "PUT", "DELETE"})
        );
        if(req.getMethod().equals("PATCH")) {
            this.doPatch(req, resp);
        }
        else{
            super.service(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getAttribute("Claim.Sid").toString();
        if (userId == null) {
            super.sendRest(401, "Auth token required");
            return;
        }

        JsonObject json;
        try {
            json = parseBodyAsObject(req);
        } catch (ParseException e) {
            super.sendRest(e.getErrorOffset(), e.getMessage());
            return;
        }
        if (!json.isJsonObject()) {
            super.sendRest(422, "JSON root must be an object");
            return;
        }
        JsonElement element = json.get("userId");
        if (element == null) {
            super.sendRest(422, "JSON must have 'userId' field");
            return;
        }
        String cartUserId = json.getAsJsonObject().get("userId").getAsString();
        if (!userId.equals(cartUserId)) {
            super.sendRest(403, "Authorization mismatch");
        }
        element = json.getAsJsonObject().get("productId");
        if (element == null) {
            super.sendRest(422, "JSON must have 'productId' field");
            return;
        }
        UUID cartProductId;
        try {
            cartProductId = UUID.fromString(element.getAsString());
        } catch (IllegalArgumentException e) {
            super.sendRest(422, "'productId' field must be a valid UUID");
            return;
        }
        element = json.getAsJsonObject().get("count");
        int count;
        if (element == null) {
            count = 1;
        }
        else{
            try {
                count = element.getAsInt();
                if (count <= 0) {
                    super.sendRest(403, "'count' field must be a positive integer" );
                    return;
                }
            }
            catch (NumberFormatException e) {
                super.sendRest(403, "'count' field must be an integer");
                return;
            }
        }
        if (cartDao.add(UUID.fromString(userId), cartProductId, count))
            super.sendRest(201, userId);
        else
            super.sendRest(500, "See Server log for details");

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getAttribute("Claim.Sid");
        if (userId == null) {
            super.sendRest(401, "Auth token required");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        super.restResponse.getMeta().setParams(params);
        sendRest(200, cartDao.getCart(userId));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject json;
        try {
            json = parseBodyAsObject(req);
        } catch (ParseException e) {
            super.sendRest(e.getErrorOffset(), e.getMessage());
            return;
        }
        if (!json.isJsonObject()) {
            super.sendRest(422, "JSON root must be an object");
            return;
        }
        JsonElement element = json.get("cartId");
        if (element == null) {
            super.sendRest(422, "JSON must have 'cartId' field");
            return;
        }
        String str = json.getAsJsonObject().get("cartId").getAsString();
        UUID cartUuid;
        try {
            cartUuid = UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            super.sendRest(422, "'cartId' field must be a valid UUID");
            return;
        }
        element = json.get("cartId");
        if (element == null) {
            super.sendRest(422, "JSON must have 'cartId' field");
            return;
        }
        str = json.getAsJsonObject().get("productId").getAsString();
        UUID productUuid;
        try {
            productUuid = UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            super.sendRest(422, "'productId' field must be a valid UUID");
            return;
        }
        element = json.get("delta");
        if (element == null) {
            super.sendRest(422, "JSON must have 'delta' field");
            return;
        }
        int delta = element.getAsInt();
        try {
            if (cartDao.update(cartUuid, productUuid, delta)) {
                super.sendRest(200, "Updated");
            } else {
                super.sendRest(409, "Update failed");
            }
        } catch (Exception ignored) {
            super.sendRest(500, "See Server log for details");
        }
    }
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UUID cartId;
        try {
            cartId = UUID.fromString(req.getParameter("cart-id"));
        }
        catch (IllegalArgumentException e) {
            super.sendRest(400, "'cart-id' parameter must be a valid UUID");
            return;
        }
        boolean isCanceled = req.getParameter("is-canceled").equals("Y");
        super.sendRest(202, cartDao.close(cartId, isCanceled));
    }

    private JsonObject parseBodyAsObject(HttpServletRequest request) throws ParseException {
        JsonElement json = parseBody(request);
        if (!json.isJsonObject()) {
            throw new ParseException("JSON root must be an object", 422);

        }
        return json.getAsJsonObject();
    }

    private JsonElement parseBody(HttpServletRequest request) throws ParseException {
        if (!request.getContentType().startsWith("application/json")) {
            throw new ParseException("application/json expected", 400);

        }
        String jsonString;
        try {
            jsonString = stringReader.read(request.getInputStream());
        } catch (IOException e) {
            logger.warning(e.getMessage());
            throw new ParseException("JSON could not be extracted", 400);
        }
        JsonElement json;
        try {
            json = gson.fromJson(jsonString, JsonElement.class);
        } catch (JsonSyntaxException e) {
            throw new ParseException("JSON could not be parsed", 400);
        }
        return json;
    }
}
