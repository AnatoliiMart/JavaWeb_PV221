package itstep.learning.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class RestService {
    public void sendRestError(HttpServletResponse resp, String msg) throws IOException {
        sendRestError(resp, 400, msg);
    }
    public void sendRestError(HttpServletResponse resp, int code, String msg) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(new RestReponseStatus(code));
        restResponse.setData(msg);
        sendRest(resp, restResponse);
    }

    public void sendRestResponse(HttpServletResponse resp, Object data) throws IOException {
        sendRestResponse(resp, 200, data);
    }
    public void sendRestResponse(HttpServletResponse resp, int code, Object data) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(new RestReponseStatus(code));
        restResponse.setData(data);
        sendRest(resp, restResponse);
    }
    public void sendRest(HttpServletResponse resp, RestResponse restResponse) throws IOException {
        sendRest(resp, restResponse, 0);
    }
    public void sendRest(HttpServletResponse resp, RestResponse restResponse, int maxAge) throws IOException {
        Gson gson = new GsonBuilder().serializeNulls().create();
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", maxAge == 0 ? "no-cache" : "max-age=" + maxAge);
        resp.getWriter().write(gson.toJson(restResponse));
    }
}
