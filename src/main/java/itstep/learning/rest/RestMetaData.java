package itstep.learning.rest;

import java.util.Date;
import java.util.Map;

public class RestMetaData {
    public String getUri() {
        return uri;
    }

    public RestMetaData setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public RestMetaData setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getName() {
        return name;
    }

    public RestMetaData setName(String name) {
        this.name = name;
        return this;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public RestMetaData setServerTime(Date serverTime) {
        this.serverTime = serverTime;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public RestMetaData setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public String getLocale() {
        return locale;
    }

    public RestMetaData setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public String[] getAcceptedMethods() {
        return acceptedMethods;
    }

    public RestMetaData setAcceptedMethods(String[] acceptedMethods) {
        this.acceptedMethods = acceptedMethods;
        return this;
    }

    private String uri;
    private String method;
    private String name;
    private Date serverTime;
    private Map<String,Object> params;
    private String locale;
    private String[] acceptedMethods;

}
/*
    {
        ...
        meta:{
            uri: "/shop/product",
            method: "GET",
            name: "Product list",
            serverTime: 123134244567,
            params: {
                productId: 123456-343...,
            },
            locale: "UK-UA",
            acceptMethods: ["GET","POST","PUT","DELETE"]
        }
    }
*/
