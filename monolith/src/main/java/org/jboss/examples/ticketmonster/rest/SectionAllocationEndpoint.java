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
import org.jboss.examples.ticketmonster.model.SectionAllocation;
import org.jboss.examples.ticketmonster.rest.dto.SectionAllocationDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
@ApplicationScoped
@Path("/sectionallocations")
public class SectionAllocationEndpoint
{
   @PersistenceContext
   EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(SectionAllocationDTO dto)
   {
      SectionAllocation entity = dto.fromDTO(null, em);
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(SectionAllocationEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Long id)
   {
      SectionAllocation entity = em.find(SectionAllocation.class, id);
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
      TypedQuery<SectionAllocation> findByIdQuery = em.createQuery("SELECT DISTINCT s FROM SectionAllocation s LEFT JOIN FETCH s.performance LEFT JOIN FETCH s.section WHERE s.id = :entityId ORDER BY s.id", SectionAllocation.class);
      findByIdQuery.setParameter("entityId", id);
      SectionAllocation entity;
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
      SectionAllocationDTO dto = new SectionAllocationDTO(entity);
      return Response.ok(dto).build();
   }

   @GET
   @Produces("application/json")
   public List<SectionAllocationDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
   {
      TypedQuery<SectionAllocation> findAllQuery = em.createQuery("SELECT DISTINCT s FROM SectionAllocation s LEFT JOIN FETCH s.performance LEFT JOIN FETCH s.section ORDER BY s.id", SectionAllocation.class);
      if (startPosition != null)
      {
         findAllQuery.setFirstResult(startPosition);
      }
      if (maxResult != null)
      {
         findAllQuery.setMaxResults(maxResult);
      }
      final List<SectionAllocation> searchResults = findAllQuery.getResultList();
      final List<SectionAllocationDTO> results = new ArrayList<SectionAllocationDTO>();
      for (SectionAllocation searchResult : searchResults)
      {
         SectionAllocationDTO dto = new SectionAllocationDTO(searchResult);
         results.add(dto);
      }
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(@PathParam("id") Long id, SectionAllocationDTO dto)
   {
      TypedQuery<SectionAllocation> findByIdQuery = em.createQuery("SELECT DISTINCT s FROM SectionAllocation s LEFT JOIN FETCH s.performance LEFT JOIN FETCH s.section WHERE s.id = :entityId ORDER BY s.id", SectionAllocation.class);
      findByIdQuery.setParameter("entityId", id);
      SectionAllocation entity;
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
