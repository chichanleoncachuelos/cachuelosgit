package com.example.cachuelosfrontend.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.Executor;
//import java.util.TimerTask;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.enterprise.context.*;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.UserTransaction;

import com.example.cachuelos.view.ApplicationBean;
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.Message;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.utils.Constants;
import com.example.cachuelosfrontend.utils.PasswordEncrypter;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.PictureSize;
import facebook4j.Reading;
import facebook4j.auth.AccessToken;

import javax.faces.event.ActionEvent;

import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

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
@SessionScoped
public class SimpleLoginBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	ApplicationBean applicationBean;

	String loginname;
	String password;
	String loggedInStatus = "false";
	String messageLogin = "";
	String messageRegister = "";
	String messageAcceptOffer = "";
	String messageCommentRating = "";
	String apiKey = "";
	String appId = "";
	String appSecret = "";
	User userLogged;
	int intentCount = 0;
	int intentCountAcceptOffer = 0;
	int intentCountCommentRating = 0;
	int hasToFacebookLogout = 0;
	Cachuelo cachueloToComment;
	boolean gotKickedOut = false;
    private String facebookid = "";
	private String facebooktoken = "";
	private String facebookemail = "";
	private String facebookidTmp = "";
	private String facebooktokenTmp = "";
	private String facebookemailTmp = "";
	private String facebookurlpicture = "";

	public String getFacebookid() {
		return facebookid;
	}

	public void setFacebookid(String facebookid) {
		this.facebookid = facebookid;
	}

	public String getFacebooktoken() {
		return facebooktoken;
	}

	public void setFacebooktoken(String facebooktoken) {
		this.facebooktoken = facebooktoken;
	}

	public String getFacebookidTmp() {
		return facebookidTmp;
	}

	public void setFacebookidTmp(String facebookidTmp) {
		this.facebookidTmp = facebookidTmp;
	}

	public String getFacebooktokenTmp() {
		return facebooktokenTmp;
	}

	public void setFacebooktokenTmp(String facebooktokenTmp) {
		this.facebooktokenTmp = facebooktokenTmp;
	}

	public String getFacebookemailTmp() {
		return facebookemailTmp;
	}

	public void setFacebookemailTmp(String facebooemailTmp) {
		this.facebookemailTmp = facebooemailTmp;
	}
	public String getFacebookemail() {
		return facebookemail;
	}

	public void setFacebookemail(String facebookemail) {
		this.facebookemail = facebookemail;		
	}
	public String getFacebookurlpicture() {
		return facebookurlpicture;
	}

	public void setFacebookurlpicture(String facebookurlpicture) {
		this.facebookurlpicture = facebookurlpicture;
	}
	public int getHasToFacebookLogout() {
		return hasToFacebookLogout;
	}

	public void setHasToFacebookLogout(int hasToFacebookLogout) {
		this.hasToFacebookLogout = hasToFacebookLogout;
	}

	protected List<Message> allMyMessages = new ArrayList<Message>();// todos
																		// los
																		// mensajes
	// donde el usuario
	// logeado es
	// remitente o
	// receptor
	protected List<Message> myChatList = new ArrayList<Message>();// lista de
																	// cada
	// conversacion en la
	// que el usuario esta
	// involucrado
	
	Timer timer;
	
	@Resource
	UserTransaction tx;

	@PersistenceContext(type = PersistenceContextType.EXTENDED)
	private EntityManager em;

	@PostConstruct
	private void init() {
		System.out.println("constructor");
		simpleModel = new DefaultMapModel();
		apiKey = applicationBean.getApiKey();
		appId = applicationBean.getAppId();
		appSecret = applicationBean.getAppSecret();
		listCachueloLocation = new ArrayList<Cachuelo>();
	}

	public List<Message> getMyChatList() {
		return myChatList;
	}

	public void setMyChatList(List<Message> myChatList) {
		this.myChatList = myChatList;
	}

	public List<Message> getAllMyMessages() {
		return allMyMessages;
	}

	public void setAllMyMessages(List<Message> allMyMessages) {
		this.allMyMessages = allMyMessages;
	}

	// ///////////////////////////////////

	public int getIntentCountCommentRating() {
		return intentCountCommentRating;
	}

	public void setIntentCountCommentRating(int intentCountCommentRating) {
		this.intentCountCommentRating = intentCountCommentRating;
	}

	public String getMessageCommentRating() {
		return messageCommentRating;
	}

	public void setMessageCommentRating(String messageCommentRating) {
		this.messageCommentRating = messageCommentRating;
	}

	public Cachuelo getCachueloToComment() {
		return cachueloToComment;
	}

	public void setCachueloToComment(Cachuelo cachueloToComment) {
		this.cachueloToComment = cachueloToComment;
	}

	public int getIntentCountAcceptOffer() {
		return intentCountAcceptOffer;
	}

	public void setIntentCountAcceptOffer(int intentCountAcceptOffer) {
		this.intentCountAcceptOffer = intentCountAcceptOffer;
	}

	public String getMessageRegister() {
		return messageRegister;
	}

	public void setMessageRegister(String messageRegister) {
		this.messageRegister = messageRegister;
		intentCount = 0;
	}

	public String getMessageAcceptOffer() {
		return messageAcceptOffer;
	}

	public void setMessageAcceptOffer(String messageAcceptOffer) {
		this.messageAcceptOffer = messageAcceptOffer;
	}

	public String getMessageLogin() {
		return messageLogin;
	}

	public void setMessageLogin(String messageLogin) {
		this.messageLogin = messageLogin;
	}

	public String getLoggedInStatus() {
		return loggedInStatus;
	}

	public void setLoggedInStatus(String loggedInStatus) {
		this.loggedInStatus = loggedInStatus;
	}

	public String getLoginname() {
		return loginname;
	}

	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public User getUserLogged() {
		return userLogged;
	}

	public void setUserLogged(User userLogged) {
		this.userLogged = userLogged;
	}

	public boolean isGotKickedOut() {
		return gotKickedOut;
	}

	public void setGotKickedOut(boolean gotKickedOut) {
		this.gotKickedOut = gotKickedOut;
	}

	public String logOut() {
		userLogged = null;
		loginname = "";
		if (facebookid.length()!=0) {
			hasToFacebookLogout=1;
			facebookid="";
		}
		loggedInStatus = "false";
		ExternalContext context = FacesContext.getCurrentInstance()
				.getExternalContext();
		allMyMessages.clear();
		myChatList.clear();

		try {
			context.redirect(context.getRequestContextPath()
					+ "/faces/frontend/frontend.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
			messageLogin = Constants.LOGOUT_ERROR;
			return "failed";
		}
		messageLogin = "";
		return "success";
	}

	public String checkValidUser() {
		intentCount = 0;
		hasToFacebookLogout=0;
		messageRegister = "";
		userLogged = doLogin(loginname,
				PasswordEncrypter.encryptPassword(password));// return
																// "success";
		if (userLogged != null) {
			System.out.println("login success");
			setLoggedInStatus("true");
			this.gotKickedOut = false;
			messageLogin = "";
			executeLocationQuery();
			return "success";
		} else {
			System.out.println("login failed");
			setLoggedInStatus("false");
			messageLogin = Constants.LOGIN_ERROR;
			return "fail";
		}
	}

	public User doLogin(String email, String password) {
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
			return null;
		}
		return user;
	}

	public void checkIfLoginStatusIsFalse() {
		String str = loggedInStatus + " / " + loginname;
		System.out.println("simpleLoginStatus: " + str);
		if (loggedInStatus.compareTo("false") == 0) {
			ExternalContext context = FacesContext.getCurrentInstance()
					.getExternalContext();
			try {
				context.redirect(context.getRequestContextPath()
						+ "/faces/frontend/frontend.xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void checkIfLoginStatusIsTrue() {		
		intentCount++;
		if (loggedInStatus.compareTo("true") == 0) {
			ExternalContext context = FacesContext.getCurrentInstance()
					.getExternalContext();
			try {
				context.redirect(context.getRequestContextPath()
						+ "/faces/frontend/homecachuelos/view.xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (intentCount >= 2) {
			intentCount = 0;
			messageLogin = "";
			messageRegister = "";
		}
		if (gotKickedOut) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Mensaje Importante:", null));
			FacesMessage message = new FacesMessage(
					"Su sesion ha sido cerrada", "Vuelva a iniciar la sesion");
			FacesContext.getCurrentInstance().addMessage(null, message);
			this.gotKickedOut = false;
		}
	}

	public void checkIfThereIsMessageAcceptedOffer() {
		intentCountAcceptOffer++;
		if (intentCountAcceptOffer >= 2) {
			intentCountAcceptOffer = 0;
			messageAcceptOffer = "";
		}
	}

	public void checkIfThereIsMessageCommentRating() {
		intentCountCommentRating++;
		if (intentCountCommentRating >= 2) {
			intentCountCommentRating = 0;
			messageCommentRating = "";
		}
	}

	public List<Message> listMyChats() {
		final List<Message> resultsPartial = em.createQuery(
				"SELECT DISTINCT m FROM Message m ORDER BY m.idMessage",
				Message.class).getResultList();
		myChatList.clear();
		allMyMessages.clear();
		for (Message message : resultsPartial) {
			if (message.getUserByIdReceiver().getIdUser() == userLogged
					.getIdUser()
					|| message.getUserByIdUserSender().getIdUser() == userLogged
							.getIdUser()) {
				message.setUserToReply(findUserToReply(message, userLogged));
				allMyMessages.add(message);// todos mis mensajes en un solo
											// array
				if (!isElementOnList(myChatList, message)) {
					myChatList.add(message);// mi lista de chats
				}
			}
		}
		return myChatList;
	}

	private boolean isElementOnList(List<Message> myChatList, Message message) {
		boolean result = false;
		for (Message messageItem : myChatList) {
			User userToReplyFromChat = findUserToReply(messageItem, userLogged);
			User userToReplyFromMessage = findUserToReply(message, userLogged);
			if (message.getCachuelo().getIdCachuelo() == messageItem
					.getCachuelo().getIdCachuelo()
					&& userToReplyFromChat.getIdUser() == userToReplyFromMessage
							.getIdUser()) {
				return true;
			}
		}
		return result;
	}

	private User findUserToReply(Message message, User userLogged) {
		User userResul;
		if (message.getUserByIdReceiver().getIdUser() == userLogged.getIdUser()) {
			userResul = message.getUserByIdUserSender();
		} else {
			userResul = message.getUserByIdReceiver();
		}
		return userResul;
	}

	private static final Executor threadPool = Executors.newFixedThreadPool(5);

	public void listMyMessagesForChat(ActionEvent actionEvent) {
		System.out.println("timer simulation begin");
		threadPool.execute(new Runnable() {
			public void run() {
				if (loggedInStatus.compareTo("true") == 0) {
					System.out.println("timer simulation inside");
					// if (newMessageArrived()) {
					listMyChats();
				}
			}
		});
		System.out.println("timer simulation end");
	}

	public boolean newMessageArrived() {
		boolean boolResul = false;
		if (countMessages() > allMyMessages.size()) {
			boolResul = true;
		}
		return boolResul;
	}

	private long countMessages() {
		long count = 0;
		BigInteger bigIntegerCount;
		bigIntegerCount = (BigInteger) em
				.createNativeQuery(
						"select count(1) from Message m where m.idUserSender = :idUserSenderParam or m.idReceiver = :idReceiverParam")
				.setParameter("idUserSenderParam",
						userLogged.getIdUser().toString())
				.setParameter("idReceiverParam",
						userLogged.getIdUser().toString()).getSingleResult();
		count = bigIntegerCount.longValue();
		System.out.println("msg count" + count);
		return count;
	}

	private MapModel simpleModel;
	private Marker marker;

	public MapModel getSimpleModel() {
		return simpleModel;
	}

	public void setSimpleModel(MapModel simpleModel) {
		this.simpleModel = simpleModel;
	}

	public void onMarkerSelect(OverlaySelectEvent event) {
		marker = (Marker) event.getOverlay();
		int tmp = executeMarkerSelectedQuery();
		FacesContext.getCurrentInstance().addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Mensaje Importante:", null));
		if (tmp == 1) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Cachuelo: "
							+ marker.getTitle(), null));
		} else {
			FacesContext
					.getCurrentInstance()
					.addMessage(
							null,
							new FacesMessage(
									FacesMessage.SEVERITY_INFO,
									"Mensaje: Hay mas de un cachuelo en esta ubicacion",
									null));
		}
	}

	public void executeLocationQuery() {
		List<Cachuelo> listCachuelo = getMine();
		simpleModel.getMarkers().clear();
		List<Cachuelo> listCachueloTmp = new ArrayList<Cachuelo>();

		for (Cachuelo cachuelo : listCachuelo) {
			if (cachuelo.getLocation() != null) {
				listCachueloTmp.add(cachuelo);
			}

		}
		for (Cachuelo cachueloTmp : listCachueloTmp) {
			LatLng coord = new LatLng(cachueloTmp.getLocation().getLat(),
					cachueloTmp.getLocation().getLng());
			Marker marker = new Marker(coord, cachueloTmp.getName());
			Object data = calculateArrayLocations(cachueloTmp.getLocation(),
					listCachuelo);
			marker.setData(data);
			simpleModel.addOverlay(marker);
		}
	}

	private Object calculateArrayLocations(Location location, List<Cachuelo> listCachuelo) {
		List<Cachuelo> listCachueloPart = new ArrayList<Cachuelo>();
		for (Cachuelo cachuelo : listCachuelo) {
			if (cachuelo.getLocation() != null) {
				if (cachuelo.getLocation().getIdLocation() == location
						.getIdLocation()) {
					listCachueloPart.add(cachuelo);
				}
			}
		}
		return listCachueloPart;
	}

	@SuppressWarnings("unchecked")
	public int executeMarkerSelectedQuery() {
		listCachueloLocation.clear();
		List<Cachuelo> listCachueloPart = ((List<Cachuelo>) marker.getData());
		for (Cachuelo cachuelo : listCachueloPart) {
			listCachueloLocation.add(cachuelo);
		}
		return listCachueloLocation.size();
	}

	private List<Cachuelo> listCachueloLocation;

	public List<Cachuelo> getListCachueloLocation() {
		return listCachueloLocation;
	}

	public void setListCachueloLocation(List<Cachuelo> listCachueloLocation) {
		this.listCachueloLocation = listCachueloLocation;
	}

	public List<Cachuelo> getMine() {
		List<Cachuelo> list;
		List<Cachuelo> listFiltered = new ArrayList<Cachuelo>();
		CriteriaQuery<Cachuelo> criteria = this.em.getCriteriaBuilder()
				.createQuery(Cachuelo.class);
		list = this.em.createQuery(
				criteria.select(criteria.from(Cachuelo.class))).getResultList();
		for (Cachuelo cach : list) {
			if (this.userLogged != null) {
				if (cach.getUser().getIdUser() == this.userLogged.getIdUser()) {
					listFiltered.add(cach);
				}
			}
		}
		return listFiltered;
	}

	public void facebookCompleted(String facebookidParam,
			String facebooktokenParam, String facebookemailParam) {
		System.out.println("Facebook antes: id:" + facebookidTmp + " email:"
				+ facebookemailTmp);
		System.out.println("Facebook recibido: id:" + facebookidParam
				+ " email:" + facebookemailParam);
		facebookid = "" + facebookidParam;
		facebooktoken = "" + facebooktokenParam;
		facebookemail = "" + facebookemailParam;
		// useFacebook(facebooktoken);
		//useFacebook("CAACEdEose0cBAPDXlfNT2hlZA8ZCf1mVHl7jxlwyXJqFZBzBHuuR7dNjOZAvnZAfEcjoirmSzBmRuqHp4JsjkYsK4YD8X7AkYU2d7Aql9gW5EbEZC91gudJ4HHJw5XeSOYRGqZB29OMzgcJBcXzFop57FxbWUEevwaIEJRHf3FKUAmzJHUEGUEDNVIRhXYOE5tu4BdGFrUBdQZDZD");
		checkValidUserFacebook();
	}

	public String checkValidUserFacebook() {
		hasToFacebookLogout=0;
		intentCount = 0;
		messageRegister = "";
		userLogged = doLoginFacebook();// return
										// "success";
		if (userLogged != null) {
			System.out.println("login facebook success");
			setLoggedInStatus("true");
			this.gotKickedOut = false;
			messageLogin = "";
			executeLocationQuery();
			return "success";
		} else {
			System.out.println("login facebook failed");
			setLoggedInStatus("false");
			messageLogin = Constants.LOGIN_ERROR;
			return "fail";
		}
	}

	public User doLoginFacebook() {
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
		}
		if (user != null)
			return updateFacebookUser(user);
		else {
			user = registerUserFacebook();
			this.setMessageRegister(Constants.REGISTER_SUCCESS);
			return user;
		}

	}
	
	private User updateFacebookUser(User us) {
		Facebook facebook = new FacebookFactory().getInstance();
		facebook.setOAuthAppId(appId, appSecret);
		facebook.setOAuthPermissions("email,publish_actions");
		facebook.setOAuthAccessToken(new AccessToken(facebooktoken, null));
		String nameFacebook ="";
		String emailFacebook ="";
		String urlFacebookPicture ="";
		try {
			facebook4j.User user = facebook.getMe();
			facebook4j.User user2 = facebook.getUser(facebookid, new Reading().fields("email"));
			nameFacebook=""+user.getName();
			emailFacebook=""+user2.getEmail();
			URL largePic = facebook.getPictureURL(facebookid,PictureSize.large);
			urlFacebookPicture=""+largePic.toString();
			facebookurlpicture=""+urlFacebookPicture;
			System.out.println("Mi nombre:" + nameFacebook );
			System.out.println("Mi email2:" + emailFacebook);
			System.out.println("Mi url:" + urlFacebookPicture);
//			facebook.postStatusMessage("Ya funciona Facebook Cachuelos");
		} catch (FacebookException e) {
			e.printStackTrace();
		}
		String name = "";
		String lastName = "";
		if (nameFacebook.contains(" ")) {
			name     = nameFacebook.substring(0, nameFacebook.indexOf(" "));
			lastName = nameFacebook.substring(nameFacebook.indexOf(' ')+1);
		}else{
			name=""+nameFacebook;
			lastName="NoLastName";
		}		
		us.setName(name);
		us.setLastName(lastName);
		us.setEmail(emailFacebook);
		us.setFacebooktoken(facebooktoken);
		try {
			tx.begin();
			em.merge(us);
			tx.commit();
		} catch (Exception e) {
			System.out.println("Error Facebook update:");
			return null;
		}
		return us;
	}

	private User registerUserFacebook() {
		Facebook facebook = new FacebookFactory().getInstance();
		facebook.setOAuthAppId(appId, appSecret);
		facebook.setOAuthPermissions("email,publish_actions");
		facebook.setOAuthAccessToken(new AccessToken(facebooktoken, null));
		String nameFacebook ="";
		String emailFacebook ="";
		String urlFacebookPicture ="";
		try {
			facebook4j.User user = facebook.getMe();
			facebook4j.User user2 = facebook.getUser(facebookid, new Reading().fields("email"));
			nameFacebook=""+user.getName();
			emailFacebook=""+user2.getEmail();
			URL largePic = facebook.getPictureURL(facebookid,PictureSize.large);
			urlFacebookPicture=""+largePic.toString();
			facebookurlpicture=""+urlFacebookPicture;
			System.out.println("Mi nombre:" + nameFacebook );
			System.out.println("Mi email2:" + emailFacebook);
			System.out.println("Mi url:" + urlFacebookPicture);
//			facebook.postStatusMessage("Ya funciona Facebook Cachuelos");
		} catch (FacebookException e) {
			e.printStackTrace();
		}
		String name = "";
		String lastName = "";
		if (nameFacebook.contains(" ")) {
			name     = nameFacebook.substring(0, nameFacebook.indexOf(" "));
			lastName = nameFacebook.substring(nameFacebook.indexOf(' ')+1);
		}else{
			name=""+nameFacebook;
			lastName="NoLastName";
		}		
		User us = new User(name, lastName, emailFacebook, 0, 1, " ", null);//1 is worker /0 is not active/ null datestamp
		String secretkey = UUID.randomUUID().toString();
		us.setSecretkey(secretkey);
		us.setFacebookid(facebookid);
		us.setFacebooktoken(facebooktoken);
		us.setDni("00000000");
		Zone zone = em.find(Zone.class, 1);
		us.setZone(zone);
		try {
			tx.begin();
			em.persist(us);
			tx.commit();
		} catch (Exception e) {
			System.out.println("Error Facebook register:");
			return null;
		}
		return us;
	}
	public void saveFacebookPicture() {
		try {
			URL url = new URL(facebookurlpicture);
			InputStream is = url.openStream();
			String secretKey = UUID.randomUUID().toString()+"face.jpg";	
			FileOutputStream os = new FileOutputStream(System.getenv("OPENSHIFT_DATA_DIR") + secretKey);
			byte[] b = new byte[2048];
			int length;
			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}
			is.close();
			os.flush();
			os.close();
			userLogged.setPictureFull(secretKey);
			resize(secretKey , 0.15);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void resize(String inputImageUrl, int scaledWidth, int scaledHeight)
			throws IOException {
		String secretKey = UUID.randomUUID().toString();

		// reads input image
		java.io.File inputFile = new java.io.File(System.getenv("OPENSHIFT_DATA_DIR") + inputImageUrl);
		BufferedImage inputImage = ImageIO.read(inputFile);

		// creates output image
		BufferedImage outputImage = new BufferedImage(scaledWidth,
				scaledHeight, inputImage.getType());

		// scales the input image to the output image
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
		g2d.dispose();

		// extracts extension of output file
		String formatName = inputImageUrl.substring(inputImageUrl
				.lastIndexOf(".") + 1);

		// writes to output file
		ImageIO.write(outputImage, formatName,
				new java.io.File(System.getenv("OPENSHIFT_DATA_DIR")+ secretKey + inputImageUrl));
		userLogged.setPictureThumb(secretKey + inputImageUrl);
		try {
			tx.begin();
			em.merge(userLogged);
			tx.commit();
		} catch (Exception e) {
			System.out.println("Error Facebook update:");
		}
	}

	public void resize(String inputImageUrl, double percent) throws IOException {
		java.io.File inputFile = new java.io.File(System.getenv("OPENSHIFT_DATA_DIR") + inputImageUrl);
		BufferedImage inputImage = ImageIO.read(inputFile);
		int scaledWidth = (int) (inputImage.getWidth() * percent);
		int scaledHeight = (int) (inputImage.getHeight() * percent);
		resize(inputImageUrl, scaledWidth, scaledHeight);
	}

	protected void useFacebook(String accessToken) {
		Facebook facebook = new FacebookFactory().getInstance();
		facebook.setOAuthAppId(appId, appSecret);
		facebook.setOAuthAccessToken(new AccessToken(accessToken, null));
		try {
			facebook4j.User user = facebook.getMe();
			System.out.println("Mi nombre:" + user.getName());
			//facebook.postStatusMessage("ya funciona Facebook Cachuelos");
		} catch (FacebookException e) {
			e.printStackTrace();
		}
	}

	@Remove
	public void beanRemoved() {
		userLogged = null;
		loginname = "";
		loggedInStatus = "false";
		this.gotKickedOut = false;
		System.out.println("sesion expired");
	}

	@PreDestroy
	public void beanDestroyed() {
		userLogged = null;
		loginname = "";
		loggedInStatus = "false";
		this.gotKickedOut = false;
		System.out.println("sesion expired");
	}
}