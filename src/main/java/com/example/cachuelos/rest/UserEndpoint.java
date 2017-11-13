package com.example.cachuelos.rest;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.cachuelosfrontend.model.User;
import com.example.cachuelos.utils.Constants;
import com.example.cachuelos.utils.PasswordEncrypterSHABase64;
import com.example.cachuelos.utils.RegistrationEmail;
import com.example.cachuelos.utils.Utility;
import com.example.cachuelos.view.ApplicationBean;

/**
 * 
 */
@Stateless
@Path("/users")
public class UserEndpoint {

	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;
	
	@Inject
	ApplicationBean applicationBean;

	@POST
	public String create(String input) {
		String fullname = "";
		String email = "";
		String isfacebook = "0";
		String facebookid = "";
		String facebooktoken = "";
		String password = "";
		int id = -1;
		try {
			JSONObject json = new JSONObject(input);
			fullname = json.getString("name");
			email = json.getString("email");
			isfacebook = json.getString("isfacebook");
			if (isfacebook.compareTo("1") == 0) {
				facebookid = json.getString("facebookid");
				facebooktoken = json.getString("facebooktoken");
			}
			password = json.getString("password");
		} catch (JSONException e) {
			e.printStackTrace();
			return Utility.constructJSON(Constants.USERS, false,
					Constants.JSON_PARSER_ERROR);
		}

		if (isfacebook.compareTo("0") == 0) {
			if (!existUser(email)) {
				id = insertUser(fullname, email, password,facebookid,facebooktoken,false);
				if (id != -1) {
					return Utility.constructJSON(Constants.USERS, true);
				} else {
					return Utility.constructJSON(Constants.USERS, false,
							Constants.PERSISTANCE_ERROR);
				}
			} else {
				return Utility.constructJSON(Constants.USERS, false,
						Constants.ALREADY_REGISTERED);
			}	
		}else{
			if (!existUserFacebook(facebookid)) {
				id = insertUser(fullname, email, password,facebookid,facebooktoken,true);
				if (id != -1) {
					return Utility.constructJSON(Constants.USERS, true);
				} else {
					return Utility.constructJSON(Constants.USERS, false,
							Constants.PERSISTANCE_ERROR);
				}
			} else {
				mergeUser(facebookid,facebooktoken,email);
				return Utility.constructJSON(Constants.USERS, false,
						Constants.ALREADY_REGISTERED);
			}
		}
		
	}

	private void mergeUser(String facebookid,String facebooktoken, String email) {
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.facebookid = :facebookidParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("facebookidParam", facebookid);
		User entity;
		try {
			entity = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			entity = null;
		}

		if (entity == null) {
			
		} else {
			entity.setFacebooktoken(facebooktoken);
			entity.setEmail(email);
			try {
				em.merge(entity);
			} catch (Exception e) {
				entity = null;
				e.printStackTrace();
			}
		}		
	}

	private int insertUser(String fullname, String email, String password, String facebookid, String facebooktoken, boolean isfacebook) {
		String name = "";
		String lastName = "";
		if (fullname.contains(" ")) {
			name = fullname.substring(0, fullname.indexOf(" "));
			lastName = fullname.substring(fullname.indexOf(' ') + 1);
		} else {
			name = "" + fullname;
			lastName = "NoLastName";
		}
		User us = new User(name, lastName, email, 0, 0, password, null);// 1 is
																		// worker
																		// /0 is
																		// not
																		// active/
																		// null
																		// datestamp
		String secretkey = UUID.randomUUID().toString();
		us.setSecretkey(secretkey);
		us.setFacebookid(facebookid);
		us.setFacebooktoken(facebooktoken);
		if (isfacebook) {
			us.setIsActive(1);
		}
		
		try {
			em.persist(us);
		} catch (Exception e) {
			return -1;
		}
		String host = applicationBean.getIpAddress();
		System.out.println("host: "+host +"   ////fin host");
		RegistrationEmail.sendRegistrationMail(secretkey, email,host);
		return us.getIdUser();
	}

