package edu.course.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingManagerTest {

    private IPaymentGateway gateway;
    private INotificationService notifier;
    private IEventRepository repo;
    private BookingManager manager;

    @BeforeEach
    void setUp() {
        gateway = mock(IPaymentGateway.class);
        notifier = mock(INotificationService.class);
        repo = mock(IEventRepository.class);
        manager = new BookingManager(gateway, notifier, repo);
    }

    @Test
    void successfulBooking() throws Exception {
        BookingRequest req = new BookingRequest("C1", "E1", 2, 2500);
        when(repo.isSoldOut("E1")).thenReturn(false);
        when(gateway.processPayment("C1", 5000)).thenReturn("TX-123");

        BookingResult result = manager.processBooking(req);

        assertEquals(BookingResult.Status.SUCCESS, result.getStatus());
        assertEquals("TX-123", result.getTransactionId());
        verify(repo, times(1)).isSoldOut("E1");
        verify(gateway, times(1)).processPayment("C1", 5000);
        verify(repo, times(1)).saveBooking(req);
        verify(notifier, times(1)).sendBookingConfirmation("C1", "E1");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "null,E1,2,2500",
        "'',E1,2,2500",
        "'   ',E1,2,2500",
        "C1,null,2,2500",
        "C1,'',2,2500",
        "C1,'   ',2,2500",
        "C1,E1,0,2500",
        "C1,E1,6,2500",
        "C1,E1,2,0",
        "C1,E1,2,-100"
    }, nullValues = "null")
    void invalidRequestIsRejectedBeforeAnyDependency(
            String customerId, String eventId, int qty, int price) {
        BookingRequest req = new BookingRequest(customerId, eventId, qty, price);

        BookingResult result = manager.processBooking(req);

        assertEquals(BookingResult.Status.INVALID_REQUEST, result.getStatus());
        verifyNoInteractions(repo, gateway, notifier);
    }

    @Test
    void soldOutEvent() throws Exception {
        BookingRequest req = new BookingRequest("C1", "E1", 2, 2500);
        when(repo.isSoldOut("E1")).thenReturn(true);

        BookingResult result = manager.processBooking(req);

        assertEquals(BookingResult.Status.SOLD_OUT, result.getStatus());
        verify(repo, times(1)).isSoldOut("E1");
        verify(gateway, never()).processPayment(anyString(), anyInt());
        verify(repo, never()).saveBooking(any());
        verify(notifier, never()).sendBookingConfirmation(anyString(), anyString());
    }

    @Test
    void failedPayment() throws Exception {
        BookingRequest req = new BookingRequest("C1", "E1", 2, 2500);
        when(repo.isSoldOut("E1")).thenReturn(false);
        when(gateway.processPayment("C1", 5000))
                .thenThrow(new PaymentFailedException("declined"));

        BookingResult result = manager.processBooking(req);

        assertEquals(BookingResult.Status.PAYMENT_FAILED, result.getStatus());
        verify(repo, times(1)).isSoldOut("E1");
        verify(gateway, times(1)).processPayment("C1", 5000);
        verify(repo, never()).saveBooking(any());
        verify(notifier, never()).sendBookingConfirmation(anyString(), anyString());
    }
    @ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"   "})
void blankTransactionIdIsPaymentFailure(String txId) throws Exception {
    BookingRequest req = new BookingRequest("C1", "E1", 2, 2500);
    when(repo.isSoldOut("E1")).thenReturn(false);
    when(gateway.processPayment("C1", 5000)).thenReturn(txId);

    BookingResult result = manager.processBooking(req);

    assertEquals(BookingResult.Status.PAYMENT_FAILED, result.getStatus());
    verify(repo, never()).saveBooking(any());
    verify(notifier, never()).sendBookingConfirmation(anyString(), anyString());
}
@Test
void notificationFailureDoesNotFailBooking() throws Exception {
    BookingRequest req = new BookingRequest("C1", "E1", 2, 2500);
    when(repo.isSoldOut("E1")).thenReturn(false);
    when(gateway.processPayment("C1", 5000)).thenReturn("TX-123");
    doThrow(new RuntimeException("smtp down"))
        .when(notifier).sendBookingConfirmation("C1", "E1");

    BookingResult result = manager.processBooking(req);

    assertEquals(BookingResult.Status.SUCCESS, result.getStatus());
    assertEquals("TX-123", result.getTransactionId());
    verify(repo, times(1)).saveBooking(req);
}
}