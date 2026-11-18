
package edu.course.booking;

public interface IPaymentGateway {

    boolean processPayment(String customerId, int amountInCents);

}

