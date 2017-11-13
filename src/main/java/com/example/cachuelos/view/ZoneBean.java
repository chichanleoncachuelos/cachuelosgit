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

import com.example.cachuelosfrontend.model.Zone;
import com.example.cachuelosfrontend.model.User;
import com.googlecode.gmaps4jsf.component.common.Position;

import java.util.Iterator;

/**
 * Backing bean for Zone entities.
 * <p>
 * This class provides CRUD functionality for all Zone entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class ZoneBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Zone entities
	 */

	@Inject
	SimpleAdminBean simpleAdminBean;

	// @PostConstruct
	// private void initialize() {
	// simpleLoginBean.checkIfLoginStatusIsFalse();
	// System.out.println("initalizing zone: ");

	// Maps
	// simpleAdminBean.setDraggableModel(new DefaultMapModel());
	// LatLng coord1 = new LatLng(12.071886098955085, 77.08684869110584);
	// simpleAdminBean.getDraggableModel().addOverlay(new Marker(coord1,
	// "Nuevo"));
	// for (Marker premarker : simpleAdminBean.getDraggableModel().getMarkers())
	// {
	// premarker.setDraggable(true);
	// }
	// }

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Zone zone;

	public Zone getZone() {
		return this.zone;
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
			this.zone = this.example;
		} else {
			this.zone = findById(getId());
		}
	}

	public Zone findById(Integer id) {

		return this.entityManager.find(Zone.class, id);
	}

	/*
	 * Support updating and deleting Zone entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.id == null) {
				String lati;
				String lngi;
				// if
				// (simpleAdminBean.getLat()==0.0&&simpleAdminBean.getLng()==0.0){
				// lati = ""+simpleAdminBean.getLatTmp();
				// lngi = ""+simpleAdminBean.getLngTmp();
				// }else{
				//
				// }
				lati = "" + simpleAdminBean.getLat();
				lngi = "" + simpleAdminBean.getLng();
				this.zone.setLat((float) Double.parseDouble(lati));
				this.zone.setLng((float) Double.parseDouble(lngi));
				this.entityManager.persist(this.zone);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.zone);
				return "view?faces-redirect=true&id=" + this.zone.getIdZone();
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
			Zone deletableEntity = findById(getId());
			Iterator<User> iterUsers = deletableEntity.getUsers().iterator();
			for (; iterUsers.hasNext();) {
				User nextInUsers = iterUsers.next();
				nextInUsers.setZone(null);
				iterUsers.remove();
				this.entityManager.merge(nextInUsers);
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
	 * Support searching Zone entities with pagination
	 */

	private int page;
	private long count;
	private List<Zone> pageItems;

	private Zone example = new Zone();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Zone getExample() {
		return this.example;
	}

	public void setExample(Zone example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Zone> root = countCriteria.from(Zone.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Zone> criteria = builder.createQuery(Zone.class);
		root = criteria.from(Zone.class);
		TypedQuery<Zone> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Zone> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String name = this.example.getName();
		if (name != null && !"".equals(name)) {
			predicatesList.add(builder.like(root.<String> get("name"),
					'%' + name + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Zone> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Zone entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Zone> getAll() {

		CriteriaQuery<Zone> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Zone.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Zone.class))).getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final ZoneBean ejbProxy = this.sessionContext
				.getBusinessObject(ZoneBean.class);

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

				return String.valueOf(((Zone) value).getIdZone());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Zone add = new Zone();

	public Zone getAdd() {
		return this.add;
	}

	public Zone getAdded() {
		Zone added = this.add;
		this.add = new Zone();
		return added;
	}

	// private MapModel draggableModel;

	// private Marker marker;

	// Double lat = 0.0;
	// Double lng = 0.0;

	// Double latTmp = 0.0;
	// Double lngTmp = 0.0;

	// public Double getLngTmp() {
	// return lngTmp;
	// }
	//
	// public void setLngTmp(Double lngTmp) {
	// this.lngTmp = lngTmp;
	// }
	//
	// public Double getLatTmp() {
	// return latTmp;
	// }
	//
	// public void setLatTmp(Double latTmp) {
	// this.latTmp = latTmp;
	// }

	// public double getLat() {
	// return lat;
	// }
	//
	// public void setLat(double lat) {
	// this.lat = lat;
	// }
	//
	// public double getLng() {
	// return lng;
	// }
	//
	// public void setLng(double lng) {
	// this.lng = lng;
	// }

	// public MapModel getDraggableModel() {
	// return draggableModel;
	// }

	// public void onMarkerDrag(MarkerDragEvent event) {
	// marker = event.getMarker();
	// System.out.println("inside Dummy: lat:" + marker.getLatlng().getLat()
	// + " lng:" + marker.getLatlng().getLng());
	// lat = marker.getLatlng().getLat();
	// lng = marker.getLatlng().getLng();
	// }

	// public void addMarker(ActionEvent actionEvent) {
	// System.out.println("Adding Marker: lat:" + simpleAdminBean.getLatTmp()+
	// " lng:" + simpleAdminBean.getLngTmp());
	// for (Marker premarker : simpleAdminBean.getDraggableModel().getMarkers())
	// {
	// premarker.setLatlng(new LatLng(simpleAdminBean.getLatTmp(),
	// simpleAdminBean.getLngTmp()));
	// premarker.setDraggable(true);
	// }
	// lat = simpleAdminBean.getLatTmp();
	// lng = simpleAdminBean.getLngTmp();
	// }

	@SuppressWarnings("unused")
	public void processValueChangeForMarker(ValueChangeEvent valueChangeEvent) {
		Position value = (Position) valueChangeEvent.getNewValue();
		com.googlecode.gmaps4jsf.component.marker.Marker marker = (com.googlecode.gmaps4jsf.component.marker.Marker) valueChangeEvent
				.getSource();
		// String idUserCreator = marker.getJsVariable();
		if (value != null) {// && idUserCreator!=null
			String lat = value.getLatitude();
			String lng = value.getLongitude();
			zone.setLat((float) Double.parseDouble(lat));
			zone.setLng((float) Double.parseDouble(lng));
		}
	}
}