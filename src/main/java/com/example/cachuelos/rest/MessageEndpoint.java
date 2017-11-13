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
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Message;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelos.utils.Constants;
import com.example.cachuelos.utils.Utility;

/**
 * 
 */
@Stateless
@Path("/messages")
public class MessageEndpoint {
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;
	private String apikey = "";

	@Context
	private ServletContext context;

	protected void newSender() {
		apikey = (String) context.getAttribute(ApiKeyInitializer
				.getAttributeAccessKey());
		System.out.println("sendMsg safely using KEY");
	}

	@POST
	public String create(String input) {
		newSender();
		String message = "";
		String idReceiver = "";
		String idUserSender = "";
		String idCachuelo = "";

		try {
			JSONObject json = new JSONObject(input);
			message = json.getString("message");
			idReceiver = json.getString("idReceiver");
			idUserSender = json.getString("idUserSender");
			idCachuelo = json.getString("idCachuelo");

		} catch (JSONException e) {
			e.printStackTrace();
			return Utility.constructJSON(Constants.MESSAGES, false,
					Constants.JSON_PARSER_ERROR);
		}
		int id = -1;
		id = insertMessage(message, idReceiver, idUserSender, idCachuelo);
		if (id > -1) {
			return Utility.constructJSON(Constants.MESSAGES, true);
		} else {
			return Utility.constructJSON(Constants.MESSAGES, false,
					Constants.GENERAL_ERROR);
		}
	}

	private int insertMessage(String message, String idReceiver,
			String idUserSender, String idCachuelo) {

		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :idParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("idParam", Integer.parseInt(idUserSender));
		User userSender;
		try {
			userSender = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return -2;// no hay usuario para ese id
		}

		TypedQuery<User> findByIdQuery2 = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :idParam ORDER BY u.idUser",
						User.class);
		findByIdQuery2.setParameter("idParam", Integer.parseInt(idReceiver));
		User userReceiver;
		try {
			userReceiver = findByIdQuery2.getSingleResult();
		} catch (NoResultException nre) {
			return -3;// no hay usuario para ese id
		}

		TypedQuery<Cachuelo> findByIdQuery3 = em
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c WHERE c.idCachuelo = :entityId ORDER BY c.idCachuelo",
						Cachuelo.class);
		findByIdQuery3.setParameter("entityId", Integer.parseInt(idCachuelo));
		Cachuelo cach;
		try {
			cach = findByIdQuery3.getSingleResult();
		} catch (NoResultException nre) {
			return -4;// no hay usuario para ese id
		}

		Message msgDB = new Message(userReceiver, userSender, cach, message,
				null);// created at null
		try {
			em.persist(msgDB);
		} catch (Exception e) {
			return -1;// error sql
		}
		int i = -1;
		i = sendMessageToPhone(userReceiver, userSender, message, idCachuelo);
		if (i > 0) {
			return msgDB.getIdMessage();
		} else {
			return -5;
		}

	}

	private int sendMessageToPhone(User userReceiver, User userSender,
			String message, String idCachuelo) {
		if (userReceiver.getGcmid() == null) {
			System.out.println("GCMID is NULL");
			return 2;
		} else {
			if (userReceiver.getGcmid().compareTo("") == 0) {
				return 3;
			}
		}
		String strJson = Utility.constructJSON(Constants.TAG_SEND_MESSAGE,
				message, idCachuelo, userSender.getIdUser().toString(),
				userSender);
		List<String> partialDevices = new ArrayList<String>();
		partialDevices.add(userReceiver.getGcmid());
		GCMSenderJson senderJson = new GCMSenderJson();
		senderJson.sendMessage(partialDevices, strJson, apikey);

		System.out.println("Sending Message out " + strJson);
		return 1;
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Integer id) {
		Message entity = em.find(Message.class, id);
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
		TypedQuery<Message> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT m FROM Message m WHERE m.idMessage = :entityId ORDER BY m.idMessage",
						Message.class);
		findByIdQuery.setParameter("entityId", id);
		Message entity;
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
	public List<Message> listAll() {
		final List<Message> results = em.createQuery(
				"SELECT DISTINCT m FROM Message m ORDER BY m.idMessage",
				Message.class).getResultList();
		return results;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(Message entity) {
		entity = em.merge(entity);
		return Response.noContent().build();
	}
}