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

import org.json.JSONException;
import org.json.JSONObject;

import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.model.Zone;
import com.example.cachuelos.utils.Constants;
import com.example.cachuelos.utils.Utility;

/**
 * 
 */
@Stateless
@Path("/locations")
public class LocationEndpoint {
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	@POST
	public String create(String input) {
		String lat = "";
		String lng = "";
		String name = "";
		String idUserCreator = "";
		String isFavorite = "";

		try {
			JSONObject json = new JSONObject(input);
			lat = json.getString("lat");
			lng = json.getString("lng");
			name = json.getString("name");
			idUserCreator = json.getString("idUserCreator");
			isFavorite = json.getString("isFavorite");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(belongsToAvailableZones(lat,lng)){
			
			Location loc = insertLocation(lat,lng,name,idUserCreator,isFavorite);
			if (loc != null) {
				String strRet= Utility.constructJSON(Constants.LOCATIONS, true,loc);
				return strRet;
			} else {
				String strRet= Utility.constructJSON(Constants.LOCATIONS,false, Constants.PERSISTANCE_ERROR);
				return strRet;
			}
		}
		else{
			String strRet= Utility.constructJSON(Constants.LOCATIONS,false, Constants.LOCATIONS_AVAILABLE_ZONES_ERROR);
			return strRet;
		}
		
		// em.persist(entity);
		// Response.created(UriBuilder.fromResource(UserEndpoint.class).path(String.valueOf(entity.getIdUser())).build()).build();
	}

	private boolean belongsToAvailableZones(String lat, String lng) {
		boolean resul=false;
		
		final List<Zone> list = em.createQuery(
				"SELECT DISTINCT z FROM Zone z ORDER BY z.idZone",
				Zone.class).getResultList();
		for (int i = 0; i < list.size(); i++) {			
			Zone zon=list.get(i);
			Double dist=calculateDistance(zon.getLat(),zon.getLng(),Double.parseDouble(lat),Double.parseDouble(lng));
			if (dist<=zon.getRadius()){				
				return true;
			}
		}
		return resul;
	}

	private Double calculateDistance(float lat1, float lng1, double lat2,
			double lng2) {
		
		int EARTH_RADIUS_KM = 6371;
	    double lat1Rad = Math.toRadians(lat1);
	    double lat2Rad = Math.toRadians(lat2);
	    double deltaLonRad = Math.toRadians(lng2 - lng1);

	    return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad)) * EARTH_RADIUS_KM;
	}

	private Location insertLocation(String lat, String lng, String name,
			String idUserCreator, String isFavorite) {
		
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :idParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("idParam", Integer.parseInt(idUserCreator));
		User us;
		try {
			us = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return null;//no hay usuario para ese id
		}		
		Location loc;
		loc = new Location(us, Double.parseDouble(lat), Double.parseDouble(lng), name);
		loc.setIsFavorite(Integer.parseInt(isFavorite));
		try {
			em.persist(loc);
		} catch (Exception e) {
			return null;//no hay usuario para ese id
		}
		return loc;
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Integer id) {
		Location entity = em.find(Location.class, id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findById(@PathParam("id") Integer id) {
		TypedQuery<Location> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT loc FROM Location loc  WHERE loc.idLocation = :entityId ORDER BY loc.idLocation",
						Location.class);
		findByIdQuery.setParameter("entityId", id);
		Location entity;
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
	@Path("byiduser")
	@Produces("application/json")
	public List<Location> findById2(@QueryParam("idUser") String id) {
		
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :idParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("idParam", Integer.parseInt(id));
		User user;
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			user=null;// no hay usuario para ese id
			return null;
		}
		
		final List<Location> results = em.createQuery(
				"SELECT DISTINCT l FROM Location l  LEFT JOIN FETCH l.user as user WHERE user.idUser = :idParam2 ORDER BY l.idLocation",
				Location.class).setParameter("idParam2", user.getIdUser()).getResultList();
		return results;
	}

	@GET
	@Produces("application/json")
	public List<Location> listAll() {
		final List<Location> results = em.createQuery(
				"SELECT DISTINCT l FROM Location l ORDER BY l.idLocation",
				Location.class).getResultList();
		return results;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(Location entity) {
		entity = em.merge(entity);
		return Response.noContent().build();
	}
}