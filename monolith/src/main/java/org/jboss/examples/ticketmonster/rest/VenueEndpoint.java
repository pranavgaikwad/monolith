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
import org.jboss.examples.ticketmonster.model.Venue;
import org.jboss.examples.ticketmonster.rest.dto.VenueDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
@ApplicationScoped
@Path("forge/venues")
public class VenueEndpoint
{
   @PersistenceContext
   EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(VenueDTO dto)
   {
      Venue entity = dto.fromDTO(null, em);
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(VenueEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id)
   {
      Venue entity = em.find(Venue.class, id);
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
      TypedQuery<Venue> findByIdQuery = em.createQuery("SELECT DISTINCT v FROM Venue v LEFT JOIN FETCH v.sections LEFT JOIN FETCH v.mediaItem WHERE v.id = :entityId ORDER BY v.id", Venue.class);
      findByIdQuery.setParameter("entityId", id);
      Venue entity;
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
      VenueDTO dto = new VenueDTO(entity);
      return Response.ok(dto).build();
   }

   @GET
   @Produces("application/json")
   public List<VenueDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
   {
      TypedQuery<Venue> findAllQuery = em.createQuery("SELECT DISTINCT v FROM Venue v LEFT JOIN FETCH v.sections LEFT JOIN FETCH v.mediaItem ORDER BY v.id", Venue.class);
      if (startPosition != null)
      {
         findAllQuery.setFirstResult(startPosition);
      }
      if (maxResult != null)
      {
         findAllQuery.setMaxResults(maxResult);
      }
      final List<Venue> searchResults = findAllQuery.getResultList();
      final List<VenueDTO> results = new ArrayList<VenueDTO>();
      for (Venue searchResult : searchResults)
      {
         VenueDTO dto = new VenueDTO(searchResult);
         results.add(dto);
      }
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(@PathParam("id") Long id, VenueDTO dto)
   {
      TypedQuery<Venue> findByIdQuery = em.createQuery("SELECT DISTINCT v FROM Venue v LEFT JOIN FETCH v.sections LEFT JOIN FETCH v.mediaItem WHERE v.id = :entityId ORDER BY v.id", Venue.class);
      findByIdQuery.setParameter("entityId", id);
      Venue entity;
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
