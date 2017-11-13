package com.example.cachuelos.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.cachuelos.gcmserver.ApiKeyInitializer;
import com.example.cachuelos.gcmserver.GCMSenderJson;
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Cachuelostate;
import com.example.cachuelosfrontend.model.Imagecachuelo;
import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.Typecachuelo;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.model.Workerbytypecachuelo;
import com.example.cachuelos.utils.Constants;
import com.example.cachuelos.utils.Utility;

/**
 * servicios para los cachuelos*
 */
@Stateless
@Path("/cachuelos")
public class CachueloEndpoint {
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	protected final Logger logger = Logger.getLogger(getClass().getName());
	private static final int MULTICAST_SIZE = 1000;
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
		int id = -1;
		String idLocation = "";
		String idTypeCachuelo = "";
		String idUserPoster = "";
		String toBeCompleteAt = "";
		String name = "";
		String price = "";
		String description = "";
		JSONArray urlImages = new JSONArray();

		try {
			JSONObject json = new JSONObject(input);
			idLocation = json.getString("idLocation");
			idTypeCachuelo = json.getString("idTypeCachuelo");
			idUserPoster = json.getString("idUserPoster");
			toBeCompleteAt = json.getString("toBeCompleteAt");
			name = json.getString("name");
			price = json.getString("price");
			description = json.getString("description");
			urlImages = json.getJSONArray("urlImages");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		id = insertCachuelo(idLocation, idTypeCachuelo, idUserPoster,
				toBeCompleteAt, name, price, description, urlImages);
		if (id > 0) {
			Cachuelo cach = new Cachuelo();
			return Utility.constructJSON(Constants.CACHUELOS, true, cach, ""
					+ id);
		} else {
			return Utility.constructJSON(Constants.CACHUELOS, false,
					Constants.GENERAL_ERROR);
		}
	}

	private int sendCachueloToPhone(User userSender, Cachuelo cachuelo,
			Typecachuelo typ) {

		String strJson = Utility.constructJSON(Constants.TAG_SEND_CACHUELO,
				userSender, cachuelo);
		final List<User> usersList = em
				.createQuery(
						"SELECT DISTINCT u FROM User u WHERE u.isworker = :idParam ORDER BY u.idUser",
						User.class).setParameter("idParam", 1).getResultList();

		// Result resul = null;
		int tasks = 0;
		if (usersList.isEmpty()) {
			return 2;
		} else {
			// NOTE: check below is for demonstration purposes; a real
			// application
			// could always send a multicast, even for just one recipient
			if (usersList.size() == 1
					&& usersList.get(0).getIdUser() != userSender.getIdUser()
					&& userIsAssignedToType(usersList.get(0), typ)) {
				// send a single message using plain post
				String registrationId = usersList.get(0).getGcmid();
				if (registrationId == null) {
					System.out.println("GCMID is NULL");
					return 3;
				} else {
					if (registrationId.compareTo("") == 0) {
						System.out.println("GCMID is empty");
						return 4;
					}
				}
				List<String> partialDevices = new ArrayList<String>();
				partialDevices.add(registrationId);
				GCMSenderJson senderJson = new GCMSenderJson();
				senderJson.sendMessage(partialDevices, strJson, apikey);

			} else {
				// send a multicast message using JSON
				// must split in chunks of 1000 devices (GCM limit)
				int total = usersList.size();
				List<String> partialDevices = new ArrayList<String>(total);
				int counter = 0;

				for (User user : usersList) {
					counter++;
					if (user.getGcmid() != null) {
						if (user.getIdUser() != userSender.getIdUser()
								&& user.getGcmid().compareTo("") != 0
								&& userIsAssignedToType(user, typ)) {
							partialDevices.add(user.getGcmid());
						}
					}
					int partialSize = partialDevices.size();
					if (partialSize == MULTICAST_SIZE || counter == total) {
						if (partialSize > 0) {
							// asyncSend(partialDevices, strJson);
							GCMSenderJson senderJson = new GCMSenderJson();
							senderJson.sendMessage(partialDevices,
									strJson, apikey);
							partialDevices.clear();
							tasks++;
						}
					}
				}
			}
		}
		System.out
				.println("Message Cachuelos out" + strJson + " tasks: " + tasks);
		return 1;
	}

