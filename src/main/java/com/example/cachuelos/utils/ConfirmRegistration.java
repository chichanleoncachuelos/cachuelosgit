package com.example.cachuelos.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import com.example.cachuelosfrontend.model.User;

@WebServlet(name = "ConfirmRegistration", urlPatterns = { "/validateRegister.do" })
public class ConfirmRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// ESTE SERVICIO NO ES REST
	/**
	 * @see HttpServlet#HttpServlet()
	 */

	@Resource
	UserTransaction tx;
	
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	public ConfirmRegistration() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside Confirmation begin");

		String secretkey = "";
		try {
			Map<String, String> paramMap = ConfirmRegistration
					.getQueryParams(request.getQueryString());
			secretkey = paramMap.get("secretkey");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (secretkey != null) {
			System.out.println("Secret Ok");
		} else {
			System.out.println("Regisrtation Confirmation Failed");
			response.sendRedirect("RegistrationConfirmationError.jsp");
			return;
		}
		if (secretkey.compareTo("") == 0) {
			System.out.println("Regisrtation Confirmation Failed");
			response.sendRedirect("RegistrationConfirmationError.jsp");
			return;
		} else {
			System.out.println("Secret ok");
		}

		User thisUser;
		thisUser = confirmRegistration(secretkey);
		HttpSession session = request.getSession(true);
		if (thisUser != null) {
			// response.sendRedirect("RegistrationConfirmation.jsp");
			session.setAttribute("name", thisUser.getName());
			session.setAttribute("lastname", thisUser.getLastName());
			session.setAttribute("email", thisUser.getEmail());
			response.sendRedirect(calculateURL(request));
		} else {
			System.out.println("Regisrtation Confirmation Failed");
			response.sendRedirect("RegistrationConfirmationError.jsp");
		}
		System.out.println("Inside Confirmation success");
	}

	private String calculateURL(HttpServletRequest request) {
		// TODO Auto-generated method stub
		String loginURL = request.getContextPath() + "/faces/mail/view.xhtml";
		return loginURL;
	}

	private User confirmRegistration(String secretKey) {
		User user=null;
		TypedQuery<User> findByIdQuery = em
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.secretkey = :secretParam ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("secretParam", secretKey);
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			user = null;
			return null;
		}
		user.setIsActive(1);

		try {
			tx.begin();
			em.merge(user);
			tx.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return user;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * This method will return the number of parameters in the request url with
	 * its values
	 * 
	 * @author come2niks
	 * @param url
	 * @return
	 */
	private static Map<String, String> getQueryParams(String query) {
		try {
			Map<String, String> params = new HashMap<String, String>();
			if (query.length() > 1) {
				for (String param : query.split("&")) {
					String[] pair = param.split("=");
					String key = URLDecoder.decode(pair[0], "UTF-8");
					String value = "";
					if (pair.length > 1) {
						value = URLDecoder.decode(pair[1], "UTF-8");
					}
					params.put(key, value);
				}
			}

			return params;
		} catch (UnsupportedEncodingException ex) {
			throw new AssertionError(ex);
		}
	}
}