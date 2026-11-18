/**
 * Add the assessed production classes for the Ticket Booking System here.
 */
package edu.course.booking;

public class BookingRequest {

    private String customerId;
    private String eventId;
    private int ticketQuantity;
    private int pricePerTicketInCents;
    public BookingRequest(String customerId, String eventId,
                          int ticketQuantity, int pricePerTicketInCents) {

        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID is required");
        }

        if (ticketQuantity < 1 || ticketQuantity > 5) {
            throw new IllegalArgumentException("Ticket quantity must be between 1 and 5");
        }

        if (pricePerTicketInCents <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        this.customerId = customerId;
        this.eventId = eventId;
        this.ticketQuantity = ticketQuantity;
        this.pricePerTicketInCents = pricePerTicketInCents;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getEventId() {
        return eventId;
    }

    public int getTicketQuantity() {
        return ticketQuantity;
    }

    public int getPricePerTicketInCents() {
        return pricePerTicketInCents;
    }
}
