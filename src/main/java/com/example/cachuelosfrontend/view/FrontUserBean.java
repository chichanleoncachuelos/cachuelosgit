package com.example.cachuelosfrontend.view;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
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
import com.example.cachuelosfrontend.model.Commentrating;
import com.example.cachuelosfrontend.model.Digitalizeddocument;
import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.Message;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.model.Userlocationbytime;
import com.example.cachuelosfrontend.model.Workerbytypecachuelo;
import com.example.cachuelosfrontend.model.Zone;
import com.example.cachuelosfrontend.utils.Constants;
import com.example.cachuelosfrontend.utils.PasswordEncrypter;
import com.example.cachuelos.view.ApplicationBean;
import com.example.cachuelos.view.File;
import com.example.cachuelos.view.RegistrationEmail;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import java.util.Iterator;

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
@Stateful
@ConversationScoped
public class FrontUserBean implements Serializable {
	/*
	 * Support creating and retrieving Commentrating entities
	 */

	@Inject
	SimpleLoginBean simpleLoginBean;
	
	@Inject
	ApplicationBean applicationBean;
	
	@Inject
	RegistrationEmail registrationEmail;

	User userLogged;
	String  userLoggedPassword;

	@PostConstruct
	private void initialize() {
		String str = simpleLoginBean.getLoggedInStatus() + " / "
				+ simpleLoginBean.getLoginname();
		userLogged = simpleLoginBean.getUserLogged();
		System.out.println("UserBean: " + str);
	}

	private void filterMyCommentRatings() {
		List<Commentrating> tmpLst = getAllCommentRating();
		for (Commentrating commentrating : tmpLst) {
			if (isAboutMe(commentrating)
					&& commentrating.getCommentratingtype().getIdCommentType() == 1) {
				userRatings.add(commentrating);
			}
		}

	}

	private boolean isAboutMe(Commentrating commentrating) {
		boolean resulBool = false;
		Set<Offer> offers = new HashSet<Offer>(0);
		Cachuelo cach = commentrating.getCachuelo();
		offers = cach.getOffers();
		for (Offer offer : offers) {
			if (offer.getUser().getIdUser() == this.user.getIdUser()
					&& offer.getIsAccepted() == 1) {
				return true;
			}
		}
		return resulBool;
	}

	public User getUserLogged() {
		return userLogged;
	}

	public void setUserLogged(User userLogged) {
		this.userLogged = userLogged;
	}

	List<Commentrating> userRatings = new ArrayList<Commentrating>();

	private static final long serialVersionUID = 1L;

	public List<Commentrating> getUserRatings() {
		return userRatings;
	}

	public void setUserRatings(List<Commentrating> userRatings) {
		this.userRatings = userRatings;
	}

