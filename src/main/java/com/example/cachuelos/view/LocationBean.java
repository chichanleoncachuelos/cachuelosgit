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
import javax.faces.event.ValueChangeEvent;
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

import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.User;
import com.googlecode.gmaps4jsf.component.common.Position;
import com.googlecode.gmaps4jsf.component.marker.Marker;

import java.util.Iterator;

/**
 * Backing bean for Location entities.
 * <p>
 * This class provides CRUD functionality for all Location entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class LocationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Location entities
	 */

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Location location;

	public Location getLocation() {
		return this.location;
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
			this.location = this.example;
		} else {
			this.location = findById(getId());
		}
	}

	public Location findById(Integer id) {

		return this.entityManager.find(Location.class, id);
	}

	/*
	 * Support updating and deleting Location entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.id == null) {
				this.entityManager.persist(this.location);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.location);
				return "view?faces-redirect=true&id="
						+ this.location.getIdLocation();
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
			Location deletableEntity = findById(getId());
			User user = deletableEntity.getUser();
			user.getLocations().remove(deletableEntity);
			deletableEntity.setUser(null);
			this.entityManager.merge(user);
			Iterator<Cachuelo> iterCachuelos = deletableEntity.getCachuelos()
					.iterator();
			for (; iterCachuelos.hasNext();) {
				Cachuelo nextInCachuelos = iterCachuelos.next();
				nextInCachuelos.setLocation(null);
				iterCachuelos.remove();
				this.entityManager.merge(nextInCachuelos);
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
	 * Support searching Location entities with pagination
	 */

	private int page;
	private long count;
	private List<Location> pageItems;

	private Location example = new Location();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Location getExample() {
		return this.example;
	}

	public void setExample(Location example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Location> root = countCriteria.from(Location.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Location> criteria = builder.createQuery(Location.class);
		root = criteria.from(Location.class);
		TypedQuery<Location> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Location> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		User user = this.example.getUser();
		if (user != null) {
			predicatesList.add(builder.equal(root.get("user"), user));
		}
		Integer isFavorite = this.example.getIsFavorite();
		if (isFavorite != null && isFavorite.intValue() != 0) {
			predicatesList
					.add(builder.equal(root.get("isFavorite"), isFavorite));
		}
		String name = this.example.getName();
		if (name != null && !"".equals(name)) {
			predicatesList.add(builder.like(root.<String> get("name"),
					'%' + name + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Location> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Location entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Location> getAll() {

		CriteriaQuery<Location> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Location.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Location.class))).getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final LocationBean ejbProxy = this.sessionContext
				.getBusinessObject(LocationBean.class);

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

				return String.valueOf(((Location) value).getIdLocation());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Location add = new Location();

	public Location getAdd() {
		return this.add;
	}

	public Location getAdded() {
		Location added = this.add;
		this.add = new Location();
		return added;
	}

	@SuppressWarnings("unused")
	public void processValueChangeForMarker(ValueChangeEvent valueChangeEvent) {
		Position value = (Position) valueChangeEvent.getNewValue();
		Marker marker = (Marker) valueChangeEvent.getSource();
		String idUserCreator = marker.getJsVariable();
		if (value != null) {// && idUserCreator!=null
			String lat = value.getLatitude();
			String lng = value.getLongitude();
			location.setLat(Double.parseDouble(lat));
			location.setLng(Double.parseDouble(lng));
		}
	}
}