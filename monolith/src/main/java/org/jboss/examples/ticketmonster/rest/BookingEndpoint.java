package org.jboss.examples.ticketmonster.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.examples.ticketmonster.model.Booking;
import org.jboss.examples.ticketmonster.rest.dto.BookingDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
@ApplicationScoped
@Path("forge/bookings")
public class BookingEndpoint
{
   @PersistenceContext
   private EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(BookingDTO dto)
   {
      Booking entity = dto.fromDTO(null, em);
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(BookingEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id)
   {
      Booking entity = em.find(Booking.class, id);
      if (entity == null)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      em.remove(entity);
      return Response.noContent().build();
   }

   @GET
   @Path("/{id:[0-9][0-9]*}")
   @Produces("application/json")
   public Response findById(@PathParam("id") Long id)
   {
      TypedQuery<Booking> findByIdQuery = em.createQuery("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.tickets LEFT JOIN FETCH b.performance WHERE b.id = :entityId ORDER BY b.id", Booking.class);
      findByIdQuery.setParameter("entityId", id);
      Booking entity;
      try
      {
         entity = findByIdQuery.getSingleResult();
      }
      catch (NoResultException nre)
      {
         entity = null;
      }
      if (entity == null)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      BookingDTO dto = new BookingDTO(entity);
      return Response.ok(dto).build();
   }

   @GET
   @Produces("application/json")
   public List<BookingDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
   {
      TypedQuery<Booking> findAllQuery = em.createQuery("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.tickets LEFT JOIN FETCH b.performance ORDER BY b.id", Booking.class);
      if (startPosition != null)
      {
         findAllQuery.setFirstResult(startPosition);
      }
      if (maxResult != null)
      {
         findAllQuery.setMaxResults(maxResult);
      }
      final List<Booking> searchResults = findAllQuery.getResultList();
      final List<BookingDTO> results = new ArrayList<BookingDTO>();
      for (Booking searchResult : searchResults)
      {
         BookingDTO dto = new BookingDTO(searchResult);
         results.add(dto);
      }
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(@PathParam("id") Long id, BookingDTO dto)
   {
      TypedQuery<Booking> findByIdQuery = em.createQuery("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.tickets LEFT JOIN FETCH b.performance WHERE b.id = :entityId ORDER BY b.id", Booking.class);
      findByIdQuery.setParameter("entityId", id);
      Booking entity;
      try
      {
         entity = findByIdQuery.getSingleResult();
      }
      catch (NoResultException nre)
      {
         entity = null;
      }
      entity = dto.fromDTO(entity, em);
      try
      {
         entity = em.merge(entity);
      }
      catch (OptimisticLockException e)
      {
         return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
      }
      return Response.noContent().build();
   }
}
