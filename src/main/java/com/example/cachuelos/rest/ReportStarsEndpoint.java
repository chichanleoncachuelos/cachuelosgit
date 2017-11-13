package com.example.cachuelos.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;

import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Commentrating;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.User;

/**
 * 
 */
@Stateless
@Path("/reportstars")
public class ReportStarsEndpoint {

	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;
	
	@GET
	@Path("/nothing")	
	public String nothing() {		
		return "hola string";
	}


	@GET
	@Path("/workerstars")
	@Produces("application/xml")
	public List<User> listAllWorkersXml() {
		final List<User> results = em.createQuery(
				"SELECT DISTINCT u FROM User u ORDER BY u.idUser", User.class)
				.getResultList();
		final List<User> resultsFinal = filterWorkerStars(results);
		return resultsFinal;
	}

	private List<User> filterWorkerStars(List<User> results) {
		List<User> resultsFinal = new ArrayList<User>();
		for (int i = 0; i < results.size(); i++) {
			User tmpUser = results.get(i);
			if (tmpUser.getIsworker() == 1 && tmpUser.getIsActive() == 1) {
				Double tmpStars = calculateStars(tmpUser);
				tmpUser.setAvgStars(tmpStars);
				resultsFinal.add(tmpUser);
			}
		}
		return resultsFinal;
	}

	private Double calculateStars(User user) {
		Double result = 0.0;
		final List<Commentrating> userCommentRatings = calculateArrayCommentRating(user);
		int arraySize = userCommentRatings.size();
		if (arraySize == 0) {
			return 0.0;
		} else {
			Double starsTotal = 0.0;
			for (Commentrating commRat : userCommentRatings) {
				starsTotal = starsTotal + commRat.getStars();
			}
			result = starsTotal / arraySize;
			return result;
		}
	}

	private List<Commentrating> calculateArrayCommentRating(User user) {
		List<Commentrating> arrayResul = new ArrayList<Commentrating>();
		final List<Commentrating> allCommentRating = em.createQuery(
				"SELECT DISTINCT c FROM Commentrating c ORDER BY c.idComment",
				Commentrating.class).getResultList();
		for (Commentrating commRat : allCommentRating) {
			if(commRat.getCommentratingtype().getIdCommentType()==1){
				if(commentIsAboutUser(user,commRat)){
					arrayResul.add(commRat);
				}
			}
		}
		return arrayResul;
	}

	private boolean commentIsAboutUser(User user, Commentrating commRat) {
		boolean resulBool=false;
		Set<Offer> offers = new HashSet<Offer>(0);
		Cachuelo cach=commRat.getCachuelo();
		offers=cach.getOffers();
		for (Offer offer : offers) {
			if (offer.getUser().getIdUser()==user.getIdUser()&&offer.getIsAccepted()==1){
				return true;
			}
		}
		return resulBool;
	}
}