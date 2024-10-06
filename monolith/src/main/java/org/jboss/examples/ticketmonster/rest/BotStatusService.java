package org.jboss.examples.ticketmonster.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.examples.ticketmonster.service.BotService;

import java.util.List;

/**
 * A non-RESTful service for providing the current state of the Bot. This service also allows the bot to be started, stopped or
 * the existing bookings to be deleted.
 * 
 * @author Vineet Reynolds
 * 
 */
@Path("/bot")
public class BotStatusService {

    @Inject
    BotService botService;

    /**
     * Produces a JSON representation of the bot's log, containing a maximum of 50 messages logged by the Bot.
     * 
     * @return The JSON representation of the Bot's log
     */
    @Path("messages")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getMessages() {
        return botService.fetchLog();
    }

    /**
     * Produces a representation of the bot's current state. This is a string - "RUNNING" or "NOT_RUNNING" depending on whether
     * the bot is active.
     * 
     * @return The represntation of the Bot's current state.
     */
    @Path("status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBotStatus() {
        BotState state = botService.isBotActive() ? BotState.RUNNING
            : BotState.NOT_RUNNING;
        return Response.ok(state).build();
    }

    /**
     * Updates the state of the Bot with the provided state. This may trigger the bot to start itself, stop itself, or stop and
     * delete all existing bookings.
     * 
     * @param updatedStatus The new state of the Bot. Only the state property is considered; any messages provided are ignored.
     * @return An empty HTTP 201 response.
     */
    @Path("status")
    @PUT
    public Response updateBotStatus(BotState updatedState) {
        if (updatedState.equals(BotState.RUNNING)) {
            botService.start();
        } else if (updatedState.equals(BotState.NOT_RUNNING)) {
            botService.stop();
        } else if (updatedState.equals(BotState.RESET)) {
            botService.deleteAll();
        }
        return Response.noContent().build();
    }

}
