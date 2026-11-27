package edu.course.booking;

public class BookingManager {
    private final IPaymentGateway paymentGateway;
    private final INotificationService notificationService;
    private final IEventRepository eventRepository;

    public BookingManager(IPaymentGateway paymentGateway,
                          INotificationService notificationService,
                          IEventRepository eventRepository) {
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
    }
    private boolean isValid(BookingRequest r) {
    return r != null
            && r.getCustomerId() != null && !r.getCustomerId().isBlank()
            && r.getEventId() != null && !r.getEventId().isBlank()
            && r.getTicketQuantity() >= 1 && r.getTicketQuantity() <= 5
            && r.getPricePerTicketInCents() > 0;
}


    public BookingResult processBooking(BookingRequest r) {
       if (!isValid(r)) {
    return BookingResult.of(BookingResult.Status.INVALID_REQUEST);
}
        if (eventRepository.isSoldOut(r.getEventId())) {
            return BookingResult.of(BookingResult.Status.SOLD_OUT);
        }
int total = calculateTotal(r);
        try {
            String tx = paymentGateway.processPayment(r.getCustomerId(), total);
            if (tx == null || tx.isBlank()) {
    return BookingResult.of(BookingResult.Status.PAYMENT_FAILED);
}
            eventRepository.saveBooking(r);
  try {
                notificationService.sendBookingConfirmation(r.getCustomerId(), r.getEventId());
            } catch (RuntimeException e) {
                // booking is already paid and saved; notification failure must not undo it
            }
            return BookingResult.success(tx);
        } catch (PaymentFailedException e) {
            return BookingResult.of(BookingResult.Status.PAYMENT_FAILED);
        }
    }
    private int calculateTotal(BookingRequest r) {
        return r.getTicketQuantity() * r.getPricePerTicketInCents();
    }
}