	private boolean userIsAssignedToType(User user, Typecachuelo typ) {
		boolean resul = false;
		for (Iterator<Workerbytypecachuelo> iterator = user
				.getWorkerbytypecachuelos().iterator(); iterator.hasNext();) {
			Workerbytypecachuelo workerbytype = (Workerbytypecachuelo) iterator
					.next();
			System.out.println("Type Cachuelo ? : "
					+ workerbytype.getTypecachuelo().getName());
			if (workerbytype.getTypecachuelo().getIdTypeCachuelo() == typ
					.getIdTypeCachuelo() && workerbytype.getIsAvailable() == 1) {
				System.out.println("User is assigned return true");
				return true;
			}
		}
		System.out.println("User is assigned return false");
		return resul;
	}

	private int insertCachuelo(String idLocation, String idTypeCachuelo,
			String idUserPoster, String toBeCompleteAt, String name,
			String price, String description, JSONArray urlImages) {

		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :idParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("idParam", Integer.parseInt(idUserPoster));
		User user;
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return -2;// no hay usuario para ese id
		}

		TypedQuery<Typecachuelo> findByIdQuery2 = em
				.createQuery(
						"SELECT DISTINCT t FROM Typecachuelo t  WHERE t.idTypeCachuelo = :idParam2 ORDER BY t.idTypeCachuelo",
						Typecachuelo.class);
		findByIdQuery2.setParameter("idParam2",
				Integer.parseInt(idTypeCachuelo));
		Typecachuelo typ;
		try {
			typ = findByIdQuery2.getSingleResult();
		} catch (NoResultException nre) {
			return -3;// no hay Typecachuelo para ese id
		}
		TypedQuery<Location> findByIdQuery3 = em
				.createQuery(
						"SELECT DISTINCT loc FROM Location loc WHERE loc.idLocation = :idParam3 ORDER BY loc.idLocation",
						Location.class);
		findByIdQuery3.setParameter("idParam3", Integer.parseInt(idLocation));
		Location loc;
		try {
			loc = findByIdQuery3.getSingleResult();
		} catch (NoResultException nre) {
			loc = null;// no hay location
		}

		Cachuelo cach = new Cachuelo(typ, user, null, name);
		cach.setPrice(price);
		cach.setToBeCompleteAt(toBeCompleteAt);
		cach.setDescription(description);
		cach.setLocation(loc);
		cach.setCachuelostate(getCachueloState("1"));// trae el primer estado
														// Abierto

		try {
			em.persist(cach);
		} catch (Exception e) {
			return -1;// error sql
		}
		if (urlImages.length() > 0) {
			int imageStatus = insertImages(cach, urlImages);
			if (imageStatus == -1) {
				return -4;
			}
		}

