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

    public BookingResult processBooking(BookingRequest r) {
        if (r == null
                || r.getCustomerId() == null || r.getCustomerId().isBlank()
                || r.getEventId() == null || r.getEventId().isBlank()
                || r.getTicketQuantity() < 1 || r.getTicketQuantity() > 5
                || r.getPricePerTicketInCents() <= 0) {
            return BookingResult.of(BookingResult.Status.INVALID_REQUEST);
        }
        if (eventRepository.isSoldOut(r.getEventId())) {
            return BookingResult.of(BookingResult.Status.SOLD_OUT);
        }
        int total = r.getTicketQuantity() * r.getPricePerTicketInCents();
        try {
            String tx = paymentGateway.processPayment(r.getCustomerId(), total);
            if (tx == null || tx.isBlank()) {
    return BookingResult.of(BookingResult.Status.PAYMENT_FAILED);
}
            eventRepository.saveBooking(r);
            notificationService.sendBookingConfirmation(r.getCustomerId(), r.getEventId());
            return BookingResult.success(tx);
        } catch (PaymentFailedException e) {
            return BookingResult.of(BookingResult.Status.PAYMENT_FAILED);
        }
    }
}