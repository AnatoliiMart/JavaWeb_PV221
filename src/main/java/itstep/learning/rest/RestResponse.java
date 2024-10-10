package itstep.learning.rest;

public class RestResponse {
    private RestReponseStatus status;
    private RestMetaData meta;
    private Object data;


    public RestMetaData getMeta() {
        return meta;
    }

    public void setMeta(RestMetaData metaData) {
        this.meta = metaData;
    }

    public RestReponseStatus getStatus() {
        return status;
    }

    public RestResponse setStatus(RestReponseStatus status) {
        this.status = status;
        return this;
    }

    public RestResponse setStatus(int code) {
        return this.setStatus(new RestReponseStatus(code));
    }

    public Object getData() {
        return data;
    }

    public RestResponse setData(Object data) {
        this.data = data;
        return this;
    }
}
