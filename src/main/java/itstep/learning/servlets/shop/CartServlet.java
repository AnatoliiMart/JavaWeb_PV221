package itstep.learning.servlets.shop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.stream.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
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
        super.service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getAttribute("Claim.Sid").toString();
        if (userId == null) {
            super.sendRest(401, "Auth token required");
            return;
        }
        if (!req.getContentType().startsWith("application/json")) {
            super.sendRest(415, "application/json expected");
            return;
        }
        String jsonString;
        try {
            jsonString = stringReader.read(req.getInputStream());
        } catch (IOException e) {
            logger.warning(e.getMessage());
            super.sendRest(400, "JSON could not be extracted");
            return;
        }
        JsonElement json;
        try {
            json = gson.fromJson(jsonString, JsonElement.class);
        } catch (JsonSyntaxException e) {
            super.sendRest(400, "JSON could not be parsed");
            return;
        }
        if (!json.isJsonObject()) {
            super.sendRest(422, "JSON root must be an object");
            return;
        }
        JsonElement element = json.getAsJsonObject().get("userId");
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
        if (cartDao.add(UUID.fromString(userId), cartProductId, 1))
            super.sendRest(201, jsonString);
        else
            super.sendRest(500, "See Server log for details");

    }
}
