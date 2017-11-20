package com.example.cachuelosfrontend.view;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Cachuelostate;
import com.example.cachuelosfrontend.model.Commentrating;
import com.example.cachuelosfrontend.model.Imagecachuelo;
import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.Message;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.Typecachuelo;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.model.Workerbytypecachuelo;
import com.example.cachuelosfrontend.utils.Constants;
import com.example.cachuelos.gcmserver.GCMSenderJson;
import com.example.cachuelos.utils.Utility;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Backing bean for Cachuelo entities.
 * <p>
 * This class provides CRUD functionality for all Cachuelo entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class FrontCachueloBean implements Serializable {

	private static final long serialVersionUID = 1L;

	protected final Logger logger = Logger.getLogger(getClass().getName());
	private static final int MULTICAST_SIZE = 1000;
	private String apikey = "";

	protected void newSender() {
		apikey = simpleLoginBean.getApiKey();
		System.out.println("sendMsg safely using KEY");
	}

	@Inject
	SimpleLoginBean simpleLoginBean;

	@PostConstruct
	private void initialize() {
		newSender();
	}



//	User userLogged;

	List<Commentrating> filteredCommentRatings;

	public List<Commentrating> getFilteredCommentRatings() {
		return filteredCommentRatings;
	}

	public void setFilteredCommentRatings(
			List<Commentrating> filteredCommentRatings) {
		this.filteredCommentRatings = filteredCommentRatings;
	}

	/*
	 * Support creating and retrieving Cachuelo entities
	 */

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Cachuelo cachuelo;

	public Cachuelo getCachuelo() {
		return this.cachuelo;
	}

	@Inject
	private Conversation conversation;

	@PersistenceContext(type = PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	public String create() {

		this.conversation.begin();
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		if (FacesContext.getCurrentInstance().isPostback()) {
			return;
		}

		if (this.conversation.isTransient()) {
			this.conversation.begin();
		}

		if (this.id == null) {
			this.cachuelo = this.example;
			this.cachuelo.setName(" ");
		} else {
			this.cachuelo = findById(getId());
			this.filteredCommentRatings = filterCommentRatings(this.cachuelo
					.getCommentratings());
			selectedDate = calculateDateFromString(this.cachuelo
					.getToBeCompleteAt());
			simpleLoginBean.setCachueloToComment(this.cachuelo);
		}

		locale = Locale.US;// MMM d, yyyy
		popup = true;// dd/M/yy hh:mm a
		pattern = "dd/MM/yyyy hh:mm a";
		jointPoint = "bottomleft";
		direction = "bottomright";
		readonly = true;
		enableManualInput = false;
		showInput = true;
		boundary = "inactive";
	}

	private List<Commentrating> filterCommentRatings(
			Set<Commentrating> commentratings) {
		List<Commentrating> lstResul = new ArrayList<Commentrating>();
		for (Commentrating commentrating : commentratings) {
			if (commentrating.getCommentratingtype().getIdCommentType() == 1) {
				lstResul.add(commentrating);
			}
		}
		return lstResul;
	}

	public Cachuelo findById(Integer id) {

		return this.entityManager.find(Cachuelo.class, id);
	}

	/*
	 * Support updating and deleting Cachuelo entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (simpleLoginBean.getUserLogged() != null) {
				if (this.id == null) {
					this.cachuelo.setUser(simpleLoginBean.getUserLogged());
					String strDate = calculateConvertedDate(selectedDate);
					this.cachuelo.setToBeCompleteAt(strDate);
					this.cachuelo.setCachuelostate(getCachueloState("1"));
					this.entityManager.persist(this.cachuelo);
					insertImages(this.cachuelo);
					sendCachueloToPhone(this.cachuelo.getUser(), cachuelo,
							this.cachuelo.getTypecachuelo());
					simpleLoginBean.executeLocationQuery();
					return "search?faces-redirect=true";
				} else {
					String strDate = calculateConvertedDate(selectedDate);
					this.cachuelo.setToBeCompleteAt(strDate);
					this.entityManager.merge(this.cachuelo);
					simpleLoginBean.executeLocationQuery();
					return "view?faces-redirect=true&id="
							+ this.cachuelo.getIdCachuelo();
				}
			}else {
				simpleLoginBean.setGotKickedOut(true);
				return "/frontend/frontend.xhtml?faces-redirect=true";
			}			
		} catch (Exception e) {
//			FacesContext.getCurrentInstance().addMessage(null,
//					new FacesMessage(e.getMessage()));
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Rellene los campos obligatorios"));
			return null;
		}
	}

	private String calculateConvertedDate(Date anyDate) {
		String strResul = "";
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		strResul = df.format(anyDate);
		return strResul;
	}

	public Date calculateDateFromString(String anyStringDate) {
		Date dateResul = null;
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		try {
			dateResul = df.parse(anyStringDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateResul;
	}

	public String delete() {
		this.conversation.end();

		try {
			Cachuelo deletableEntity = findById(getId());
			Typecachuelo typecachuelo = deletableEntity.getTypecachuelo();
			typecachuelo.getCachuelos().remove(deletableEntity);
			deletableEntity.setTypecachuelo(null);
			this.entityManager.merge(typecachuelo);
			Cachuelostate cachuelostate = deletableEntity.getCachuelostate();
			cachuelostate.getCachuelos().remove(deletableEntity);
			deletableEntity.setCachuelostate(null);
			this.entityManager.merge(cachuelostate);
			Location location = deletableEntity.getLocation();
			location.getCachuelos().remove(deletableEntity);
			deletableEntity.setLocation(null);
			this.entityManager.merge(location);
			User user = deletableEntity.getUser();
			user.getCachuelos().remove(deletableEntity);
			deletableEntity.setUser(null);
			this.entityManager.merge(user);
			Iterator<Commentrating> iterCommentratings = deletableEntity
					.getCommentratings().iterator();
			for (; iterCommentratings.hasNext();) {
				Commentrating nextInCommentratings = iterCommentratings.next();
				nextInCommentratings.setCachuelo(null);
				iterCommentratings.remove();
				this.entityManager.merge(nextInCommentratings);
			}
			Iterator<Offer> iterOffers = deletableEntity.getOffers().iterator();
			for (; iterOffers.hasNext();) {
				Offer nextInOffers = iterOffers.next();
				nextInOffers.setCachuelo(null);
				iterOffers.remove();
				this.entityManager.merge(nextInOffers);
			}
			Iterator<Imagecachuelo> iterImagecachuelos = deletableEntity
					.getImagecachuelos().iterator();
			for (; iterImagecachuelos.hasNext();) {
				Imagecachuelo nextInImagecachuelos = iterImagecachuelos.next();
				iterImagecachuelos.remove();
				// //////////////////////
				Path path = FileSystems.getDefault().getPath(
						"/opt/app-root/src/",
						nextInImagecachuelos.getUrlImage());
				boolean success;
				try {
					success = Files.deleteIfExists(path);
					System.out.println("Delete status: " + success);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// //////////////////////
				this.entityManager.remove(nextInImagecachuelos);
			}
			Iterator<Message> iterMessages = deletableEntity.getMessages()
					.iterator();
			for (; iterMessages.hasNext();) {
				Message nextInMessages = iterMessages.next();
				nextInMessages.setCachuelo(null);
				iterMessages.remove();
				this.entityManager.merge(nextInMessages);
			}
			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Cachuelo entities with pagination
	 */

	private int page;
	private long count;
	private List<Cachuelo> pageItems;

	private Cachuelo example = new Cachuelo();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Cachuelo getExample() {
		return this.example;
	}

	public void setExample(Cachuelo example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Cachuelo> root = countCriteria.from(Cachuelo.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Cachuelo> criteria = builder.createQuery(Cachuelo.class);
		root = criteria.from(Cachuelo.class);
		TypedQuery<Cachuelo> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Cachuelo> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		Typecachuelo typecachuelo = this.example.getTypecachuelo();
		if (typecachuelo != null) {
			predicatesList.add(builder.equal(root.get("typecachuelo"),
					typecachuelo));
		}
		Cachuelostate cachuelostate = this.example.getCachuelostate();
		if (cachuelostate != null) {
			predicatesList.add(builder.equal(root.get("cachuelostate"),
					cachuelostate));
		}
		Location location = this.example.getLocation();
		if (location != null) {
			predicatesList.add(builder.equal(root.get("location"), location));
		}
		// User user = this.example.getUser();
		if (simpleLoginBean.getUserLogged() != null) {
			predicatesList.add(builder.equal(root.get("user"), simpleLoginBean.getUserLogged()));
		}

		String name = this.example.getName();
		if (name != null && !"".equals(name)) {
			predicatesList.add(builder.like(root.<String> get("name"),
					'%' + name + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Cachuelo> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Cachuelo entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Cachuelo> getAll() {
		List<Cachuelo> list;
		List<Cachuelo> listFiltered = new ArrayList<Cachuelo>();
		CriteriaQuery<Cachuelo> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Cachuelo.class);
		list = this.entityManager.createQuery(
				criteria.select(criteria.from(Cachuelo.class))).getResultList();
		for (Cachuelo cach : list) {
			if (simpleLoginBean.getUserLogged() != null) {
				if (cach.getUser().getIdUser() == simpleLoginBean.getUserLogged().getIdUser()) {
					listFiltered.add(cach);
				}
			}
		}
		return listFiltered;
	}

	public List<Cachuelo> getAllComplete() {
		List<Cachuelo> list;
		CriteriaQuery<Cachuelo> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Cachuelo.class);
		list = this.entityManager.createQuery(
				criteria.select(criteria.from(Cachuelo.class))).getResultList();
		return list;
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final FrontCachueloBean ejbProxy = this.sessionContext
				.getBusinessObject(FrontCachueloBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context,
					UIComponent component, String value) {

				return ejbProxy.findById(Integer.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context,
					UIComponent component, Object value) {

				if (value == null) {
					return "";
				}

				return String.valueOf(((Cachuelo) value).getIdCachuelo());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Cachuelo add = new Cachuelo();

	public Cachuelo getAdd() {
		return this.add;
	}

	public Cachuelo getAdded() {
		Cachuelo added = this.add;
		this.add = new Cachuelo();
		return added;
	}

	private static final String[] WEEK_DAY_LABELS = new String[] { "Sun *",
			"Mon +", "Tue +", "Wed +", "Thu +", "Fri +", "Sat *" };
	private Locale locale;

	private boolean popup;
	private boolean readonly;
	private boolean showInput;
	private boolean enableManualInput;
	private String pattern;
	private Date currentDate;
	private Date selectedDate;
	private String jointPoint;
	private String direction;
	private String boundary;

	private boolean useCustomDayLabels;

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public boolean isPopup() {
		return popup;
	}

	public void setPopup(boolean popup) {
		this.popup = popup;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	// public CalendarBean() {
	//
	// locale = Locale.US;
	// popup = true;
	// pattern = "MMM d, yyyy";
	// jointPoint = "bottomleft";
	// direction = "bottomright";
	// readonly = true;
	// enableManualInput=false;
	// showInput=true;
	// boundary = "inactive";
	// }

	public boolean isShowInput() {
		return showInput;
	}

	public void setShowInput(boolean showInput) {
		this.showInput = showInput;
	}

	public boolean isEnableManualInput() {
		return enableManualInput;
	}

	public void setEnableManualInput(boolean enableManualInput) {
		this.enableManualInput = enableManualInput;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public void selectLocale(ValueChangeEvent event) {

		String tLocale = (String) event.getNewValue();
		if (tLocale != null) {
			String lang = tLocale.substring(0, 2);
			String country = tLocale.substring(3);
			locale = new Locale(lang, country, "");
		}
	}

	public boolean isUseCustomDayLabels() {
		return useCustomDayLabels;
	}

	public void setUseCustomDayLabels(boolean useCustomDayLabels) {
		this.useCustomDayLabels = useCustomDayLabels;
	}

	public Object getWeekDayLabelsShort() {
		if (isUseCustomDayLabels()) {
			return WEEK_DAY_LABELS;
		} else {
			return null;
		}
	}

	public String getCurrentDateAsText() {
		Date currentDate = getCurrentDate();
		if (currentDate != null) {
			return DateFormat.getDateInstance(DateFormat.FULL).format(
					currentDate);
		}

		return null;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public Date getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(Date selectedDate) {
		this.selectedDate = selectedDate;
	}

	public String getJointPoint() {
		return jointPoint;
	}

	public void setJointPoint(String jointPoint) {
		this.jointPoint = jointPoint;
	}

	public void selectJointPoint(ValueChangeEvent event) {
		jointPoint = (String) event.getNewValue();
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public void selectDirection(ValueChangeEvent event) {
		direction = (String) event.getNewValue();
	}

	public String getBoundary() {
		return boundary;
	}

	public void setBoundary(String boundary) {
		this.boundary = boundary;
	}

	public int sendCachueloToPhone(User userSender, Cachuelo cachuelo,
			Typecachuelo typ) {

		String strJson = Utility.constructJSON(Constants.TAG_SEND_CACHUELO,
				userSender, cachuelo);
		final List<User> usersList = entityManager
				.createQuery(
						"SELECT DISTINCT u FROM User u WHERE u.isworker = :idParam ORDER BY u.idUser",
						User.class).setParameter("idParam", 1).getResultList();

		int tasks = 0;
		if (usersList.isEmpty()) {
			return 1;
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
							GCMSenderJson senderJson = new GCMSenderJson();
							senderJson.sendMessage(partialDevices,
									strJson, apikey);
							tasks++;
						}
					}
				}
			}
		}
		System.out
				.println("Message Cachuelos out:" + strJson + " tasks: " + tasks);
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

//	private void asyncSend(List<String> partialDevices, final String msg) {
//		final List<String> devices = new ArrayList<String>(partialDevices);
//		threadPool.execute(new Runnable() {
//
//			public void run() {
//				com.google.android.gcm.server.Message message = new com.google.android.gcm.server.Message.Builder()
//						.timeToLive(30).delayWhileIdle(false)
//						.addData("message", msg).build();
//				MulticastResult multicastResult;
//				try {
//					multicastResult = sender.send(message, devices, 5);
//				} catch (IOException e) {
//					logger.log(Level.SEVERE, "Error posting messages", e);
//					return;
//				}
//				List<Result> results = multicastResult.getResults();
//				for (int i = 0; i < devices.size(); i++) {
//					String regId = devices.get(i);
//					Result result = results.get(i);
//					String messageId = result.getMessageId();
//					if (messageId != null) {
//						logger.fine("Succesfully sent message to device: "
//								+ regId + "; messageId = " + messageId);
//						String canonicalRegId = result
//								.getCanonicalRegistrationId();
//						if (canonicalRegId != null) {
//							logger.info("canonicalRegId update reg "
//									+ canonicalRegId);
//						}
//					} else {
//						String error = result.getErrorCodeName();
//						if (error
//								.equals(com.google.android.gcm.server.Constants.ERROR_NOT_REGISTERED)) {
//							logger.info("Unregistered device: " + regId);
//						} else {
//							logger.severe("Error sending message to " + regId
//									+ ": " + error);
//						}
//					}
//				}
//			}
//		});
//	}

	public Cachuelostate getCachueloState(String id) {
		Cachuelostate cachState;

		TypedQuery<Cachuelostate> findByIdQuery = entityManager
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

	// /////////////////// images
	private ArrayList<File> files = new ArrayList<File>();
	private int uploadsAvailable = 3;
	boolean uploadDisabled = false;

	public void listener(FileUploadEvent event) throws Exception {
		String secretKey = UUID.randomUUID().toString();
		UploadedFile uploadedPic = event.getUploadedFile();
		File file = new File();
		file.setLength(uploadedPic.getData().length);
		file.setName(secretKey + uploadedPic.getName());
		file.setData(uploadedPic.getData());
		files.add(file);
		// cambbios
		FileOutputStream os = new FileOutputStream(
				"/opt/app-root/src/" + secretKey
						+ uploadedPic.getName());
		os.write(file.getData());
		os.flush();
		os.close();
		// fin camnbios
		uploadsAvailable--;
		if (uploadsAvailable == 0) {
			uploadDisabled = true;
		}
	}

	public String clearUploadData() {
		System.out.println("Trying to Delete...");
		// delete if exists
		for (File file : files) {
			Path path = FileSystems.getDefault().getPath(
					"/opt/app-root/src/", file.getName());
			boolean success;
			try {
				success = Files.deleteIfExists(path);
				System.out.println("Delete status: " + success);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		files.clear();
		setUploadsAvailable(3);
		uploadDisabled = false;
		return "";
	}

	public String clearUploadDataRedirect() {
		System.out.println("Trying to Delete with redirect...");
		this.conversation.end();
		// delete if exists
		for (File file : files) {
			Path path = FileSystems.getDefault().getPath(
					"/opt/app-root/src/", file.getName());
			boolean success;
			try {
				success = Files.deleteIfExists(path);
				System.out.println("Delete status: " + success);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		files.clear();
		setUploadsAvailable(3);
		uploadDisabled = false;
		this.cachuelo.setName("");
		return "search?faces-redirect=true";
	}

	public boolean getUploadDisabled() {
		return uploadDisabled;
	}

	public void setUploadDisabled(boolean uploadDisabled) {
		this.uploadDisabled = uploadDisabled;
	}

	public int getUploadsAvailable() {
		return uploadsAvailable;
	}

	public void setUploadsAvailable(int uploadsAvailable) {
		this.uploadsAvailable = uploadsAvailable;
	}

	public ArrayList<File> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<File> files) {
		this.files = files;
	}

	public int getSize() {
		if (getFiles().size() > 0) {
			return getFiles().size();
		} else {
			return 0;
		}
	}

	public long getTimeStamp() {
		return System.currentTimeMillis();
	}

	public int insertImages(Cachuelo cach) {
		for (int i = 0; i < files.size(); i++) {
			String urlImage = files.get(i).getName();
			Imagecachuelo img = new Imagecachuelo(cach, urlImage);
			try {
				entityManager.persist(img);
			} catch (Exception e) {
				return -1;// error sql
			}
		}
		return 1;
	}

}