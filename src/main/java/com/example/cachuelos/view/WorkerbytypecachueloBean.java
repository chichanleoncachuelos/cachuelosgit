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

import com.example.cachuelosfrontend.model.Workerbytypecachuelo;
import com.example.cachuelosfrontend.model.Typecachuelo;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.model.WorkerbytypecachueloId;

/**
 * Backing bean for Workerbytypecachuelo entities.
 * <p>
 * This class provides CRUD functionality for all Workerbytypecachuelo entities.
 * It focuses purely on Java EE 6 standards (e.g.
 * <tt>&#64;ConversationScoped</tt> for state management,
 * <tt>PersistenceContext</tt> for persistence, <tt>CriteriaBuilder</tt> for
 * searches) rather than introducing a CRUD framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class WorkerbytypecachueloBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Workerbytypecachuelo entities
	 */

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Integer idtype;

	public Integer getIdtype() {
		return this.idtype;
	}

	public void setIdtype(Integer idtype) {
		this.idtype = idtype;
	}

	Typecachuelo typecachueloTmp;
	User userTmp;
	Workerbytypecachuelo workerbytypecachueloTmp;

	private Workerbytypecachuelo workerbytypecachuelo;

	public Workerbytypecachuelo getWorkerbytypecachuelo() {
		return this.workerbytypecachuelo;
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

		if (this.id == null && this.idtype == null) {
			this.workerbytypecachuelo = this.example;
		} else {
			this.workerbytypecachuelo = findByIdbyClass(getId(), getIdtype());
			if (this.workerbytypecachuelo.getIsAvailable() == 0) {
				this.workerbytypecachuelo.setAvailable(false);
			} else {
				this.workerbytypecachuelo.setAvailable(true);
			}
			typecachueloTmp = this.workerbytypecachuelo.getTypecachuelo();
			userTmp = this.workerbytypecachuelo.getUser();
			workerbytypecachueloTmp = this.workerbytypecachuelo;
		}
	}

	public Workerbytypecachuelo findById(Integer id) {

		return this.entityManager.find(Workerbytypecachuelo.class, id);
	}

	public Workerbytypecachuelo findByIdbyClass(Integer idUser, Integer idType) {

		WorkerbytypecachueloId tmpWorkerbytype = new WorkerbytypecachueloId(
				idUser, idType);
		return this.entityManager.find(Workerbytypecachuelo.class,
				tmpWorkerbytype);
	}

	/*
	 * Support updating and deleting Workerbytypecachuelo entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.workerbytypecachuelo.getAvailable()) {
				this.workerbytypecachuelo.setIsAvailable(1);
			} else {
				this.workerbytypecachuelo.setIsAvailable(0);
			}

			if (this.id == null && this.idtype == null) {
				WorkerbytypecachueloId tmpWorkerbytype = new WorkerbytypecachueloId(
						this.workerbytypecachuelo.getUser().getIdUser(),
						this.workerbytypecachuelo.getTypecachuelo()
								.getIdTypeCachuelo());
				this.workerbytypecachuelo.setId(tmpWorkerbytype);
				this.entityManager.persist(this.workerbytypecachuelo);
				return "search?faces-redirect=true";
			} else {
				User userAux = this.workerbytypecachuelo.getUser();
				Typecachuelo typecachueloAux = this.workerbytypecachuelo
						.getTypecachuelo();
				Integer isAvailableAux =this.workerbytypecachuelo.getIsAvailable();

				typecachueloTmp.getWorkerbytypecachuelos().remove(
						workerbytypecachueloTmp);
				workerbytypecachueloTmp.setTypecachuelo(null);
				this.entityManager.merge(typecachueloTmp);
				userTmp.getWorkerbytypecachuelos().remove(
						workerbytypecachueloTmp);
				workerbytypecachueloTmp.setUser(null);
				this.entityManager.merge(userTmp);
				this.entityManager.remove(workerbytypecachueloTmp);
				this.entityManager.flush();

				// this.workerbytypecachuelo.getId().setIdUserWorker(this.workerbytypecachuelo.getUser().getIdUser());
				// this.workerbytypecachuelo.getId().setIdTypeCachuelo(this.workerbytypecachuelo.getTypecachuelo().getIdTypeCachuelo());
				WorkerbytypecachueloId tmpWorkerbytype = new WorkerbytypecachueloId(
						userAux.getIdUser(),
						typecachueloAux.getIdTypeCachuelo());
				this.workerbytypecachuelo.setId(tmpWorkerbytype);
				this.workerbytypecachuelo.setIsAvailable(isAvailableAux);
				this.workerbytypecachuelo.setUser(userAux);
				this.workerbytypecachuelo.setTypecachuelo(typecachueloAux);				
				this.entityManager.persist(this.workerbytypecachuelo);

				return "view?faces-redirect=true&id="
						+ this.workerbytypecachuelo.getUser().getIdUser()
						+ "&idtype="
						+ this.workerbytypecachuelo.getTypecachuelo()
								.getIdTypeCachuelo();
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
			Workerbytypecachuelo deletableEntity = findByIdbyClass(getId(),
					getIdtype());
			Typecachuelo typecachuelo = deletableEntity.getTypecachuelo();
			typecachuelo.getWorkerbytypecachuelos().remove(deletableEntity);
			deletableEntity.setTypecachuelo(null);
			this.entityManager.merge(typecachuelo);
			User user = deletableEntity.getUser();
			user.getWorkerbytypecachuelos().remove(deletableEntity);
			deletableEntity.setUser(null);
			this.entityManager.merge(user);
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
	 * Support searching Workerbytypecachuelo entities with pagination
	 */

	private int page;
	private long count;
	private List<Workerbytypecachuelo> pageItems;

	private Workerbytypecachuelo example = new Workerbytypecachuelo();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Workerbytypecachuelo getExample() {
		return this.example;
	}

	public void setExample(Workerbytypecachuelo example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Workerbytypecachuelo> root = countCriteria
				.from(Workerbytypecachuelo.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Workerbytypecachuelo> criteria = builder
				.createQuery(Workerbytypecachuelo.class);
		root = criteria.from(Workerbytypecachuelo.class);
		TypedQuery<Workerbytypecachuelo> query = this.entityManager
				.createQuery(criteria.select(root).where(
						getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		List<Workerbytypecachuelo> tmpList = query.getResultList();
		this.pageItems = tmpList;
	}

	private Predicate[] getSearchPredicates(Root<Workerbytypecachuelo> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		Typecachuelo typecachuelo = this.example.getTypecachuelo();
		if (typecachuelo != null) {
			predicatesList.add(builder.equal(root.get("typecachuelo"),
					typecachuelo));
		}
		User user = this.example.getUser();
		if (user != null) {
			predicatesList.add(builder.equal(root.get("user"), user));
		}
		Integer isAvailable = 0;
		if (this.example.getAvailable()) {
			isAvailable = 1;
		} else {
			isAvailable = 0;
		}
		if (isAvailable != null && isAvailable.intValue() != 0) {
			predicatesList.add(builder.equal(root.get("isAvailable"),
					isAvailable));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Workerbytypecachuelo> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Workerbytypecachuelo entities (e.g. from
	 * inside an HtmlSelectOneMenu)
	 */

	public List<Workerbytypecachuelo> getAll() {

		CriteriaQuery<Workerbytypecachuelo> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Workerbytypecachuelo.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Workerbytypecachuelo.class)))
				.getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final WorkerbytypecachueloBean ejbProxy = this.sessionContext
				.getBusinessObject(WorkerbytypecachueloBean.class);

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

				return String.valueOf(((Workerbytypecachuelo) value).getId());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Workerbytypecachuelo add = new Workerbytypecachuelo();

	public Workerbytypecachuelo getAdd() {
		return this.add;
	}

	public Workerbytypecachuelo getAdded() {
		Workerbytypecachuelo added = this.add;
		this.add = new Workerbytypecachuelo();
		return added;
	}
}