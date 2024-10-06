package org.jboss.examples.ticketmonster.test.rest;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.examples.ticketmonster.model.Booking;
import org.jboss.examples.ticketmonster.model.Performance;
import org.jboss.examples.ticketmonster.model.Show;
import org.jboss.examples.ticketmonster.model.Ticket;
import org.jboss.examples.ticketmonster.model.TicketPrice;
import org.jboss.examples.ticketmonster.rest.BookingRequest;
import org.jboss.examples.ticketmonster.rest.BookingService;
import org.jboss.examples.ticketmonster.rest.ShowService;
import org.jboss.examples.ticketmonster.rest.TicketRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingServiceTest {

    @Inject
    BookingService bookingService;

    @Inject
    ShowService showService;

    @Test
    @Order(1)
    public void testCreateBookings() {
        BookingRequest br = createBookingRequest(1l, 0, new int[]{4, 1}, new int[]{1,1}, new int[]{3,1});
        bookingService.createBooking(br);

        BookingRequest br2 = createBookingRequest(2l, 1, new int[]{6,1}, new int[]{8,2}, new int[]{10,2});
        bookingService.createBooking(br2);

        BookingRequest br3 = createBookingRequest(3l, 0, new int[]{4,1}, new int[]{2,1});
        bookingService.createBooking(br3);
    }

    @Test
    @Order(10)
    public void testGetBookings() {
        checkBooking1();
        checkBooking2();
        checkBooking3();
    }
    
    private void checkBooking1() {
        Booking booking = bookingService.getSingleInstance(1l);
        assertNotNull(booking);
        assertEquals("Roy Thomson Hall", booking.getPerformance().getShow().getVenue().getName());
        assertEquals("Rock concert of the decade", booking.getPerformance().getShow().getEvent().getName());
        assertEquals("bob@acme.com", booking.getContactEmail());

        // Test the ticket requests created

        assertEquals(3 + 2 + 1, booking.getTickets().size());

        List<String> requiredTickets = new ArrayList<String>();
        requiredTickets.add("A @ 219.5 (Adult)");
        requiredTickets.add("A @ 219.5 (Adult)");
        requiredTickets.add("D @ 149.5 (Adult)");
        requiredTickets.add("C @ 179.5 (Adult)");
        requiredTickets.add("C @ 179.5 (Adult)");
        requiredTickets.add("C @ 179.5 (Adult)");

        checkTickets(requiredTickets, booking);
    }
    
    private void checkBooking2() {
        Booking booking = bookingService.getSingleInstance(2l);
        assertNotNull(booking);
        assertEquals("Sydney Opera House", booking.getPerformance().getShow().getVenue().getName());
        assertEquals("Rock concert of the decade", booking.getPerformance().getShow().getEvent().getName());
        assertEquals("bob@acme.com", booking.getContactEmail());

        assertEquals(3 + 2 + 1, booking.getTickets().size());

        List<String> requiredTickets = new ArrayList<String>();
        requiredTickets.add("S2 @ 197.75 (Adult)");
        requiredTickets.add("S6 @ 145.0 (Child 0-14yrs)");
        requiredTickets.add("S6 @ 145.0 (Child 0-14yrs)");
        requiredTickets.add("S4 @ 145.0 (Child 0-14yrs)");
        requiredTickets.add("S6 @ 145.0 (Child 0-14yrs)");
        requiredTickets.add("S4 @ 145.0 (Child 0-14yrs)");

        checkTickets(requiredTickets, booking);
    }
    
    private void checkBooking3() {
        Booking booking = bookingService.getSingleInstance(3l);
        assertNotNull(booking);
        assertEquals("Roy Thomson Hall", booking.getPerformance().getShow().getVenue().getName());
        assertEquals("Shane's Sock Puppets", booking.getPerformance().getShow().getEvent().getName());
        assertEquals("bob@acme.com", booking.getContactEmail());

        assertEquals(2 + 1, booking.getTickets().size());

        List<String> requiredTickets = new ArrayList<String>();
        requiredTickets.add("B @ 199.5 (Adult)");
        requiredTickets.add("D @ 149.5 (Adult)");
        requiredTickets.add("B @ 199.5 (Adult)");
        
        checkTickets(requiredTickets, booking);
    }

    @Test
    @Order(10)
    public void testPagination() {

        // Test pagination logic
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<String, String>();

        queryParameters.add("first", "2");
        queryParameters.add("maxResults", "1");

        List<Booking> bookings = bookingService.getAll(queryParameters);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals("Sydney Opera House", bookings.get(0).getPerformance().getShow().getVenue().getName());
        assertEquals("Rock concert of the decade", bookings.get(0).getPerformance().getShow().getEvent().getName());
    }

    @Test
    @Order(20)
    public void testDelete() {
        bookingService.deleteBooking(2l);
        checkBooking1();
        checkBooking3();
        try {
            bookingService.getSingleInstance(2l);
        } catch (Exception e) {
            if (e.getCause() instanceof NoResultException) {
                return;
            }
        }
        fail("Expected NoResultException did not occur.");
    }

    private BookingRequest createBookingRequest(Long showId, int performanceNo, int[]... sectionAndCategories) {
        Show show = showService.getSingleInstance(showId);

        Performance performance = new ArrayList<Performance>(show.getPerformances()).get(performanceNo);

        BookingRequest bookingRequest = new BookingRequest(performance, "bob@acme.com");

        List<TicketPrice> possibleTicketPrices = new ArrayList<TicketPrice>(show.getTicketPrices());
        int i = 1;
        for (int[] sectionAndCategory : sectionAndCategories) {
            for (TicketPrice ticketPrice : possibleTicketPrices) {
                int sectionId = sectionAndCategory[0];
                int categoryId = sectionAndCategory[1];
                if(ticketPrice.getSection().getId() == sectionId && ticketPrice.getTicketCategory().getId() == categoryId) {
                    bookingRequest.addTicketRequest(new TicketRequest(ticketPrice, i));
                    i++;
                    break;
                }
            }
        }

        return bookingRequest;
    }
    
    private void checkTickets(List<String> requiredTickets, Booking booking) {
        List<String> bookedTickets = new ArrayList<String>();
        for (Ticket t : booking.getTickets()) {
            bookedTickets.add(new StringBuilder().append(t.getSeat().getSection()).append(" @ ").append(t.getPrice()).append(" (").append(t.getTicketCategory()).append(")").toString());
        }
        System.out.println(bookedTickets);
        for (String requiredTicket : requiredTickets) {
            assertTrue(bookedTickets.contains(requiredTicket), "Required ticket not present: " + requiredTicket);
        }
    }

}
