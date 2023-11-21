package org.jboss.examples.ticketmonster.test.rest;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.examples.ticketmonster.model.Show;
import org.jboss.examples.ticketmonster.rest.ShowService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class ShowServiceTest {

    @Inject
    ShowService showService;
    
    @Test
    public void testGetShowById() {
        
        // Test loading a single show
        Show show = showService.getSingleInstance(1l);
        assertNotNull(show);
        assertEquals("Roy Thomson Hall", show.getVenue().getName());
        assertEquals("Rock concert of the decade", show.getEvent().getName());
        
    }
    
    @Test
    public void testPagination() {
        
        // Test pagination logic
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<String, String>();

        queryParameters.add("first", "2");
        queryParameters.add("maxResults", "1");
        
        List<Show> shows = showService.getAll(queryParameters);
        assertNotNull(shows);
        assertEquals(1, shows.size());
        assertEquals("Sydney Opera House", shows.get(0).getVenue().getName());
        assertEquals("Rock concert of the decade", shows.get(0).getEvent().getName());
    }
    
    @Test
    public void testGetShowsByVenue() {
        
        // Test getting shows by venue
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<java.lang.String, java.lang.String>();

        queryParameters.add("venue", "1");
        
        List<Show> shows = showService.getAll(queryParameters);
        assertNotNull(shows);
        assertEquals(2, shows.size());
        for (Show s: shows) {
            assertEquals("Roy Thomson Hall", s.getVenue().getName());
        }
    }
    
    @Test
    public void testGetShowsByEvent() {
        
        // Test getting shows by event
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<String, String>();

        queryParameters.add("event", "1");
        
        List<Show> shows = showService.getAll(queryParameters);
        assertNotNull(shows);
        assertEquals(3, shows.size());
        for (Show s: shows) {
            assertEquals("Rock concert of the decade", s.getEvent().getName());
        }
    }
    
    @Test
    public void testGetShowByPerformance() {
        
        // Test getting shows by performance
        Show show = showService.getShowByPerformance(1l);
        assertNotNull(show);
        assertEquals("Roy Thomson Hall", show.getVenue().getName());
        assertEquals("Rock concert of the decade", show.getEvent().getName());
    }

}
