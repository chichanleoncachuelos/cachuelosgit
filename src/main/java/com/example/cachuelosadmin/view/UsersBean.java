package com.example.cachuelosadmin.view;

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

import com.example.cachuelosadmin.model.Users;
import com.example.cachuelosadmin.model.Userroles;
import com.example.cachuelos.utils.PasswordEncrypterSHABase64;

import java.util.Iterator;

/**
 * Backing bean for Users entities.
 * <p>
 * This class provides CRUD functionality for all Users entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class UsersBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Users entities
	 */

	private String id;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private Users users;

	public Users getUsers() {
		return this.users;
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
			this.users = this.example;			
		} else {
			this.users = findById(getId());
		}
	}

	public Users findById(String id) {

		return this.entityManager.find(Users.class, id);
	}

	/*
	 * Support updating and deleting Users entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.id == null) {
				String pwd = PasswordEncrypterSHABase64.encryptPassword(this.users.getPasswd());
				this.users.setPasswd(pwd);
				this.entityManager.persist(this.users);

				return "search?faces-redirect=true";
			} else {
				String pwd = PasswordEncrypterSHABase64.encryptPassword(this.users.getPasswd());
				this.users.setPasswd(pwd);
				this.entityManager.merge(this.users);
				return "view?faces-redirect=true&id="
						+ this.users.getUsername();
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
			Users deletableEntity = findById(getId());
			Iterator<Userroles> iterUserroleses = deletableEntity
					.getUserroleses().iterator();
			for (; iterUserroleses.hasNext();) {
				Userroles nextInUserroleses = iterUserroleses.next();
				nextInUserroleses.setUsers(null);
				iterUserroleses.remove();
				this.entityManager.merge(nextInUserroleses);
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
	 * Support searching Users entities with pagination
	 */

	private int page;
	private long count;
	private List<Users> pageItems;

	private Users example = new Users();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Users getExample() {
		return this.example;
	}

	public void setExample(Users example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Users> root = countCriteria.from(Users.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Users> criteria = builder.createQuery(Users.class);
		root = criteria.from(Users.class);
		TypedQuery<Users> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Users> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String username = this.example.getUsername();
		if (username != null && !"".equals(username)) {
			predicatesList.add(builder.like(root.<String> get("username"),
					'%' + username + '%'));
		}
		String passwd = this.example.getPasswd();
		if (passwd != null && !"".equals(passwd)) {
			predicatesList.add(builder.like(root.<String> get("passwd"),
					'%' + passwd + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Users> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Users entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Users> getAll() {

		CriteriaQuery<Users> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Users.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Users.class))).getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final UsersBean ejbProxy = this.sessionContext
				.getBusinessObject(UsersBean.class);

		return new Converter() {

			@Override
			public Object getAsObject(FacesContext context,
					UIComponent component, String value) {

				return ejbProxy.findById(String.valueOf(value));
			}

			@Override
			public String getAsString(FacesContext context,
					UIComponent component, Object value) {

				if (value == null) {
					return "";
				}

				return String.valueOf(((Users) value).getUsername());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Users add = new Users();

	public Users getAdd() {
		return this.add;
	}

	public Users getAdded() {
		Users added = this.add;
		this.add = new Users();
		return added;
	}
}