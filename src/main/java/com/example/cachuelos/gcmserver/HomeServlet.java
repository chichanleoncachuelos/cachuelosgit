package com.example.cachuelos.gcmserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.cachuelosfrontend.model.User;

/**
 * Servlet that adds display number of devices and button to send a message.
 * <p>
 * This servlet is used just by the browser (i.e., not device) and contains the
 * main page of the demo app.
 */
@SuppressWarnings("serial")
@WebServlet(name = "HomeServlet",urlPatterns = {"/home"},loadOnStartup=1)
public class HomeServlet extends BaseServlet {

	static final String ATTRIBUTE_STATUS = "status";
	
	@PersistenceContext(unitName = "forge-default")
	private EntityManager em;

	/**
	 * Displays the existing messages and offer the option to send a new one.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

		out.print("<html><body>");
		out.print("<head>");
		out.print("  <title>Cachuelo Mensaje Comunitario</title>");
		out.print("  <link rel='icon' href='favicon.png'/>");
		out.print("</head>");
		String status = (String) req.getAttribute(ATTRIBUTE_STATUS);
		if (status != null) {
			out.print(status);
		}
//		List<String> devices = new ArrayList<String>();
		final List<User> results = em.createQuery(
				"SELECT DISTINCT u FROM User u ORDER BY u.idUser", User.class)
				.getResultList();
		// String
		// str="APA91bHVLOTNoANYxH-NYS2ukta28xlUb7Yzbwk0aujWsh6Ol8UaJ75a95OR5j0g8PSbD1nR-Oa0jzWMuMiVgJEG-TI7L54fRTGocSqRGNkxeIqXQTghVI8TndZoKO4o0iUS55o9_TVmCQW8vIBI-fQh25QP4Bp0zQ";
		// devices.add(str);
		// try {
		// devices = DBConnection.getAllUsers();
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		if (results.isEmpty()) {
			out.print("<h2>No hay usuarios registrados!</h2>");
		} else {
			out.print("<h2>" + results.size()+ " usuario(s) registrados!</h2>");
			out.print("<form name='form' method='POST' action='sendAll'>");
			out.print("<div> <textarea rows='2' name='message' cols='23' placeholder='Mensaje a transmitir a la comunidad'>");
			out.print("</textarea> </div>");
			out.print("<input type='submit' value='Enviar Mensaje' />");
			out.print("<a href='/faces/index.xhtml'>Regresar</a>");			
			out.print("</form>");
		}
		out.print("</body></html>");// hhh
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doGet(req, resp);
	}

}