	private boolean existUser(String email) {
		boolean resul = false;

		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.email = :emailParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("emailParam", email);
		User entity;
		try {
			entity = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			entity = null;
		}

		if (entity == null) {
			resul = false;
		} else {
			resul = true;
		}
		return resul;
	}
	private boolean existUserFacebook(String facebookid) {
		boolean resul = false;

		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.facebookid = :facebookidParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("facebookidParam", facebookid);
		User entity;
		try {
			entity = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			entity = null;
		}

		if (entity == null) {
			resul = false;
		} else {
			resul = true;
		}
		return resul;
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
	@Path("/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findById(@PathParam("id") Integer id) {
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", id);
		User entity;
		try {
			entity = findByIdQuery.getSingleResult();
			String host = applicationBean.getIpAddress();
			System.out.println("host: "+host +"   ////fin host");
			entity.setName(host);
		} catch (NoResultException nre) {
			entity = null;
		}
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(entity).build();
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
	@Path("dologin")
	@Produces("application/json")
	public String doLogin(@QueryParam("email") String email,
			@QueryParam("password") String password) {
		int isactive = 1;
		User user = null;
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.email = :emailParam and u.password = :passwordParam and u.isActive = :isactiveParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("emailParam", email);
		findByIdQuery.setParameter("passwordParam", password);
		findByIdQuery.setParameter("isactiveParam", isactive);
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			user = null;
			return Utility.constructJSON(Constants.USERS, false,
					Constants.LOGIN_ERROR);
		}
		return Utility.constructJSON(Constants.USERS, true, user);
	}
	@GET
	@Path("dofacebooklogin")
	@Produces("application/json")
	public String doFacebookLogin(@QueryParam("facebookid") String facebookid) {
		int isactive = 1;
		User user = null;
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.facebookid = :facebookidParam and u.isActive = :isactiveParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("facebookidParam", facebookid);
		findByIdQuery.setParameter("isactiveParam", isactive);
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			user = null;
			return Utility.constructJSON(Constants.USERS, false,
					Constants.LOGIN_ERROR);
		}
		return Utility.constructJSON(Constants.USERS, true, user);
	}

	@GET
	@Path("dologout")
	@Produces("application/json")
	public String doLogOut(@QueryParam("idUser") String idUser) {
		User user = null;
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :idUserParam  ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("idUserParam", Integer.parseInt(idUser));
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			user = null;
			return Utility.constructJSON(Constants.USERS, false,
					Constants.LOGOUT_ERROR);
		}

		user.setGcmid("");
		try {
			em.merge(user);
		} catch (Exception e) {
			user = null;
			e.printStackTrace();
			return Utility.constructJSON(Constants.USERS, false,
					Constants.PERSISTANCE_ERROR);
		}
		return Utility.constructJSON(Constants.USERS, true);
	}

	@GET
	@Path("dohash/{password}")
	public String doHash(@PathParam("password") String password) {
		String str = PasswordEncrypterSHABase64.encryptPassword(password);
		return str;
	}

	@GET
	@Path("doupdategcmid")
	@Produces("application/json")
	public String doUpdateGCM(@QueryParam("idUser") String idUser,
			@QueryParam("gcmid") String gcmid) {
		User user = null;
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :idParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("idParam", Integer.parseInt(idUser));
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			user = null;
		}
		user.setGcmid(gcmid);
		try {
			em.merge(user);
		} catch (Exception e) {
			user = null;
			e.printStackTrace();
		}
		String resul = "";
		if (user != null) {
			resul = "success";
		} else {
			resul = "failed";
		}
		return resul;
	}

	@GET
	@Path("doupdatepicture")
	@Produces("application/json")
	public String doUpdatePicture(@QueryParam("idUser") String idUser,
			@QueryParam("pictureFull") String pictureFull,
			@QueryParam("pictureThumb") String pictureThumb) {
		User user = null;
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :idParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("idParam", Integer.parseInt(idUser));
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			user = null;
		}
		user.setPictureFull(pictureFull);
		user.setPictureThumb(pictureThumb);
		try {
			em.merge(user);
		} catch (Exception e) {
			user = null;
			e.printStackTrace();
		}
		if (user != null) {
			return Utility.constructJSON(Constants.USERS, true);
		} else {
			return Utility.constructJSON(Constants.USERS, false,
					Constants.IMAGE_ERROR);
		}
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(User entity) {
		entity = em.merge(entity);
		return Response.noContent().build();
	}
}