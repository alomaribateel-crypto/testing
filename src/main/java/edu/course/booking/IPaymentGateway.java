
package edu.course.booking;

public interface IPaymentGateway {

    String processPayment(String customerId, int amountInCents)
    throws PaymentFailedException;

}

