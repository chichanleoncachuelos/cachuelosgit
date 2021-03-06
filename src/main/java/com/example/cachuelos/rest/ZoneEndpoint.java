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
import com.example.cachuelosfrontend.model.Zone;

/**
 * 
 */
@Stateless
@Path("/zones")
public class ZoneEndpoint
{
   @PersistenceContext(unitName = "forge-default")
   private EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(Zone entity)
   {
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(ZoneEndpoint.class).path(String.valueOf(entity.getIdZone())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Integer id)
   {
      Zone entity = em.find(Zone.class, id);
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
      TypedQuery<Zone> findByIdQuery = em.createQuery("SELECT DISTINCT z FROM Zone z WHERE z.idZone = :entityId ORDER BY z.idZone", Zone.class);
      findByIdQuery.setParameter("entityId", id);
      Zone entity;
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
   public List<Zone> listAll()
   {
      final List<Zone> results = em.createQuery("SELECT DISTINCT z FROM Zone z  ORDER BY z.idZone", Zone.class).getResultList();
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(Zone entity)
   {
      entity = em.merge(entity);
      return Response.noContent().build();
   }
}