package com.example.cachuelos.rest;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import com.example.cachuelosfrontend.model.Cachuelostate;

/**
 * 
 */
@Stateless
@Path("/cachuelostates")
public class CachuelostateEndpoint
{
   @PersistenceContext(unitName = "forge-default")
   private EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(Cachuelostate entity)
   {
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(CachuelostateEndpoint.class).path(String.valueOf(entity.getIdCachueloState())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Integer id)
   {
      Cachuelostate entity = em.find(Cachuelostate.class, id);
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
   public Response findById(@PathParam("id") Integer id)
   {
      TypedQuery<Cachuelostate> findByIdQuery = em.createQuery("SELECT DISTINCT c FROM Cachuelostate c  WHERE c.idCachueloState = :entityId ORDER BY c.idCachueloState", Cachuelostate.class);
      findByIdQuery.setParameter("entityId", id);
      Cachuelostate entity;
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
      return Response.ok(entity).build();
   }

   @GET
   @Produces("application/json")
   public List<Cachuelostate> listAll()
   {
      final List<Cachuelostate> results = em.createQuery("SELECT DISTINCT c FROM Cachuelostate c ORDER BY c.idCachueloState", Cachuelostate.class).getResultList();
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(Cachuelostate entity)
   {
      entity = em.merge(entity);
      return Response.noContent().build();
   }
}