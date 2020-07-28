package services;

public class Result {

    public enum Status {
        STATUS_OK,
        STATUS_FAIL
    }

    public final Status status;
    public final String result;
    public Result(Status status, String result){
        this.status = status;
        this.result = result;
    }
}
