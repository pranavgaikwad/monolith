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
import org.jboss.examples.ticketmonster.model.MediaItem;
import org.jboss.examples.ticketmonster.rest.dto.MediaItemDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
@ApplicationScoped
@Path("/mediaitems")
public class MediaItemEndpoint
{
   @PersistenceContext
   EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(MediaItemDTO dto)
   {
      MediaItem entity = dto.fromDTO(null, em);
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(MediaItemEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id)
   {
      MediaItem entity = em.find(MediaItem.class, id);
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
      TypedQuery<MediaItem> findByIdQuery = em.createQuery("SELECT DISTINCT m FROM MediaItem m WHERE m.id = :entityId ORDER BY m.id", MediaItem.class);
      findByIdQuery.setParameter("entityId", id);
      MediaItem entity;
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
      MediaItemDTO dto = new MediaItemDTO(entity);
      return Response.ok(dto).build();
   }

   @GET
   @Produces("application/json")
   public List<MediaItemDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
   {
      TypedQuery<MediaItem> findAllQuery = em.createQuery("SELECT DISTINCT m FROM MediaItem m ORDER BY m.id", MediaItem.class);
      if (startPosition != null)
      {
         findAllQuery.setFirstResult(startPosition);
      }
      if (maxResult != null)
      {
         findAllQuery.setMaxResults(maxResult);
      }
      final List<MediaItem> searchResults = findAllQuery.getResultList();
      final List<MediaItemDTO> results = new ArrayList<MediaItemDTO>();
      for (MediaItem searchResult : searchResults)
      {
         MediaItemDTO dto = new MediaItemDTO(searchResult);
         results.add(dto);
      }
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(@PathParam("id") Long id, MediaItemDTO dto)
   {
      TypedQuery<MediaItem> findByIdQuery = em.createQuery("SELECT DISTINCT m FROM MediaItem m WHERE m.id = :entityId ORDER BY m.id", MediaItem.class);
      findByIdQuery.setParameter("entityId", id);
      MediaItem entity;
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