	public List<Commentrating> getAllCommentRating() {
		CriteriaQuery<Commentrating> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Commentrating.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Commentrating.class)))
				.getResultList();
	}

	/*
	 * Support creating and retrieving User entities
	 */

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private User user;

	public User getUser() {
		return this.user;
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
			this.user = this.example;
		} else {
			this.user = findById(getId());
			userLoggedPassword=user.getPassword();
			filterMyCommentRatings();
		}
	}

	public User findById(Integer id) {

		return this.entityManager.find(User.class, id);
	}

	/*
	 * Support updating and deleting User entities
	 */

	public String update() {
		this.conversation.end();
		try {
			if (this.id == null) {
				this.user.setPassword(PasswordEncrypter
						.encryptPassword(this.user.getPassword()));
				String secretkey = UUID.randomUUID().toString();
				this.user.setSecretkey(secretkey);
				if (!existUser(this.user.getEmail())) {
					this.entityManager.persist(this.user);
					simpleLoginBean
							.setMessageRegister(Constants.REGISTER_SUCCESS);
					String host = applicationBean.getIpAddress();
					System.out.println("host: "+host +"   ////fin host");
//					RegistrationEmail registrationEmail = new RegistrationEmail();
//					RegistrationEmail registrationEmail = (RegistrationEmail) new InitialContext().lookup("java:comp/env/registrationEmail");
					registrationEmail.send(secretkey,
							this.user.getEmail(),host);
				} else {
					simpleLoginBean
							.setMessageRegister(Constants.ALREADY_REGISTERED);
				}
				ExternalContext context = FacesContext.getCurrentInstance()
						.getExternalContext();
				try {
					context.redirect(context.getRequestContextPath()
							+ "/faces/frontend/frontend.xhtml");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "";
				}
				return "";
			} else {
				if (this.user.getIdUser() == userLogged.getIdUser()) {	
					String userPasswordChanged=this.user.getPassword();
					if (userLoggedPassword.compareTo(userPasswordChanged) != 0) {
						this.user.setPassword(PasswordEncrypter
								.encryptPassword(this.user.getPassword()));
					}										
					this.entityManager.merge(this.user);
					return "view?faces-redirect=true&id="
							+ this.user.getIdUser();
				} else {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage("ERROR"));
					return null;
				}
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	private boolean existUser(String email) {
		boolean resul = false;

		TypedQuery<User> findByIdQuery = entityManager
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

	public String delete() {
		this.conversation.end();

		try {
			User deletableEntity = findById(getId());
			Zone zone = deletableEntity.getZone();
			zone.getUsers().remove(deletableEntity);
			deletableEntity.setZone(null);
			this.entityManager.merge(zone);
			Iterator<Cachuelo> iterCachuelos = deletableEntity.getCachuelos()
					.iterator();
			for (; iterCachuelos.hasNext();) {
				Cachuelo nextInCachuelos = iterCachuelos.next();
				nextInCachuelos.setUser(null);
				iterCachuelos.remove();
				this.entityManager.merge(nextInCachuelos);
			}
			Iterator<Location> iterLocations = deletableEntity.getLocations()
					.iterator();
			for (; iterLocations.hasNext();) {
				Location nextInLocations = iterLocations.next();
				nextInLocations.setUser(null);
				iterLocations.remove();
				this.entityManager.merge(nextInLocations);
			}
			Iterator<Workerbytypecachuelo> iterWorkerbytypecachuelos = deletableEntity
					.getWorkerbytypecachuelos().iterator();
			for (; iterWorkerbytypecachuelos.hasNext();) {
				Workerbytypecachuelo nextInWorkerbytypecachuelos = iterWorkerbytypecachuelos
						.next();
				nextInWorkerbytypecachuelos.setUser(null);
				iterWorkerbytypecachuelos.remove();
				this.entityManager.merge(nextInWorkerbytypecachuelos);
			}
			Iterator<Commentrating> iterCommentratings = deletableEntity
					.getCommentratings().iterator();
			for (; iterCommentratings.hasNext();) {
				Commentrating nextInCommentratings = iterCommentratings.next();
				nextInCommentratings.setUser(null);
				iterCommentratings.remove();
				this.entityManager.merge(nextInCommentratings);
			}
			Iterator<Userlocationbytime> iterUserlocationbytimes = deletableEntity
					.getUserlocationbytimes().iterator();
			for (; iterUserlocationbytimes.hasNext();) {
				Userlocationbytime nextInUserlocationbytimes = iterUserlocationbytimes
						.next();
				nextInUserlocationbytimes.setUser(null);
				iterUserlocationbytimes.remove();
				this.entityManager.merge(nextInUserlocationbytimes);
			}
			Iterator<Offer> iterOffers = deletableEntity.getOffers().iterator();
			for (; iterOffers.hasNext();) {
				Offer nextInOffers = iterOffers.next();
				nextInOffers.setUser(null);
				iterOffers.remove();
				this.entityManager.merge(nextInOffers);
			}
			Iterator<Message> iterMessagesForIdUserSender = deletableEntity
					.getMessagesForIdUserSender().iterator();
			for (; iterMessagesForIdUserSender.hasNext();) {
				Message nextInMessagesForIdUserSender = iterMessagesForIdUserSender
						.next();
				nextInMessagesForIdUserSender.setUserByIdUserSender(null);
				iterMessagesForIdUserSender.remove();
				this.entityManager.merge(nextInMessagesForIdUserSender);
			}
			Iterator<Digitalizeddocument> iterDigitalizeddocuments = deletableEntity
					.getDigitalizeddocuments().iterator();
			for (; iterDigitalizeddocuments.hasNext();) {
				Digitalizeddocument nextInDigitalizeddocuments = iterDigitalizeddocuments
						.next();
				nextInDigitalizeddocuments.setUser(null);
				iterDigitalizeddocuments.remove();
				this.entityManager.merge(nextInDigitalizeddocuments);
			}
			Iterator<Message> iterMessagesForIdReceiver = deletableEntity
					.getMessagesForIdReceiver().iterator();
			for (; iterMessagesForIdReceiver.hasNext();) {
				Message nextInMessagesForIdReceiver = iterMessagesForIdReceiver
						.next();
				nextInMessagesForIdReceiver.setUserByIdReceiver(null);
				iterMessagesForIdReceiver.remove();
				this.entityManager.merge(nextInMessagesForIdReceiver);
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
	 * Support searching User entities with pagination
	 */

	private int page;
	private long count;
	private List<User> pageItems;

	private User example = new User();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public User getExample() {
		return this.example;
	}

	public void setExample(User example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<User> root = countCriteria.from(User.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<User> criteria = builder.createQuery(User.class);
		root = criteria.from(User.class);
		TypedQuery<User> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<User> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		Zone zone = this.example.getZone();
		if (zone != null) {
			predicatesList.add(builder.equal(root.get("zone"), zone));
		}
		String name = this.example.getName();
		if (name != null && !"".equals(name)) {
			predicatesList.add(builder.like(root.<String> get("name"),
					'%' + name + '%'));
		}
		String lastName = this.example.getLastName();
		if (lastName != null && !"".equals(lastName)) {
			predicatesList.add(builder.like(root.<String> get("lastName"),
					'%' + lastName + '%'));
		}
		String email = this.example.getEmail();
		if (email != null && !"".equals(email)) {
			predicatesList.add(builder.like(root.<String> get("email"),
					'%' + email + '%'));
		}
		String updatedAt = this.example.getUpdatedAt();
		if (updatedAt != null && !"".equals(updatedAt)) {
			predicatesList.add(builder.like(root.<String> get("updatedAt"),
					'%' + updatedAt + '%'));
		}

		predicatesList.add(builder.equal(root.<Integer> get("isworker"), 1));

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<User> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back User entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<User> getAll() {

		CriteriaQuery<User> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(User.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(User.class))).getResultList();
	}

	public List<User> getAllClients() {
		CriteriaQuery<User> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(User.class);
		List<User> listResultPartial = this.entityManager.createQuery(
				criteria.select(criteria.from(User.class))).getResultList();
		List<User> listResult = new ArrayList<User>();
		for (User user : listResultPartial) {
			if (user.getIsworker() == 0) {
				listResult.add(user);
			}
		}
		return listResult;
	}

	public List<User> getAllWorkers() {
		CriteriaQuery<User> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(User.class);
		List<User> listResultPartial = this.entityManager.createQuery(
				criteria.select(criteria.from(User.class))).getResultList();
		List<User> listResult = new ArrayList<User>();
		for (User user : listResultPartial) {
			if (user.getIsworker() == 1) {
				listResult.add(user);
			}
		}
		return listResult;
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final FrontUserBean ejbProxy = this.sessionContext
				.getBusinessObject(FrontUserBean.class);

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

				return String.valueOf(((User) value).getIdUser());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private User add = new User();

	public User getAdd() {
		return this.add;
	}

	public User getAdded() {
		User added = this.add;
		this.add = new User();
		return added;
	}

	private ArrayList<File> files = new ArrayList<File>();
	private int uploadsAvailable = 1;
	boolean uploadDisabled = false;

//	public void paint(OutputStream stream, Object object) throws IOException {
//		stream.write(getFiles().get((Integer) object).getData());
//		stream.close();
//	}

	public void listener(FileUploadEvent event) throws Exception {
		String secretKey = UUID.randomUUID().toString();
		UploadedFile uploadedPicture = event.getUploadedFile();
		File file = new File();
		file.setLength(uploadedPicture.getData().length);
		file.setName(secretKey + uploadedPicture.getName());
		file.setData(uploadedPicture.getData());
		files.add(file);
		// cambbios
		FileOutputStream os = new FileOutputStream("/opt/app-root/src" + secretKey+ uploadedPicture.getName());
		os.write(file.getData());
		os.flush();
		os.close();
		user.setPictureFull(secretKey + uploadedPicture.getName());
		resize(secretKey + uploadedPicture.getName(), 0.15);//crea la foto reducida thumbnail
		// fin camnbios
		uploadsAvailable--;
		if (uploadsAvailable == 0) {
			uploadDisabled = true;
		}
	}

	public String clearUploadData() {
		System.out.println("Trying to Delete without redirect...");
		// delete if exists
		Path path = FileSystems.getDefault().getPath("/opt/app-root/src", user.getPictureFull());
		boolean success;
		try {
			success = Files.deleteIfExists(path);
			System.out.println("Delete status: " + success);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		user.setPictureFull("");
		// ////////
		Path path2 = FileSystems.getDefault().getPath("/opt/app-root/src", user.getPictureThumb());
		try {
			success = Files.deleteIfExists(path2);
			System.out.println("Delete status: " + success);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		user.setPictureThumb("");
		files.clear();
		setUploadsAvailable(1);
		uploadDisabled = false;
		return "";
	}

	public void clearUploadDataRedirect() {
		System.out.println("Trying to Delete with redirect...");
		this.conversation.end();
		if (files.size()>0){
			// delete if exists
			System.out.println("Inside if.....");
			Path path = FileSystems.getDefault().getPath("/opt/app-root/src", user.getPictureFull());
			boolean success;
			try {
				success = Files.deleteIfExists(path);
				System.out.println("Delete status: " + success);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			user.setPictureFull("");
			// ////////
			Path path2 = FileSystems.getDefault().getPath("/opt/app-root/src", user.getPictureThumb());
			try {
				success = Files.deleteIfExists(path2);
				System.out.println("Delete status: " + success);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		user.setPictureThumb("");
		user.setName("");
		files.clear();
		setUploadsAvailable(1);
		uploadDisabled = false;
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

	public void resize(String inputImageUrl, int scaledWidth, int scaledHeight)
			throws IOException {
		String secretKey = UUID.randomUUID().toString();

		// reads input image
		java.io.File inputFile = new java.io.File("/opt/app-root/src" + inputImageUrl);
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
				new java.io.File("/opt/app-root/src"+ secretKey + inputImageUrl));
		user.setPictureThumb(secretKey + inputImageUrl);
	}

	public void resize(String inputImageUrl, double percent) throws IOException {
		java.io.File inputFile = new java.io.File("/opt/app-root/src" + inputImageUrl);
		BufferedImage inputImage = ImageIO.read(inputFile);
		int scaledWidth = (int) (inputImage.getWidth() * percent);
		int scaledHeight = (int) (inputImage.getHeight() * percent);
		resize(inputImageUrl, scaledWidth, scaledHeight);
	}

}