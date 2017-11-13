package com.example.cachuelos.gcmserver;

import com.example.cachuelosfrontend.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that adds a new message to all registered devices.
 * <p>
 * This servlet is used just by the browser (i.e., not device).
 */
@SuppressWarnings("serial")
@WebServlet(name = "SendAllMessagesServlet", urlPatterns = { "/sendAll" }, loadOnStartup = 1)
public class SendAllMessagesServlet extends BaseServlet {

	private static final int MULTICAST_SIZE = 1000;

	private String apikey;

	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		newSender(config);
	}

	/**
	 * Creates the {@link Sender} based on the servlet settings.
	 */
	protected void newSender(ServletConfig config) {
		apikey = (String) config.getServletContext().getAttribute(
				ApiKeyInitializer.getAttributeAccessKey());
	}

	/**
	 * Processes the request to add a new message.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		String userMessage = "defaultMessage";
		final List<User> results = em.createQuery(
				"SELECT DISTINCT u FROM User u ORDER BY u.idUser", User.class)
				.getResultList();

		String status;
		if (results.isEmpty()) {
			status = "Message ignorado no hay dispositivos registrados!";
		} else {
			userMessage = req.getParameter("message");
			int total = results.size();
			int subTotal = 0;
			List<String> partialDevices = new ArrayList<String>(total);
			int counter = 0;
			int tasks = 0;
			for (User user : results) {
				counter++;
				if (user.getGcmid() != null) {
					if (user.getGcmid().compareTo("") != 0) {
						subTotal++;
						partialDevices.add(user.getGcmid());
					}
				}
				int partialSize = partialDevices.size();
				if (partialSize == MULTICAST_SIZE || counter == total) {
					GCMSenderJson senderJson = new GCMSenderJson();
					senderJson.sendMessage(partialDevices, userMessage, apikey);
					// asyncSend(partialDevices, userMessage);
					partialDevices.clear();
					tasks++;
				}
			}
			status = "Enviando asincronamente " + tasks
					+ " mensaje(s) multicast a " + subTotal + " dispositivos";
		}
		req.setAttribute(HomeServlet.ATTRIBUTE_STATUS, status.toString());
		getServletContext().getRequestDispatcher("/home").forward(req, resp);
	}

}
