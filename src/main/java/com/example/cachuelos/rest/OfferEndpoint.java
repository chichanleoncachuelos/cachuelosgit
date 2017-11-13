package com.example.cachuelos.rest;

import java.io.IOException;
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
import com.example.cachuelosfrontend.model.Cachuelostate;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelos.utils.Constants;
import com.example.cachuelos.utils.Utility;

/**
 * 
 */
@Stateless
@Path("/offers")
public class OfferEndpoint {
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	@Context
	private ServletContext context;
	private String apikey = "";

	// private Sender sender;

	protected void newSender() {
		apikey = (String) context.getAttribute(ApiKeyInitializer
				.getAttributeAccessKey());
		System.out.println("sendMsg safely using KEY");
	}

	@POST
	public String create(String input) {
		newSender();
		String price = "";
		String idUserSender = "";
		String idCachuelo = "";
		String idUserToNotify = "";
		String userSenderName = "";
		String cachueloName = "";
		int id = -1;
		try {
			JSONObject json = new JSONObject(input);
			price = json.getString("price");
			idUserSender = json.getString("idUserSender");
			idCachuelo = json.getString("idCachuelo");
			idUserToNotify = json.getString("idUserToNotify");
			userSenderName = json.getString("idUserSenderName");
			cachueloName = json.getString("cachueloName");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (!isClosed(idCachuelo)) {
			id = insertOffer(price, idUserSender, idCachuelo, idUserToNotify,
					userSenderName, cachueloName);
			if (id > 0) {
				Offer off = new Offer();
				off.setIdOffer(id);
				return Utility.constructJSON(Constants.OFFERS, true, off);
			} else {
				return Utility.constructJSON(Constants.OFFERS, false,
						Constants.GENERAL_ERROR);
			}
		} else {
			return Utility.constructJSON(Constants.OFFERS, false,
					Constants.CACHUELO_IS_CLOSED_ERROR);
		}

	}

	private boolean isClosed(String idCachuelo) {
		TypedQuery<Cachuelo> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c WHERE c.idCachuelo = :entityId ORDER BY c.idCachuelo",
						Cachuelo.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idCachuelo));
		Cachuelo cach;
		try {
			cach = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return true;
		}
		if (cach.getCachuelostate().getIdCachueloState() == 3
				|| cach.getCachuelostate().getIdCachueloState() == 4) {
			return true;
		} else {
			return false;
		}
	}

	private int insertOffer(String price, String idUserSender,
			String idCachuelo, String idUserToNotify, String userSenderName,
			String cachueloName) {

		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idUserSender));
		User userSender;
		try {
			userSender = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return -1;
		}

		TypedQuery<Cachuelo> findByIdQuery2 = em
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c WHERE c.idCachuelo = :entityId ORDER BY c.idCachuelo",
						Cachuelo.class);
		findByIdQuery2.setParameter("entityId", Integer.parseInt(idCachuelo));
		Cachuelo cachuelo;
		try {
			cachuelo = findByIdQuery2.getSingleResult();
		} catch (NoResultException nre) {
			return -2;
		}

		Offer offer = new Offer(userSender, cachuelo, price, null);// createdat
																	// null
		offer.setIsAccepted(0);
		cachuelo.setCachuelostate(getCachueloState("2"));
		try {
			em.persist(offer);
			em.merge(cachuelo);
		} catch (Exception e) {
			return -3;
		}
		int res = -1;
		res = sendOfferToPhone(idCachuelo, userSender, cachueloName, offer,
				idUserToNotify);
		if (res > 0) {
			return offer.getIdOffer();
		} else {
			return -4;
		}

	}

	private Cachuelostate getCachueloState(String id) {
		Cachuelostate cachState;

		TypedQuery<Cachuelostate> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT cs FROM Cachuelostate cs  WHERE cs.idCachueloState = :idParam ORDER BY cs.idCachueloState",
						Cachuelostate.class);
		findByIdQuery.setParameter("idParam", Integer.parseInt(id));
		try {
			cachState = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
		return cachState;
	}

	private int sendOfferToPhone(String idCachuelo, User userSender,
			String cachueloName, Offer offer, String idUserToNotify) {
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery
				.setParameter("entityId", Integer.parseInt(idUserToNotify));
		User userToNotify;
		try {
			userToNotify = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return -1;
		}
		if (userToNotify.getGcmid() == null) {
			System.out.println("GCMID is NULL");
			return 2;
		} else {
			if (userToNotify.getGcmid().compareTo("") == 0) {
				System.out.println("GCMID is empty");
				return 3;
			}
		}
		String strJson = Utility.constructJSON(Constants.TAG_SEND_OFFER,
				idCachuelo, userSender, cachueloName, offer);
		List<String> partialDevices = new ArrayList<String>();
		partialDevices.add(userToNotify.getGcmid());
		GCMSenderJson senderJson = new GCMSenderJson();
		senderJson.sendMessage(partialDevices, strJson, apikey);
		System.out.println("Message Offer out" + strJson);
		return 1;
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Integer id) {
		Offer entity = em.find(Offer.class, id);
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
		TypedQuery<Offer> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT o FROM Offer o WHERE o.idOffer = :entityId ORDER BY o.idOffer",
						Offer.class);
		findByIdQuery.setParameter("entityId", id);
		Offer entity;
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
	public List<Offer> listAll() {
		final List<Offer> results = em.createQuery(
				"SELECT DISTINCT o FROM Offer o ORDER BY o.idOffer",
				Offer.class).getResultList();
		return results;
	}

	@POST
	@Path("accept")
	public String acceptOfferService(String input) {
		String idOffer = "";
		newSender();
		int id = -1;
		try {
			JSONObject json = new JSONObject(input);
			idOffer = json.getString("idOffer");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		TypedQuery<Offer> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT o FROM Offer o  WHERE o.idOffer = :idParam ORDER BY o.idOffer",
						Offer.class);
		findByIdQuery.setParameter("idParam", Integer.parseInt(idOffer));
		Offer offe;
		try {
			offe = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			offe = null;// no hay OFERTA para ese id
			return null;
		}
		if (!isClosed("" + offe.getCachuelo().getIdCachuelo())) {
			id = acceptOffer(offe);
			if (id > 0) {
				if (updateCachueloStateToClosed(offe)) {
					return Utility.constructJSON(Constants.OFFERS, true);
				} else {
					return Utility.constructJSON(Constants.OFFERS, false,
							Constants.GENERAL_ERROR);
				}

			} else {
				return Utility.constructJSON(Constants.OFFERS, false,
						Constants.GENERAL_ERROR);
			}
		} else {
			return Utility.constructJSON(Constants.OFFERS, false,
					Constants.CACHUELO_IS_CLOSED_ERROR);
		}
	}

	private boolean updateCachueloStateToClosed(Offer offe) {
		TypedQuery<Cachuelo> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c WHERE c.idCachuelo = :entityId ORDER BY c.idCachuelo",
						Cachuelo.class);
		findByIdQuery.setParameter("entityId", offe.getCachuelo()
				.getIdCachuelo());
		Cachuelo cach;
		try {
			cach = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return false;
		}
		cach.setCachuelostate(getCachueloState("3"));
		cach.setFinalPrice(offe.getPrice());
		try {
			em.merge(cach);
		} catch (Exception e) {
			return false;
		}
		int res = -1;
		res = sendAcceptedOfferToPhone(cach, offe);
		if (res > 0) {
			return true;
		} else {
			return false;
		}

	}

	private int sendAcceptedOfferToPhone(Cachuelo cach, Offer offe) {
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", offe.getUser().getIdUser());
		User userToNotify;
		try {
			userToNotify = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return -1;
		}
		if (userToNotify.getGcmid() == null) {
			System.out.println("GCMID is NULL");
			return 2;
		} else {
			if (userToNotify.getGcmid().compareTo("") == 0) {
				System.out.println("GCMID is empty");
				return 3;
			}
		}
		String strJson = Utility.constructJSON(Constants.TAG_ACCEPTED_OFFER,
				cach.getUser(), cach);		
		List<String> partialDevices = new ArrayList<String>();
		partialDevices.add(userToNotify.getGcmid());
		GCMSenderJson senderJson = new GCMSenderJson();
		senderJson.sendMessage(partialDevices, strJson, apikey);
		System.out.println("Message Offer out" + strJson);
		return 1;
	}

	private int acceptOffer(Offer offe) {
		offe.setIsAccepted(1);
		try {
			em.merge(offe);
		} catch (Exception e) {
			return -1;
		}
		return offe.getIdOffer();
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(Offer entity) {
		entity = em.merge(entity);
		return Response.noContent().build();
	}

	@GET
	@Path("getuseroffers")
	@Produces("application/json")
	public List<Offer> getUserOffers(@QueryParam("idUser") String idUser) {
		List<Offer> arrayOffers;
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idUser));
		User user;
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
		arrayOffers = findUserOffers(user);
		return arrayOffers;

	}

	private List<Offer> findUserOffers(User user) {
		List<Offer> results = new ArrayList<Offer>();
		List<Offer> resultsPartial = em.createQuery(
				"SELECT DISTINCT o FROM Offer o ORDER BY o.idOffer",
				Offer.class).getResultList();
		for (Offer offer : resultsPartial) {
			if (offer.getUser().getIdUser() == user.getIdUser()
					|| isAboutUser(offer, user)) {
				results.add(offer);
			}
		}
		return results;
	}

	private boolean isAboutUser(Offer offer, User user) {
		if (offer.getCachuelo().getUser().getIdUser() == user.getIdUser()) {
			return true;
		} else {
			return false;
		}
	}
}