package edu.course.booking;

public class BookingManager {

    private IPaymentGateway paymentGateway;

    public BookingManager(IPaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

}