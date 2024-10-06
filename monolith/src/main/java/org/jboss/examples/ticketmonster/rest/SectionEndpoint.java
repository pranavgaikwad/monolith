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
import org.jboss.examples.ticketmonster.model.Section;
import org.jboss.examples.ticketmonster.rest.dto.SectionDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
@ApplicationScoped
@Path("/sections")
public class SectionEndpoint
{
   @PersistenceContext
   EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(SectionDTO dto)
   {
      Section entity = dto.fromDTO(null, em);
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(SectionEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id)
   {
      Section entity = em.find(Section.class, id);
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
      TypedQuery<Section> findByIdQuery = em.createQuery("SELECT DISTINCT s FROM Section s LEFT JOIN FETCH s.venue WHERE s.id = :entityId ORDER BY s.id", Section.class);
      findByIdQuery.setParameter("entityId", id);
      Section entity;
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
      SectionDTO dto = new SectionDTO(entity);
      return Response.ok(dto).build();
   }

   @GET
   @Produces("application/json")
   public List<SectionDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
   {
      TypedQuery<Section> findAllQuery = em.createQuery("SELECT DISTINCT s FROM Section s LEFT JOIN FETCH s.venue ORDER BY s.id", Section.class);
      if (startPosition != null)
      {
         findAllQuery.setFirstResult(startPosition);
      }
      if (maxResult != null)
      {
         findAllQuery.setMaxResults(maxResult);
      }
      final List<Section> searchResults = findAllQuery.getResultList();
      final List<SectionDTO> results = new ArrayList<SectionDTO>();
      for (Section searchResult : searchResults)
      {
         SectionDTO dto = new SectionDTO(searchResult);
         results.add(dto);
      }
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(@PathParam("id") Long id, SectionDTO dto)
   {
      TypedQuery<Section> findByIdQuery = em.createQuery("SELECT DISTINCT s FROM Section s LEFT JOIN FETCH s.venue WHERE s.id = :entityId ORDER BY s.id", Section.class);
      findByIdQuery.setParameter("entityId", id);
      Section entity;
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
