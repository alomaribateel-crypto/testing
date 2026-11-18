package edu.course.booking;

public interface IEventRepository {

    boolean hasAvailableTickets(String eventId, int quantity);

    void recordBooking(String eventId, int quantity);

}

