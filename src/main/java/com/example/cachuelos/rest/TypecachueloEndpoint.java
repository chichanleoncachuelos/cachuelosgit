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
import com.example.cachuelosfrontend.model.Typecachuelo;

/**
 * 
 */
@Stateless
@Path("/typecachuelos")
public class TypecachueloEndpoint
{
   @PersistenceContext(unitName = "forge-default")
   private EntityManager em;

   @POST
   @Consumes("application/json")
   public Response create(Typecachuelo entity)
   {
      em.persist(entity);
      return Response.created(UriBuilder.fromResource(TypecachueloEndpoint.class).path(String.valueOf(entity.getIdTypeCachuelo())).build()).build();
   }

   @DELETE
   @Path("/{id:[0-9][0-9]*}")
   public Response deleteById(@PathParam("id") Integer id)
   {
      Typecachuelo entity = em.find(Typecachuelo.class, id);
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
      TypedQuery<Typecachuelo> findByIdQuery = em.createQuery("SELECT DISTINCT t FROM Typecachuelo t WHERE t.idTypeCachuelo = :entityId ORDER BY t.idTypeCachuelo", Typecachuelo.class);
      findByIdQuery.setParameter("entityId", id);
      Typecachuelo entity;
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
   public List<Typecachuelo> listAll()
   {
      final List<Typecachuelo> results = em.createQuery("SELECT DISTINCT t FROM Typecachuelo t ORDER BY t.idTypeCachuelo", Typecachuelo.class).getResultList();
      return results;
   }

   @PUT
   @Path("/{id:[0-9][0-9]*}")
   @Consumes("application/json")
   public Response update(Typecachuelo entity)
   {
      entity = em.merge(entity);
      return Response.noContent().build();
   }
}