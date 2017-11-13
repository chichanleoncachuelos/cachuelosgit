package com.example.cachuelos.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.cachuelos.gcmserver.ApiKeyInitializer;
import com.example.cachuelos.gcmserver.GCMSenderJson;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.model.Userlocationbytime;
import com.example.cachuelos.utils.Constants;
import com.example.cachuelos.utils.Utility;

/**
 * 
 */
@Stateless
@Path("/userlocationbytimes")
public class UserlocationbytimeEndpoint {
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;
	
	@Context
    private ServletContext context;
	private String apikey = "";

	protected void newSender() {
		apikey = (String) context.getAttribute(ApiKeyInitializer
				.getAttributeAccessKey());
		System.out.println("sendMsg safely using KEY");
	}
	
	@POST
	public String create(String input) {
		int id = -1;
		String lat = "";
		String lng = "";
		String idUser = "";

		boolean success = false;
		try {
			JSONObject json = new JSONObject(input);
			lat = json.getString("lat");
			lng = json.getString("lng");
			idUser = json.getString("idUser");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		id = insertUserLocation(lat, lng, idUser);
		if (id > 0) {
			success = true;
		} else {
			success = false;
		}

		if (success)
			return "success" + id;
		else {
			return "failed" + id;
		}
	}

	private int insertUserLocation(String lat, String lng,
			String idUser) {

		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :idParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("idParam", Integer.parseInt(idUser));
		User us;
		try {
			us = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return -2;// no hay usuario para ese id
		}

		Userlocationbytime loc = new Userlocationbytime(us,Double.parseDouble(lat), Double.parseDouble(lng), null);
		try {
			em.persist(loc);
		} catch (Exception e) {
			return -1;// error sql
		}
		return loc.getIdUserLocationByTime();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Integer id) {
		Userlocationbytime entity = em.find(Userlocationbytime.class, id);
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
		TypedQuery<Userlocationbytime> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM Userlocationbytime u  WHERE u.idUserLocationByTime = :entityId ORDER BY u.idUserLocationByTime",
						Userlocationbytime.class);
		findByIdQuery.setParameter("entityId", id);
		Userlocationbytime entity;
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
	@Path("requestuserlocation/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public String requestuserlocation(@PathParam("id") Integer id) {
		newSender();
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", id);
		User user;
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return "failed";
		}
		if (user == null) {
			return "failed";
		}
		int i=-1;
		if (user.getGcmid()!=null){
			if (user.getGcmid().compareTo("")!=0 && user.getGcmid().compareTo(" ")!=0){
				i=sendRequestPhoneLocation(user);
			}
			else{
				i=-4;
			}
		}else{
			i=-3;
		}
		if (i==1){
			return "success";
		}else{
			return "failed";
		}		
	}
	private int sendRequestPhoneLocation(User user) {				
		String strJson = Utility.constructJSON(Constants.TAG_GET_USER_LOCATION, user.getIdUser().toString());		
		List<String> partialDevices = new ArrayList<String>();
		partialDevices.add(user.getGcmid());
		GCMSenderJson senderJson = new GCMSenderJson();
		senderJson.sendMessage(partialDevices, strJson, apikey);
		System.out.println("Request Location out" + strJson);
		return 1;
	}

	@GET
	@Produces("application/json")
	public List<Userlocationbytime> listAll() {
		final List<Userlocationbytime> results = em
				.createQuery(
						"SELECT DISTINCT u FROM Userlocationbytime u  ORDER BY u.idUserLocationByTime",
						Userlocationbytime.class).getResultList();
		return results;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(Userlocationbytime entity) {
		entity = em.merge(entity);
		return Response.noContent().build();
	}
}