package com.example.cachuelosfrontend.view;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
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
import com.example.cachuelosfrontend.model.Imagecachuelo;
import com.example.cachuelosfrontend.model.User;

/**
 * Backing bean for Imagecachuelo entities.
 * <p>
 * This class provides CRUD functionality for all Imagecachuelo entities. It
 * focuses purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt>
 * for state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class FrontImagecachueloBean implements Serializable {
	
	@Inject
	SimpleLoginBean simpleLoginBean;

	@PostConstruct
	private void initialize() {
		// simpleLoginBean.checkIfLoginStatusIsFalse();
		userLogged = simpleLoginBean.getUserLogged();
	}
	
	public User getUserLogged() {
		return userLogged;
	}

	public void setUserLogged(User userLogged) {
		this.userLogged = userLogged;
	}
	
	User userLogged;

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Imagecachuelo entities
	 */

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Imagecachuelo imagecachuelo;

	public Imagecachuelo getImagecachuelo() {
		return this.imagecachuelo;
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
			this.imagecachuelo = this.example;
			this.imagecachuelo.setUrlImage("");
		} else {
			this.imagecachuelo = findById(getId());
		}
	}

	public Imagecachuelo findById(Integer id) {

		return this.entityManager.find(Imagecachuelo.class, id);
	}

	/*
	 * Support updating and deleting Imagecachuelo entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.id == null) {
				this.entityManager.persist(this.imagecachuelo);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.imagecachuelo);
				return "view?faces-redirect=true&id="
						+ this.imagecachuelo.getIdImageCachuelo();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {
		this.conversation.end();

		try {
			Imagecachuelo deletableEntity = findById(getId());
			Cachuelo cachuelo = deletableEntity.getCachuelo();
			cachuelo.getImagecachuelos().remove(deletableEntity);
			deletableEntity.setCachuelo(null);
			this.entityManager.merge(cachuelo);
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
	 * Support searching Imagecachuelo entities with pagination
	 */

	private int page;
	private long count;
	private List<Imagecachuelo> pageItems;

	private Imagecachuelo example = new Imagecachuelo();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 5;
	}

	public Imagecachuelo getExample() {
		return this.example;
	}

	public void setExample(Imagecachuelo example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Imagecachuelo> root = countCriteria.from(Imagecachuelo.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));

		// Populate this.pageItems

		CriteriaQuery<Imagecachuelo> criteria = builder
				.createQuery(Imagecachuelo.class);
		root = criteria.from(Imagecachuelo.class);
		TypedQuery<Imagecachuelo> query = this.entityManager
				.createQuery(criteria.select(root).where(
						getSearchPredicates(root)));
		
		
		
		List<Imagecachuelo> filteredArray = filterArray(query.getResultList());
		this.count = filteredArray.size();
		this.pageItems = getPageResults(filteredArray);
	}

	private List<Imagecachuelo> filterArray(List<Imagecachuelo> imageArray) {
			List<Imagecachuelo> filterResul = new ArrayList<Imagecachuelo>();
			
			
			for (Imagecachuelo img : imageArray) {
				if (img.getCachuelo().getUser().getIdUser()==userLogged.getIdUser()){
					filterResul.add(img);
				}
			}
			return filterResul;
		}

	private Predicate[] getSearchPredicates(Root<Imagecachuelo> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		Cachuelo cachuelo = this.example.getCachuelo();
		if (cachuelo != null) {
			predicatesList.add(builder.equal(root.get("cachuelo"), cachuelo));
		}
		
		String urlImage = this.example.getUrlImage();
		if (urlImage != null && !"".equals(urlImage)) {
			predicatesList.add(builder.like(root.<String> get("urlImage"),
					'%' + urlImage + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}
	
	
	private List<Imagecachuelo> getPageResults(List<Imagecachuelo> imageList) {
		List<Imagecachuelo> pageResul = new ArrayList<Imagecachuelo>();
		int initialPosition = 0;
		int finalPosition = 0;
		initialPosition = this.page * getPageSize();
		finalPosition = initialPosition + getPageSize();
		for (int i = initialPosition; i < finalPosition; i++) {
			if (i < imageList.size()) {
				pageResul.add(imageList.get(i));
			}
		}
		return pageResul;
	}

	public List<Imagecachuelo> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Imagecachuelo entities (e.g. from inside
	 * an HtmlSelectOneMenu)
	 */

	public List<Imagecachuelo> getAll() {

		CriteriaQuery<Imagecachuelo> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Imagecachuelo.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Imagecachuelo.class)))
				.getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final FrontImagecachueloBean ejbProxy = this.sessionContext
				.getBusinessObject(FrontImagecachueloBean.class);

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

				return String.valueOf(((Imagecachuelo) value)
						.getIdImageCachuelo());
			}
		};
	}

	private ArrayList<File> files = new ArrayList<File>();

	public void listener(FileUploadEvent event) throws Exception {
		String secretKey = UUID.randomUUID().toString();
		UploadedFile item = event.getUploadedFile();
		File file = new File();
		file.setLength(item.getData().length);
		file.setName(secretKey + item.getName());
		file.setData(item.getData());
		files.add(file);
		// cambbios
		FileOutputStream os = new FileOutputStream("/opt/app-root/src/"
				+ secretKey + item.getName());
		os.write(file.getData());
		os.flush();
		os.close();
		imagecachuelo.setUrlImage(secretKey + item.getName());
		// fin camnbios
	}

	public String clearUploadData() {
		files.clear();
		// delete if exists
		Path path = FileSystems.getDefault().getPath("/opt/app-root/src/",
				imagecachuelo.getUrlImage());
		boolean success;
		try {
			success = Files.deleteIfExists(path);
			System.out.println("Delete status: " + success);
		} catch (IOException e) {
			e.printStackTrace();
		}
		imagecachuelo.setUrlImage("");
		return "";
	}
	
	public String clearUploadDataRedirect() {
		this.conversation.end();
		if (imagecachuelo.getUrlImage().compareTo("")==0){
			
		}
		else{
			files.clear();
			// delete if exists
			Path path = FileSystems.getDefault().getPath("/opt/app-root/src/",
					imagecachuelo.getUrlImage());
			boolean success;
			try {
				success = Files.deleteIfExists(path);
				System.out.println("Delete status: " + success);
			} catch (IOException e) {
				e.printStackTrace();
			}
			imagecachuelo.setUrlImage("");
		}		
		return "search?faces-redirect=true";
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

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Imagecachuelo add = new Imagecachuelo();

	public Imagecachuelo getAdd() {
		return this.add;
	}

	public Imagecachuelo getAdded() {
		Imagecachuelo added = this.add;
		this.add = new Imagecachuelo();
		return added;
	}
}