		int res = -1;
		res = sendCachueloToPhone(user, cach, typ);
		if (res > 0) {
			return cach.getIdCachuelo();
		} else {
			return -5;
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

	private int insertImages(Cachuelo cach, JSONArray urlImages) {

		for (int i = 0; i < urlImages.length(); i++) {
			JSONObject json;
			String urlImage = "";
			try {
				json = urlImages.getJSONObject(i);
				urlImage = json.getString("urlImage");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			Imagecachuelo img = new Imagecachuelo(cach, urlImage);
			try {
				em.persist(img);
			} catch (Exception e) {
				return -1;// error sql
			}
		}
		return 1;
	}

	@POST
	@Path("finish")
	public String finishCachueloService(String input) {
		String idCachuelo = "";
		String idUserPoster = "";
		newSender();
		boolean updateSuccess = false;
		try {
			JSONObject json = new JSONObject(input);
			idCachuelo = json.getString("idCachuelo");
			idUserPoster = json.getString("idUserPoster");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		TypedQuery<Cachuelo> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c WHERE c.idCachuelo = :entityId ORDER BY c.idCachuelo",
						Cachuelo.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idCachuelo));
		Cachuelo cach;
		try {
			cach = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			cach = null;// no hay Cachuelo para ese id
			return null;
		}
		updateSuccess = finishCachuelo(cach, idUserPoster);
		if (updateSuccess) {
			return Utility.constructJSON(Constants.CACHUELOS, true);
		} else {
			return Utility.constructJSON(Constants.OFFERS, false,
					Constants.GENERAL_ERROR);
		}
	}

	private boolean finishCachuelo(Cachuelo cach, String idUserPoster) {
		cach.setCachuelostate(getCachueloState("4"));

		int res = -1;
		res = sendSurveyToPhone(cach, idUserPoster);

		if (res > 0) {
			try {
				em.merge(cach);
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	private int sendSurveyToPhone(Cachuelo cachuelo, String idUserPoster) {

		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idUserPoster));
		User userSender;
		try {
			userSender = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return -1;
		}
		if (cachuelo.getUser().getGcmid() == null) {
			System.out.println("GCMID is NULL");
			return 2;
		} else {
			if (cachuelo.getUser().getGcmid().compareTo("") == 0) {
				System.out.println("GCMID is empty");
				return 3;
			}
		}
		String strJson = Utility.constructJSON(Constants.TAG_SURVEY,
				userSender, cachuelo, "1");

		// Message msg = new Message.Builder().timeToLive(30)
		// .delayWhileIdle(false).addData("message", strJson).build();
		List<String> partialDevices = new ArrayList<String>();
		partialDevices.add(cachuelo.getUser().getGcmid());
		GCMSenderJson senderJson = new GCMSenderJson();
		senderJson.sendMessage(partialDevices, strJson, apikey);

		System.out.println("Message Survey out:" + strJson);
		return 1;
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Integer id) {
		Cachuelo entity = em.find(Cachuelo.class, id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	@GET
	@Path("getcachuelobyid")
	@Produces("application/json")
	public Response findById(@QueryParam("idCachuelo") String idCachuelo) {
		TypedQuery<Cachuelo> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c  LEFT JOIN FETCH c.user WHERE c.idCachuelo = :entityId ORDER BY c.idCachuelo",
						Cachuelo.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idCachuelo));
		Cachuelo entity;
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
	public List<Cachuelo> listAll() {
		final List<Cachuelo> results = em
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.commentratings ORDER BY c.idCachuelo",
						Cachuelo.class).getResultList();
		return results;
	}

	@GET
	@Path("getnearcachuelo")
	@Produces("application/json")
	public List<Cachuelo> getNearCachuelo(@QueryParam("idUser") String idUser,
			@QueryParam("lat") String lat, @QueryParam("lng") String lng) {
		final List<Cachuelo> results = em
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.commentratings ORDER BY c.idCachuelo",
						Cachuelo.class).getResultList();
		return addAssignedToMeIfNecessary(results, idUser);
	}

	private List<Cachuelo> addAssignedToMeIfNecessary(
			List<Cachuelo> arrayCachuelos, String idUser) {
		List<Cachuelo> results = new ArrayList<Cachuelo>();
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idUser));
		User thisUser;
		try {
			thisUser = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			thisUser = null;
		}
		if (thisUser == null) {
			return results;
		} else {
			if (thisUser.getIsworker() == 0) {
				return filter(arrayCachuelos, thisUser);
			} else {
				for (Cachuelo cachuelo : arrayCachuelos) {
					if (isAssignedToMe(cachuelo, thisUser)) {
						cachuelo.setIsAssignedToMe(1);
					}
					results.add(cachuelo);
				}
			}
		}
		return filter(results, thisUser);
	}

	private List<Cachuelo> filter(List<Cachuelo> arrayCachuelos, User thisUser) {
		List<Cachuelo> results = new ArrayList<Cachuelo>();
		if (thisUser == null) {
			return results;
		} else {
			if (thisUser.getIsworker() == 0) {// para clientes
				for (Cachuelo cachuelo : arrayCachuelos) {
					if (cachuelo.getUser().getIdUser() == thisUser.getIdUser()) {
						results.add(cachuelo);
					}
				}
			} else {// para trabajadores
				for (Cachuelo cachuelo : arrayCachuelos) {
					if (userIsAssignedToType(thisUser,
							cachuelo.getTypecachuelo())
							|| cachuelo.getUser().getIdUser() == thisUser
									.getIdUser()) {
						results.add(cachuelo);
					}
				}
			}
		}
		return results;
	}

	private boolean isAssignedToMe(Cachuelo cachuelo, User thisUser) {
		if (cachuelo.getCachuelostate().getIdCachueloState() == 1
				|| cachuelo.getCachuelostate().getIdCachueloState() == 2) {
			return false;
		} else {
			for (Offer offer : cachuelo.getOffers()) {
				if (offer.getIsAccepted() == 1
						&& offer.getUser().getIdUser() == thisUser.getIdUser()) {
					return true;
				}
			}
		}
		return false;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(Cachuelo entity) {
		entity = em.merge(entity);
		return Response.noContent().build();
	}
}