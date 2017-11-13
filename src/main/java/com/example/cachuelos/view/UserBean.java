package com.example.cachuelos.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Commentrating;
import com.example.cachuelosfrontend.model.Digitalizeddocument;
import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.Message;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.Userlocationbytime;
import com.example.cachuelosfrontend.model.Workerbytypecachuelo;
import com.example.cachuelosfrontend.model.Zone;
import com.example.cachuelosfrontend.utils.PasswordEncrypter;

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
public class UserBean implements Serializable {

	private static final long serialVersionUID = 1L;

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
		}
	}

	public User findById(Integer id) {
		User user = this.entityManager.find(User.class, id);
		if (user.getIsActive() == 0) {
			user.setActive(false);
		} else {
			user.setActive(true);
		}
		if (user.getIsworker() == 0) {
			user.setWorker(false);
		} else {
			user.setWorker(true);
		}
		return user;
	}

	/*
	 * Support updating and deleting User entities
	 */

	public String update() {
		this.conversation.end();

		if (this.user.getActive()) {
			this.user.setIsActive(1);
		} else {
			this.user.setIsActive(0);
		}

		if (this.user.getWorker()) {
			this.user.setIsworker(1);
		} else {
			this.user.setIsworker(0);
		}
		this.user.setPassword(PasswordEncrypter.encryptPassword(this.user.getPassword()));

		try {
			if (this.id == null) {
				this.entityManager.persist(this.user);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.user);
				return "view?faces-redirect=true&id=" + this.user.getIdUser();
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

		final UserBean ejbProxy = this.sessionContext
				.getBusinessObject(UserBean.class);

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
}