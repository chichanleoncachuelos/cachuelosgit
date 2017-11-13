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

import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.Zone;
import com.example.cachuelosfrontend.model.User;
import com.googlecode.gmaps4jsf.component.common.Position;

import org.primefaces.event.SlideEndEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Circle;

import java.util.Iterator;

@Named
@Stateful
@ConversationScoped
public class AvailableZonesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Zone entities
	 */

//	@Inject
//	SimpleAdminBean simpleAdminBean;

	private MapModel circleModel;

	@PostConstruct
	public void init() {
		System.out.println("initalizing dashboard:");
		circleModel = new DefaultMapModel();
	}

	public MapModel getCircleModel() {
		return circleModel;
	}

	public void onCircleSelect(OverlaySelectEvent event) {
		Circle circle = (Circle) event.getOverlay();
		String str = (String) circle.getData();
		FacesContext.getCurrentInstance().addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Mensaje Importante:", null));
		FacesContext.getCurrentInstance().addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Mensaje: " + str,
						null));
	}

	public void handleKeyEvent() {
		System.out.println("green keypressed:"+this.greenLimit);
		System.out.println("keypressed green:"+ this.greenLimit+" orange: "+this.orangeLimit);
		executeDashboardQuery();
	}

	public void handleKeyEventOrange() {
		System.out.println("orange keypressed:"+this.orangeLimit);
		System.out.println("keypressed green:"+ this.greenLimit+" orange: "+this.orangeLimit);
		executeDashboardQuery();
	}
	
	public void onSlideEnd(SlideEndEvent event) {		
		int tmpValue=event.getValue();
		System.out.println("Slide end double:"+ tmpValue);		
		System.out.println("Slide end double green:"+ this.greenLimit+" orange: "+this.orangeLimit);
		executeDashboardQuery();
    } 

	private void executeDashboardQuery() {
		circleModel.getCircles().clear();
		CriteriaQuery<Zone> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Zone.class);
		List<Zone> listZone = this.entityManager.createQuery(
				criteria.select(criteria.from(Zone.class))).getResultList();

		CriteriaQuery<Cachuelo> criteria2 = this.entityManager
				.getCriteriaBuilder().createQuery(Cachuelo.class);
		List<Cachuelo> listCachuelo = this.entityManager.createQuery(
				criteria2.select(criteria2.from(Cachuelo.class)))
				.getResultList();
		float total = 0;
		for (int i = 0; i < listZone.size(); i++) {
			int tmp = countCach(listZone.get(i), listCachuelo);
			listZone.get(i).setCachCount(tmp);
			total = total + tmp;
		}
		for (Zone zone2 : listZone) {
			LatLng coord = new LatLng(zone2.getLat(), zone2.getLng());
			Circle circle = new Circle(coord, zone2.getRadius() * 1000);
			String strColor = calculateColor(zone2);
			circle.setStrokeColor(strColor);
			circle.setFillColor(strColor);
			circle.setFillOpacity(0.5);
			circle.setStrokeOpacity(0.5);
			circle.setStrokeWeight(5);
			Object data;
			String strTmp = "Zona: " + zone2.getName();
			data = (Object) strTmp;
			circle.setData(data);
			circleModel.addOverlay(circle);
		}
	}

	private String calculateColor(Zone zone2) {
		String greenColor = "#00ff00";
		String orangeColor = "#ffbf00";
		String redColor = "#d93c3c";
		if (zone2.getCachCount() < this.greenLimit) {
			return greenColor;
		} else {
			if (zone2.getCachCount() <this.orangeLimit) {
				return orangeColor;
			} else {
				return redColor;
			}
		}
	}

	private int countCach(Zone zone2, List<Cachuelo> listCachuelo) {
		int counter = 0;
		for (Cachuelo cachuelo : listCachuelo) {
			if (cachuelo.getLocation() != null) {
				if (cachuelo.getLocation().getIdLocation() != null) {
					if (belongsToZone(cachuelo.getLocation(), zone2)) {
						counter++;
					}
				}
			}
		}
		return counter;
	}

	private boolean belongsToZone(Location location, Zone zone2) {
		boolean resul = false;
		Double dist = calculateDistance(zone2.getLat(), zone2.getLng(),
				location.getLat(), location.getLng());
		if (dist <= zone2.getRadius()) {
			resul = true;
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

	int greenLimit=0;
	int orangeLimit=300;

	public int getGreenLimit() {
		return greenLimit;
	}

	public void setGreenLimit(int greenLimit) {
		this.greenLimit = greenLimit;
	}

	public int getOrangeLimit() {
		return orangeLimit;
	}

	public void setOrangeLimit(int orangeLimit) {
		this.orangeLimit = orangeLimit;
	}

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
			executeDashboardQuery();
		} else {
			this.zone = findById(getId());
			executeDashboardQuery();
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
//				lati = "" + simpleAdminBean.getLat();
//				lngi = "" + simpleAdminBean.getLng();
				lati = "" + 0;
				lngi = "" + 0;
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

		final AvailableZonesBean ejbProxy = this.sessionContext
				.getBusinessObject(AvailableZonesBean.class);

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