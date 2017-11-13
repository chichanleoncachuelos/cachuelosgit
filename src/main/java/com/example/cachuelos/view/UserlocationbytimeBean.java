package com.example.cachuelos.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.example.cachuelosfrontend.model.Userlocationbytime;
import com.example.cachuelosfrontend.model.User;

/**
 * Backing bean for Userlocationbytime entities.
 * <p>
 * This class provides CRUD functionality for all Userlocationbytime entities.
 * It focuses purely on Java EE 6 standards (e.g.
 * <tt>&#64;ConversationScoped</tt> for state management,
 * <tt>PersistenceContext</tt> for persistence, <tt>CriteriaBuilder</tt> for
 * searches) rather than introducing a CRUD framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class UserlocationbytimeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Userlocationbytime entities
	 */

	String host = "";
	
	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Userlocationbytime userlocationbytime;

	public Userlocationbytime getUserlocationbytime() {
		return this.userlocationbytime;
	}

	@Inject
	private Conversation conversation;
	
	@Inject
	ApplicationBean applicationBean;
	
	@PostConstruct
	private void init() {
		System.out.println("constructor");
		host = applicationBean.getIpAddress();
	}

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
			this.userlocationbytime = this.example;
		} else {
			this.userlocationbytime = findById(getId());
		}
	}

	public Userlocationbytime findById(Integer id) {

		return this.entityManager.find(Userlocationbytime.class, id);
	}

	/*
	 * Support updating and deleting Userlocationbytime entities
	 */

	public String update() {
		this.conversation.end();

		try {
			if (this.id == null) {
				// this.entityManager.persist(this.userlocationbytime);
				// return "search?faces-redirect=true";

				// ExternalContext context =
				// FacesContext.getCurrentInstance().getExternalContext();
				// try {
				// context.redirect(context.getRequestContextPath() +
				// "/rest/userlocationbytimes/requestuserlocation/"+this.userlocationbytime.getUser().getIdUser().toString());
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// return "";
				// }
				sendLocationRequestWS(
						"/rest/userlocationbytimes/requestuserlocation/",
						this.userlocationbytime.getUser().getIdUser()
								.toString());
				return "search?faces-redirect=true";
			} else {
				this.entityManager.merge(this.userlocationbytime);
				return "view?faces-redirect=true&id="
						+ this.userlocationbytime.getIdUserLocationByTime();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	private static final Executor threadPool = Executors.newFixedThreadPool(5);
	private String ret = "";

	public void sendLocationRequestWS(String url, String idUser) {
		final String strUrl = new String(url);
		final String strIdUser = new String(idUser);
		threadPool.execute(new Runnable() {
			public void run() {

				HttpClient client = HttpClientBuilder.create().build();

				String strComplete = "http://"+host+ strUrl + strIdUser;
				HttpGet httpGet = new HttpGet(strComplete);

				HttpResponse response = null;

				try {
					response = client.execute(httpGet);
					if (response != null) {
						if (response.getStatusLine() != null) {
							if (response.getStatusLine().getStatusCode() == 200) {
								ret = EntityUtils.toString(response.getEntity());
							}
						} else
							ret = EntityUtils.toString(response.getEntity());
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("UserLocation response"
						+ response.toString());
				System.out.println("UserLocation return" + ret);
			}
		});
		// return ret;// siempre retorna nulo por los hilos
	}

	public String delete() {
		this.conversation.end();

		try {
			Userlocationbytime deletableEntity = findById(getId());
			User user = deletableEntity.getUser();
			user.getUserlocationbytimes().remove(deletableEntity);
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
	 * Support searching Userlocationbytime entities with pagination
	 */

	private int page;
	private long count;
	private List<Userlocationbytime> pageItems;

	private Userlocationbytime example = new Userlocationbytime();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Userlocationbytime getExample() {
		return this.example;
	}

	public void setExample(Userlocationbytime example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Userlocationbytime> root = countCriteria
				.from(Userlocationbytime.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Userlocationbytime> criteria = builder
				.createQuery(Userlocationbytime.class);
		root = criteria.from(Userlocationbytime.class);
		TypedQuery<Userlocationbytime> query = this.entityManager
				.createQuery(criteria.select(root).where(
						getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Userlocationbytime> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		User user = this.example.getUser();
		if (user != null) {
			predicatesList.add(builder.equal(root.get("user"), user));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Userlocationbytime> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Userlocationbytime entities (e.g. from
	 * inside an HtmlSelectOneMenu)
	 */

	public List<Userlocationbytime> getAll() {

		CriteriaQuery<Userlocationbytime> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Userlocationbytime.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Userlocationbytime.class)))
				.getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final UserlocationbytimeBean ejbProxy = this.sessionContext
				.getBusinessObject(UserlocationbytimeBean.class);

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

				return String.valueOf(((Userlocationbytime) value)
						.getIdUserLocationByTime());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Userlocationbytime add = new Userlocationbytime();

	public Userlocationbytime getAdd() {
		return this.add;
	}

	public Userlocationbytime getAdded() {
		Userlocationbytime added = this.add;
		this.add = new Userlocationbytime();
		return added;
	}
}