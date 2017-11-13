package com.example.cachuelos.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Commentrating;
import com.example.cachuelosfrontend.model.Commentratingtype;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelos.utils.Constants;
import com.example.cachuelos.utils.Utility;

/**
 * 
 */
@Stateless
@Path("/commentratings")
public class CommentratingEndpoint {
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	@POST
	public String create(String input) {

		String comment = "";
		String stars = "";
		String idUserPoster = "";
		String idCachuelo = "";
		String idCommentType = "";
		int id = -1;
		try {
			JSONObject json = new JSONObject(input);
			comment = json.getString("comment");
			stars= json.getString("stars");
			idUserPoster = json.getString("idUserPoster");
			idCachuelo = json.getString("idCachuelo");
			idCommentType = json.getString("idCommentType");
		} catch (JSONException e) {
			e.printStackTrace();
			return Utility.constructJSON(Constants.COMMENTRATINGS, false,
					Constants.JSON_PARSER_ERROR);
		}

		id = insertComment(comment,stars,idUserPoster,idCachuelo,idCommentType);
		if (id != -1) {
			return Utility.constructJSON(Constants.COMMENTRATINGS, true,id);
		} else {
			return Utility.constructJSON(Constants.COMMENTRATINGS, false,
					Constants.PERSISTANCE_ERROR);
		}
	}

	private int insertComment(String comment, String stars ,String idUserPoster,String idCachuelo,String idCommentType) {
		
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idUserPoster));
		User user;
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			user = null;
			return -2;
		}
		
		
		TypedQuery<Commentratingtype> findByIdQuery2 = em
				.createQuery(
						"SELECT DISTINCT ct FROM Commentratingtype ct  WHERE ct.idCommentType = :entityId ORDER BY ct.idCommentType",
						Commentratingtype.class);
		findByIdQuery2.setParameter("entityId", Integer.parseInt(idCommentType));
		Commentratingtype cType;
		try {
			cType = findByIdQuery2.getSingleResult();
		} catch (NoResultException nre) {
			cType = null;
			return -3;
		}
		
		TypedQuery<Cachuelo> findByIdQuery3 = em
				.createQuery(
						"SELECT DISTINCT ca FROM Cachuelo ca  WHERE ca.idCachuelo = :entityId ORDER BY ca.idCachuelo",
						Cachuelo.class);
		findByIdQuery3.setParameter("entityId", Integer.parseInt(idCachuelo));
		Cachuelo cachuelo;
		try {
			cachuelo = findByIdQuery3.getSingleResult();
		} catch (NoResultException nre) {
			cachuelo = null;
			return -4;
		}		
		
		Commentrating cmr = new Commentrating(cType, cachuelo, user, null, comment);
		cmr.setStars(Integer.parseInt(stars));
		try {
			em.persist(cmr);
		} catch (Exception e) {
			return -1;
		}
		return cmr.getIdComment();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Integer id) {
		Commentrating entity = em.find(Commentrating.class, id);
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
		TypedQuery<Commentrating> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT c FROM Commentrating c WHERE c.idComment = :entityId ORDER BY c.idComment",
						Commentrating.class);
		findByIdQuery.setParameter("entityId", id);
		Commentrating entity;
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
	public List<Commentrating> listAll() {
		final List<Commentrating> results = em.createQuery(
				"SELECT DISTINCT c FROM Commentrating c ORDER BY c.idComment",
				Commentrating.class).getResultList();
		return results;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(Commentrating entity) {
		entity = em.merge(entity);
		return Response.noContent().build();
	}
	
	/////////////////////////////////////
	
	
	@GET
	@Path("getratingaboutuser")
	@Produces("application/json")
	public List<Commentrating> listCommentRatingAboutUser(@QueryParam("idUser") String idUser,@QueryParam("workerMode") String workerMode) {
		
		List<Commentrating> results=new ArrayList<Commentrating>();
		
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
			return results;
		}
		if(workerMode.compareTo("1")==0){
			results=filterMyCommentRatingsWorkerMode(entity);			
		}
		else{
			results=filterMyCommentRatings(entity);
		}
		
		return results;
	}
	
	public List<Commentrating> filterMyCommentRatingsWorkerMode(User user) {
		List<Commentrating> tmpLst=getAllCommentRating();
		List<Commentrating> lstResul= new ArrayList<Commentrating>();
		for (Commentrating commentrating : tmpLst) {
			if (isAboutUserWorkerMode(commentrating, user)&&commentrating.getCommentratingtype().getIdCommentType()==2){
				lstResul.add(commentrating);
			}
		}
		return lstResul;
		
	}
	
	
	public List<Commentrating> filterMyCommentRatings(User user) {
		List<Commentrating> tmpLst=getAllCommentRating();
		List<Commentrating> lstResul= new ArrayList<Commentrating>();
		for (Commentrating commentrating : tmpLst) {
			if (isAboutUser(commentrating, user)&&commentrating.getCommentratingtype().getIdCommentType()==1){
				lstResul.add(commentrating);
			}
		}
		return lstResul;
		
	}
	
	private boolean isAboutUserWorkerMode(Commentrating commentrating, User user) {
		boolean resulBool=false;
		if (commentrating.getCachuelo().getUser().getIdUser()==user.getIdUser()){
			resulBool=true;
		}
		return resulBool;
	}
	
	private boolean isAboutUser(Commentrating commentrating, User user) {
		boolean resulBool=false;
		Set<Offer> offers = new HashSet<Offer>(0);
		Cachuelo cach=commentrating.getCachuelo();
		offers=cach.getOffers();
		for (Offer offer : offers) {
			if (offer.getUser().getIdUser()==user.getIdUser()&&offer.getIsAccepted()==1){
				return true;
			}
		}
		return resulBool;
	}
	
	public List<Commentrating> getAllCommentRating() {
		CriteriaQuery<Commentrating> criteria = this.em
				.getCriteriaBuilder().createQuery(Commentrating.class);
		return this.em.createQuery(
				criteria.select(criteria.from(Commentrating.class)))
				.getResultList();
	}
	
}