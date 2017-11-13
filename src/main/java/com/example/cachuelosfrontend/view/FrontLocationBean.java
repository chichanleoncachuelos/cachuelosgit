package com.example.cachuelosfrontend.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.example.cachuelos.view.SimpleAdminBean;
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.model.Zone;
import com.googlecode.gmaps4jsf.component.common.Position;

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
public class FrontLocationBean implements Serializable {

	@Inject
	SimpleLoginBean simpleLoginBean;

	@Inject
	SimpleAdminBean simpleAdminBean;

	// User userLogged;

	@PostConstruct
	private void initialize() {
		// simpleLoginBean.checkIfLoginStatusIsFalse();
		String str = simpleLoginBean.getLoggedInStatus() + " / "
				+ simpleLoginBean.getLoginname();
		// userLogged = simpleLoginBean.getUserLogged();
		System.out.println("locationBean: " + str);

		// Maps
		// draggableModel = new DefaultMapModel();
		// LatLng coord1 = new LatLng(-12.071886098955085, -77.08684869110584);
		// draggableModel.addOverlay(new Marker(coord1, "Nuevo"));
		// for (Marker premarker : draggableModel.getMarkers()) {
		// premarker.setDraggable(true);
		// }
	}

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
		Location loc;
		loc = this.entityManager.find(Location.class, id);
		if (loc.getIsFavorite() == 0) {
			loc.setFavorite(false);
		} else {
			loc.setFavorite(true);
		}
		return loc;
	}

	/*
	 * Support updating and deleting Location entities
	 */

	public String update() {
		this.conversation.end();
		if (this.location.getFavorite()) {
			this.location.setIsFavorite(1);
		} else {
			this.location.setIsFavorite(0);
		}
		try {
			if (simpleLoginBean.getUserLogged() != null) {
				if (this.id == null) {
					this.location.setUser(simpleLoginBean.getUserLogged());
					this.location.setLat(simpleAdminBean.getLat());
					this.location.setLng(simpleAdminBean.getLng());
					if (belongsToAvailableZones(this.location.getLat(),
							this.location.getLng())) {
						this.entityManager.persist(this.location);
					} else {
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_WARN,
										"Mensaje Importante:", null));
						FacesMessage message = new FacesMessage(
								"Esta zona no tine cobertura",
								"Revisar zonas de cobertura");
						FacesContext.getCurrentInstance().addMessage(null, message);
						return null;
					}
					return "search?faces-redirect=true";
				} else {
					if (belongsToAvailableZones(this.location.getLat(),
							this.location.getLng())) {
						this.entityManager.merge(this.location);
					} else {
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_WARN,
										"Mensaje Importante:", null));
						FacesMessage message = new FacesMessage(
								"Esta zona no tine cobertura",
								"Revisar zonas de cobertura");
						FacesContext.getCurrentInstance().addMessage(null, message);
						return null;
					}
					return "view?faces-redirect=true&id="
							+ this.location.getIdLocation();
				}
			}else {
				simpleLoginBean.setGotKickedOut(true);
				return "/frontend/frontend.xhtml?faces-redirect=true";
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

		// User user = this.example.getUser();
		User user = simpleLoginBean.getUserLogged();
		if (user != null) {
			predicatesList.add(builder.equal(root.get("user"), user));
		}
		boolean favorite = this.example.getFavorite();
		Integer isFavorite; // this.example.getIsFavorite();
		if (favorite) {
			isFavorite = 1;
		} else {
			isFavorite = 0;
		}
		if (isFavorite != 0) {
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
		List<Location> list;
		List<Location> listFiltered = new ArrayList<Location>();
		CriteriaQuery<Location> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Location.class);
		list = this.entityManager.createQuery(
				criteria.select(criteria.from(Location.class))).getResultList();
		for (Location location : list) {
			if (simpleLoginBean.getUserLogged() != null) {
				if (location.getUser().getIdUser() == simpleLoginBean
						.getUserLogged().getIdUser()) {
					listFiltered.add(location);
				}
			}
		}

		return listFiltered;
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final FrontLocationBean ejbProxy = this.sessionContext
				.getBusinessObject(FrontLocationBean.class);

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

	// private MapModel draggableModel;
	//
	// private Marker marker;
	//
	// Double lat = 0.0;
	// Double lng = 0.0;
	// Double latTmp = 0.0;
	// Double lngTmp = 0.0;
	//
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
	//
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
	//
	// public MapModel getDraggableModel() {
	// return draggableModel;
	// }
	//
	// public void onMarkerDrag(MarkerDragEvent event) {
	// marker = event.getMarker();
	// System.out.println("inside Dummy: lat:" + marker.getLatlng().getLat()
	// + " lng:" + marker.getLatlng().getLng());
	// lat = marker.getLatlng().getLat();
	// lng = marker.getLatlng().getLng();
	// }
	//
	// public void addMarker() {
	// System.out.println("Adding Marker: lat:" + latTmp+ " lng:" + lngTmp);
	// for (Marker premarker : draggableModel.getMarkers()) {
	// premarker.setLatlng(new LatLng(latTmp, lngTmp));
	// premarker.setDraggable(true);
	// }
	// lat = latTmp;
	// lng = lngTmp;
	// }

	private boolean belongsToAvailableZones(Double lat, Double lng) {
		boolean resul = false;

		final List<Zone> list = this.entityManager.createQuery(
				"SELECT DISTINCT z FROM Zone z ORDER BY z.idZone", Zone.class)
				.getResultList();
		for (int i = 0; i < list.size(); i++) {
			Zone zon = list.get(i);
			Double dist = calculateDistance(zon.getLat(), zon.getLng(), lat,
					lng);
			if (dist <= zon.getRadius()) {
				return true;
			}
		}
		return resul;
	}

	private Double calculateDistance(float lat1, float lng1, double lat2,
			double lng2) {

		int EARTH_RADIUS_KM = 6371;
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double deltaLonRad = Math.toRadians(lng2 - lng1);

		return Math
				.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad)
						* Math.cos(lat2Rad) * Math.cos(deltaLonRad))
				* EARTH_RADIUS_KM;
	}

	@SuppressWarnings("unused")
	public void processValueChangeForMarker(ValueChangeEvent valueChangeEvent) {
		Position value = (Position) valueChangeEvent.getNewValue();
		com.googlecode.gmaps4jsf.component.marker.Marker marker = (com.googlecode.gmaps4jsf.component.marker.Marker) valueChangeEvent
				.getSource();
		String idUserCreator = marker.getJsVariable();
		if (value != null) {// && idUserCreator!=null
			String lat = value.getLatitude();
			String lng = value.getLongitude();
			location.setLat(Double.parseDouble(lat));
			location.setLng(Double.parseDouble(lng));
		}
	}
}