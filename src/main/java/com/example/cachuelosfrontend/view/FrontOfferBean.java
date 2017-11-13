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
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Cachuelostate;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.utils.Constants;
import com.example.cachuelos.gcmserver.GCMSenderJson;
import com.example.cachuelos.utils.Utility;

/**
 * Backing bean for Offer entities.
 * <p>
 * This class provides CRUD functionality for all Offer entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class FrontOfferBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String apikey = "";

	/*
	 * Support creating and retrieving Offer entities
	 */

	@Inject
	SimpleLoginBean simpleLoginBean;

	User userLogged;

	@PostConstruct
	private void initialize() {
		String str = simpleLoginBean.getLoggedInStatus() + " / "
				+ simpleLoginBean.getLoginname();
		userLogged = simpleLoginBean.getUserLogged();
		System.out.println("OfferBean: " + str);
		apikey = simpleLoginBean.getApiKey();
		System.out.println("sendMsg safely using KEY");
	}

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Offer offer;

	public Offer getOffer() {
		return this.offer;
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
			this.offer = this.example;
		} else {
			this.offer = findById(getId());
		}
	}

	public Offer findById(Integer id) {
		Offer off;
		off = this.entityManager.find(Offer.class, id);
		if (off.getIsAccepted() == 0) {
			off.setAccepted(false);
			off.setAcceptedStr(Constants.NO);
		} else {
			off.setAccepted(true);
			off.setAcceptedStr(Constants.YES);
		}
		return off;
	}

	/*
	 * Support updating and deleting Offer entities
	 */

	public String update() {
		this.conversation.end();

		if (this.offer.getAccepted()) {
			this.offer.setIsAccepted(1);
		} else {
			this.offer.setIsAccepted(0);
		}
		try {
			if (this.id == null) {
				this.entityManager.persist(this.offer);
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.offer);
				return "view?faces-redirect=true&id=" + this.offer.getIdOffer();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String updateAccepted() {
		this.conversation.end();
		try {
			String strMsg = tryAcceptOffer(this.offer);
			simpleLoginBean.setMessageAcceptOffer(strMsg);
			simpleLoginBean.setIntentCountAcceptOffer(0);
			return "view?faces-redirect=true&id=" + this.offer.getIdOffer();

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {
		this.conversation.end();

		try {
			Offer deletableEntity = findById(getId());
			User user = deletableEntity.getUser();
			user.getOffers().remove(deletableEntity);
			deletableEntity.setUser(null);
			this.entityManager.merge(user);
			Cachuelo cachuelo = deletableEntity.getCachuelo();
			cachuelo.getOffers().remove(deletableEntity);
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
	 * Support searching Offer entities with pagination
	 */

	private int page;
	private long count;
	private List<Offer> pageItems;

	private Offer example = new Offer();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 5;
	}

	public Offer getExample() {
		return this.example;
	}

	public void setExample(Offer example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Offer> root = countCriteria.from(Offer.class);

		// Populate this.pageItems

		CriteriaQuery<Offer> criteria = builder.createQuery(Offer.class);
		root = criteria.from(Offer.class);
		TypedQuery<Offer> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		List<Offer> resultsPartial = query.getResultList();// todas las ofertas
		List<Offer> filteredArray = filterArray(resultsPartial);
		this.count = filteredArray.size();
		this.pageItems = getPageResults(filteredArray);
	}

	private List<Offer> getPageResults(List<Offer> offerArray) {
		List<Offer> pageResul = new ArrayList<Offer>();
		int initialPosition = 0;
		int finalPosition = 0;
		initialPosition = this.page * getPageSize();
		finalPosition = initialPosition + getPageSize();
		for (int i = initialPosition; i < finalPosition; i++) {
			if (i < offerArray.size()) {
				pageResul.add(offerArray.get(i));
			}
		}
		return pageResul;
	}

	private List<Offer> filterArray(List<Offer> offerArray) {
		List<Offer> pageResul = new ArrayList<Offer>();
		for (Offer off : offerArray) {
			if (isOfferForMe(off)) {
				pageResul.add(off);
				if (off.getIsAccepted() == 0) {
					off.setAcceptedStr(Constants.NO);
				} else {
					off.setAcceptedStr(Constants.YES);
				}
			}
		}
		return pageResul;
	}

	private boolean isOfferForMe(Offer off) {
		if (off.getCachuelo().getUser().getIdUser() == userLogged.getIdUser()) {
			return true;
		} else {
			return false;
		}

	}

	private Predicate[] getSearchPredicates(Root<Offer> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		User user = this.example.getUser();
		if (user != null) {
			predicatesList.add(builder.equal(root.get("user"), user));
		}
		Cachuelo cachuelo = this.example.getCachuelo();
		if (cachuelo != null) {
			predicatesList.add(builder.equal(root.get("cachuelo"), cachuelo));
		}
		String price = this.example.getPrice();
		if (price != null && !"0".equals(price)) {
			predicatesList.add(builder.like(root.<String> get("price"),
					'%' + price + '%'));
		}
		String updatedAt = this.example.getUpdatedAt();
		if (updatedAt != null && !"".equals(updatedAt)) {
			predicatesList.add(builder.like(root.<String> get("updatedAt"),
					'%' + updatedAt + '%'));
		}

		boolean favorite = this.example.getAccepted();
		Integer isAccepted; // this.example.getIsFavorite();
		if (favorite) {
			isAccepted = 1;
		} else {
			isAccepted = 0;
		}
		if (isAccepted != 0) {
			predicatesList
					.add(builder.equal(root.get("isAccepted"), isAccepted));
		}

		// Integer isAccepted = this.example.getIsAccepted();
		// if (isAccepted != null && isAccepted.intValue() != 0)
		// {
		// predicatesList.add(builder.equal(root.get("isAccepted"),
		// isAccepted));
		// }

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Offer> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Offer entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Offer> getAll() {

		CriteriaQuery<Offer> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Offer.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Offer.class))).getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final FrontOfferBean ejbProxy = this.sessionContext
				.getBusinessObject(FrontOfferBean.class);

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

				return String.valueOf(((Offer) value).getIdOffer());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Offer add = new Offer();

	public Offer getAdd() {
		return this.add;
	}

	public Offer getAdded() {
		Offer added = this.add;
		this.add = new Offer();
		return added;
	}

	public String tryAcceptOffer(Offer offe) {
		if (!isClosed("" + offe.getCachuelo().getIdCachuelo())) {
			id = acceptOffer(offe);
			if (id > 0) {
				if (updateCachueloStateToClosed(offe)) {
					return Constants.ACCEPTED_OFFER_SUCCESS;
				} else {
					return Constants.GENERAL_ERROR;
				}

			} else {
				return Constants.GENERAL_ERROR;
			}
		} else {
			return Constants.CACHUELO_IS_CLOSED_ERROR;
		}
	}

	private int acceptOffer(Offer offe) {
		offe.setIsAccepted(1);
		try {
			entityManager.merge(offe);
		} catch (Exception e) {
			return -1;
		}
		return offe.getIdOffer();
	}

	private boolean updateCachueloStateToClosed(Offer offe) {
		TypedQuery<Cachuelo> findByIdQuery = entityManager
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c WHERE c.idCachuelo = :entityId ORDER BY c.idCachuelo",
						Cachuelo.class);
		findByIdQuery.setParameter("entityId", offe.getCachuelo()
				.getIdCachuelo());
		Cachuelo cach;
		try {
			cach = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return false;
		}
		cach.setCachuelostate(getCachueloState("3"));
		cach.setFinalPrice(offe.getPrice());
		try {
			entityManager.merge(cach);
		} catch (Exception e) {
			return false;
		}
		int res = -1;
		res = sendAcceptedOfferToPhone(cach, offe);
		if (res > 0) {
			return true;
		} else {
			return false;
		}
		// return true;

	}

	private Cachuelostate getCachueloState(String id) {
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

	private boolean isClosed(String idCachuelo) {
		TypedQuery<Cachuelo> findByIdQuery = entityManager
				.createQuery(
						"SELECT DISTINCT c FROM Cachuelo c WHERE c.idCachuelo = :entityId ORDER BY c.idCachuelo",
						Cachuelo.class);
		findByIdQuery.setParameter("entityId", Integer.parseInt(idCachuelo));
		Cachuelo cach;
		try {
			cach = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return true;
		}
		if (cach.getCachuelostate().getIdCachueloState() == 3
				|| cach.getCachuelostate().getIdCachueloState() == 4) {
			return true;
		} else {
			return false;
		}
	}

	private int sendAcceptedOfferToPhone(Cachuelo cach, Offer offe) {
		TypedQuery<User> findByIdQuery = entityManager
				.createQuery(
						"SELECT DISTINCT u FROM User u  WHERE u.idUser = :entityId ORDER BY u.idUser",
						User.class);
		findByIdQuery.setParameter("entityId", offe.getUser().getIdUser());
		User userToNotify;
		try {
			userToNotify = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return -1;
		}
		if (userToNotify.getGcmid() == null) {
			System.out.println("GCMID is NULL");
			return 2;
		} else {
			if (userToNotify.getGcmid().compareTo("") == 0) {
				System.out.println("GCMID is empty");
				return 3;
			}
		}
		String strJson = Utility.constructJSON(Constants.TAG_ACCEPTED_OFFER,
				cach.getUser(), cach);
		List<String> partialDevices = new ArrayList<String>();
		partialDevices.add(userToNotify.getGcmid());
		GCMSenderJson senderJson = new GCMSenderJson();
		senderJson.sendMessage(partialDevices, strJson, apikey);
		System.out.println("Message Offer out" + strJson);
		return 1;
	}
}