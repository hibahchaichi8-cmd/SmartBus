package tn.smartbus.trip.exception;

public class ApiException extends RuntimeException {
  public final int status;

  public ApiException(int status, String message) {
    super(message);
    this.status = status;
  }

  public static ApiException missing(String type) {
    return new ApiException(404, type + " not found");
  }

  public static ApiException conflict(String message) {
    return new ApiException(409, message);
  }
}
