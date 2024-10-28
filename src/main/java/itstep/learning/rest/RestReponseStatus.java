package itstep.learning.rest;

public class RestReponseStatus {
    private int code;
    private String phrase;
    private boolean isSuccessful;

    public RestReponseStatus() {
    }

    public RestReponseStatus(int code) {
        this.setCode(code);
        switch (code) {
            case 200:
                this.isSuccessful = true;
                this.phrase = "Ok";
                break;
            case 201:
                this.isSuccessful = true;
                this.phrase = "Created";
                break;
            case 202:
                this.isSuccessful = true;
                this.phrase = "Accepted";
                break;
            case 401:
                this.isSuccessful = false;
                this.phrase = "Unauthorized";
                break;
            case 403:
                this.isSuccessful = false;
                this.phrase = "Forbidden";
                break;
            case 404:
                this.isSuccessful = false;
                this.phrase = "Not Found";
                break;
            case 409:
                this.isSuccessful = false;
                this.phrase = "Conflict";
                break;
            case 415:
                this.isSuccessful = false;
                this.phrase = "Unsupported Media Type";
                break;
            case 422:
                this.isSuccessful = false;
                this.phrase = "Unprocessable Entity";
                break;
            case 500:
                this.isSuccessful = false;
                this.phrase = "Internal Server Error";
                break;
            case 501:
                this.isSuccessful = false;
                this.phrase = "Not Acceptable";
                break;
            default:
                this.isSuccessful = false;
                this.phrase = "Bad Request";
                break;
        }
    }

    public int getCode() {
        return code;
    }

    public RestReponseStatus setCode(int code) {
        this.code = code;
        return this;
    }

    public String getPhrase() {
        return phrase;
    }

    public RestReponseStatus setPhrase(String phrase) {
        this.phrase = phrase;
        return this;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public RestReponseStatus setSuccessful(boolean successful) {
        isSuccessful = successful;
        return this;
    }
}
