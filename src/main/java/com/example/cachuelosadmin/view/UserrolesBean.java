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

import com.example.cachuelosadmin.model.Userroles;
import com.example.cachuelosadmin.model.Users;

/**
 * Backing bean for Userroles entities.
 * <p>
 * This class provides CRUD functionality for all Userroles entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class UserrolesBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Userroles entities
    */

   private Integer id;

   public Integer getId()
   {
      return this.id;
   }

   public void setId(Integer id)
   {
      this.id = id;
   }

   private Userroles userroles;

   public Userroles getUserroles()
   {
      return this.userroles;
   }

   @Inject
   private Conversation conversation;

   @PersistenceContext(type = PersistenceContextType.EXTENDED)
   private EntityManager entityManager;

   public String create()
   {

      this.conversation.begin();
      return "create?faces-redirect=true";
   }

   public void retrieve()
   {

      if (FacesContext.getCurrentInstance().isPostback())
      {
         return;
      }

      if (this.conversation.isTransient())
      {
         this.conversation.begin();
      }

      if (this.id == null)
      {
         this.userroles = this.example;
      }
      else
      {
         this.userroles = findById(getId());
      }
   }

   public Userroles findById(Integer id)
   {

      return this.entityManager.find(Userroles.class, id);
   }

   /*
    * Support updating and deleting Userroles entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.userroles);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.userroles);
            return "view?faces-redirect=true&id=" + this.userroles.getId();
         }
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   public String delete()
   {
      this.conversation.end();

      try
      {
         Userroles deletableEntity = findById(getId());
         Users users = deletableEntity.getUsers();
         users.getUserroleses().remove(deletableEntity);
         deletableEntity.setUsers(null);
         this.entityManager.merge(users);
         this.entityManager.remove(deletableEntity);
         this.entityManager.flush();
         return "search?faces-redirect=true";
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   /*
    * Support searching Userroles entities with pagination
    */

   private int page;
   private long count;
   private List<Userroles> pageItems;

   private Userroles example = new Userroles();

   public int getPage()
   {
      return this.page;
   }

   public void setPage(int page)
   {
      this.page = page;
   }

   public int getPageSize()
   {
      return 10;
   }

   public Userroles getExample()
   {
      return this.example;
   }

   public void setExample(Userroles example)
   {
      this.example = example;
   }

   public void search()
   {
      this.page = 0;
   }

   public void paginate()
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

      // Populate this.count

      CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
      Root<Userroles> root = countCriteria.from(Userroles.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Userroles> criteria = builder.createQuery(Userroles.class);
      root = criteria.from(Userroles.class);
      TypedQuery<Userroles> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Userroles> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      Users users = this.example.getUsers();
      if (users != null)
      {
         predicatesList.add(builder.equal(root.get("users"), users));
      }
      String userRoles = this.example.getUserRoles();
      if (userRoles != null && !"".equals(userRoles))
      {
         predicatesList.add(builder.like(root.<String> get("userRoles"), '%' + userRoles + '%'));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Userroles> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Userroles entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Userroles> getAll()
   {

      CriteriaQuery<Userroles> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Userroles.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Userroles.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final UserrolesBean ejbProxy = this.sessionContext.getBusinessObject(UserrolesBean.class);

      return new Converter()
      {

         @Override
         public Object getAsObject(FacesContext context,
               UIComponent component, String value)
         {

            return ejbProxy.findById(Integer.valueOf(value));
         }

         @Override
         public String getAsString(FacesContext context,
               UIComponent component, Object value)
         {

            if (value == null)
            {
               return "";
            }

            return String.valueOf(((Userroles) value).getId());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Userroles add = new Userroles();

   public Userroles getAdd()
   {
      return this.add;
   }

   public Userroles getAdded()
   {
      Userroles added = this.add;
      this.add = new Userroles();
      return added;
   }
}