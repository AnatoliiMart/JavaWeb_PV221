package itstep.learning.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

public class RestServlet extends HttpServlet {
    protected RestResponse restResponse;
    private HttpServletResponse resp;
    private HttpServletRequest req;
    protected final static Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.resp = resp;
        this.req = req;
        if (this.restResponse == null) {
            this.restResponse = new RestResponse();
        }
        super.service(req, resp);
    }

    protected void sendRest(int statusCode, Object data) throws IOException {
        restResponse.setStatus(statusCode).setData(data);
        switch (req.getServletPath()) {
            case "/shop/category":
                sendRest(60 * 60 * 24);
                return;
            case "/shop/product":
                sendRest(60 * 60);
                break;
            default:
                sendRest(0);
                break;
        }
    }

    protected void sendRest(int statusCode, Object data, int maxAge) throws IOException {
        restResponse.setStatus(statusCode).setData(data);
        sendRest(maxAge);
    }


    protected void sendRest(int maxAge) throws IOException {
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", maxAge == 0 ? "no-cache" : "max-age=" + maxAge);
        resp.getWriter().print(gson.toJson(restResponse));
    }
}
