package edu.course.booking;
public class BookingResult{

public enum Status { SUCCESS, INVALID_REQUEST, SOLD_OUT, PAYMENT_FAILED }
    private final Status status;
    private final String transactionId;

    private BookingResult(Status status, String transactionId) {
        this.status = status;
        this.transactionId = transactionId;
    }
    public static BookingResult success(String tx) { return new BookingResult(Status.SUCCESS, tx); }
    public static BookingResult of(Status s) { return new BookingResult(s, null); }
    public Status getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
}


