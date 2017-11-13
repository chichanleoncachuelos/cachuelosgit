package com.example.cachuelos.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.User;

/**
 * 
 */
@Stateless
@Path("/reports")
public class ReportsEndpoint {

	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	@POST
	public String create(String input) {
		return "";
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Integer id) {
		User entity = em.find(User.class, id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	@GET
	@Path("findbyid")
	@Produces("application/json")
	public Response findByIdDetail(@QueryParam("idUser") String idUser) {
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idUser));
		User entity;
		try {
			entity = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			entity = null;
		}
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(entity).build();
	}

	@GET
	@Produces("application/json")
	public List<User> listAll() {
		final List<User> results = em.createQuery(
				"SELECT DISTINCT u FROM User u ORDER BY u.idUser", User.class)
				.getResultList();
		return results;
	}
		

	@GET
	@Path("cachuelosbytype")
	@Produces("application/xml")
	public List<Cachuelo> listAllXml() {
		final List<Cachuelo> results = em
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.commentratings LEFT JOIN FETCH c.typecachuelo ORDER BY c.idCachuelo",
						Cachuelo.class).getResultList();
		final List<Cachuelo> resultsFinal = filterCachueloResults(results);
		return resultsFinal;
	}

	private List<Cachuelo> filterCachueloResults(List<Cachuelo> results) {
		List<Cachuelo> resultsFinal = new ArrayList<Cachuelo>();
		for (Cachuelo cachuelo : results) {
			if (countOnList(cachuelo, resultsFinal) == 0
					&& (cachuelo.getCachuelostate().getIdCachueloState() == 3 || cachuelo
							.getCachuelostate().getIdCachueloState() == 4)) {
				cachuelo.setTmpC(1);
				resultsFinal.add(cachuelo);
			} else {
				update(resultsFinal, cachuelo);
			}
		}
		return resultsFinal;
	}

	private void update(List<Cachuelo> resultsFinal, Cachuelo cachuelo) {
		for (int i = 0; i < resultsFinal.size(); i++) {
			Cachuelo cach = resultsFinal.get(i);
			int tmpC = cach.getTmpC();
			// int tmpPrice=Integer.parseInt(cach.getPrice());
			// int tmpFinalPrice=Integer.parseInt(cach.getFinalPrice());
			if (cach.getTypecachuelo().getIdTypeCachuelo() == cachuelo
					.getTypecachuelo().getIdTypeCachuelo()) {
				resultsFinal.get(i).setTmpC(tmpC + 1);
			}
		}
	}

	private int countOnList(Cachuelo cachuelo, List<Cachuelo> resultsFinal) {
		for (Cachuelo cach : resultsFinal) {
			if (cach.getTypecachuelo().getIdTypeCachuelo() == cachuelo
					.getTypecachuelo().getIdTypeCachuelo()) {
				return 1;
			}
		}
		return 0;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(User entity) {
		entity = em.merge(entity);
		return Response.noContent().build();
	}
}