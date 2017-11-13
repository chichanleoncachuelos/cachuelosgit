package com.example.cachuelos.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.*;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.criteria.CriteriaQuery;

import com.example.cachuelos.gcmserver.GCMSenderJson;
import com.example.cachuelos.utils.Utility;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.utils.Constants;


/**
 * Backing bean for User entities.
 * <p>
 * This class provides CRUD functionality for all User entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@ApplicationScoped
public class ApplicationBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final int MULTICAST_SIZE = 1000;

	private String apiKey = "";
	private static final String PATH = "/api.key";

	private String appId = "548974548583355";
	private String appSecret = "75d296eb64204bba907238e1260ea682";

	private String ipAddress = "";
	private String reportIpAddress = "";

	@PersistenceContext(type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	private void getIP() {
		//this.ipAddress = "" + "cachuelos-cachuelos.rhcloud.com";
		//this.reportIpAddress = "" + "jrs47-cachuelos.rhcloud.com";
		this.ipAddress = "" + "190.237.3.19:8088/cachuelos";
		this.reportIpAddress = "" + "jrs47-cachuelos.rhcloud.com";
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getReportIpAddress() {
		return reportIpAddress;
	}

	public void setReportIpAddress(String reportIpAddress) {
		this.reportIpAddress = reportIpAddress;
	}
	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	protected String getKey() {
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(PATH);
		if (stream == null) {
			throw new IllegalStateException("Could not find file " + PATH
					+ " on web resources)");
		}
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		try {
			String key = reader.readLine();
			apiKey = key;
			return key;
		} catch (IOException e) {
			throw new RuntimeException("Could not read file " + PATH, e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				System.out.println("eror api key handled:");
			}
		}
	}

	@PostConstruct
	public void applicationInitialized() {
		getIP();
		getKey();
		//getFacebook();
		// TimerGCM();
	}

	final static DateFormat fmt = DateFormat.getTimeInstance(DateFormat.LONG);

	@SuppressWarnings("unused")
	protected void TimerGCM() {
		// Create a scheduled thread pool with 5 core threads
		ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor) Executors
				.newScheduledThreadPool(5);
		Runnable periodicTask = new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("\t periodicTask Execution Time: "
							+ fmt.format(new Date()));
					sendHeartBeat();
					System.out.println("\t periodicTask End Time: "
							+ fmt.format(new Date()));
				} catch (Exception e) {

				}
			}
		};
		System.out.println("Submission Time: " + fmt.format(new Date()));
		ScheduledFuture<?> periodicFuture = sch.scheduleAtFixedRate(
				periodicTask, 0, 120, TimeUnit.SECONDS);
	}

	protected void sendHeartBeat() {
		System.out.println("Sending GCM HeartBeat");
		final List<User> results = getAllUsers();

		String strJson = Utility.constructJSON(Constants.TAG_GCM_HEARTBEAT);

		String status;
		if (results.isEmpty()) {
			status = "Message ignorado no hay dispositivos registrados!";
		} else {
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
					senderJson
							.sendMessage(partialDevices, strJson, this.apiKey);
					// asyncSend(partialDevices, userMessage);
					partialDevices.clear();
					tasks++;
				}
			}
			status = "Enviando asincronamente " + tasks
					+ " mensaje(s) multicast a " + subTotal + " dispositivos";
			System.out.println(status);
		}
	}

	private List<User> getAllUsers() {
		CriteriaQuery<User> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(User.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(User.class))).getResultList();
	}

	@PreDestroy
	public void applicationDestroyed() {
	}
}