package com.example.cachuelosfrontend.view;

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

import com.example.cachuelosfrontend.model.Commentrating;
import com.example.cachuelosfrontend.model.Commentratingtype;

import java.util.Iterator;

/**
 * Backing bean for Commentratingtype entities.
 * <p>
 * This class provides CRUD functionality for all Commentratingtype entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class FrontCommentratingtypeBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Commentratingtype entities
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

   private Commentratingtype commentratingtype;

   public Commentratingtype getCommentratingtype()
   {
      return this.commentratingtype;
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
         this.commentratingtype = this.example;
      }
      else
      {
         this.commentratingtype = findById(getId());
      }
   }

   public Commentratingtype findById(Integer id)
   {

      return this.entityManager.find(Commentratingtype.class, id);
   }

   /*
    * Support updating and deleting Commentratingtype entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.commentratingtype);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.commentratingtype);
            return "view?faces-redirect=true&id=" + this.commentratingtype.getIdCommentType();
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
         Commentratingtype deletableEntity = findById(getId());
         Iterator<Commentrating> iterCommentratings = deletableEntity.getCommentratings().iterator();
         for (; iterCommentratings.hasNext();)
         {
            Commentrating nextInCommentratings = iterCommentratings.next();
            nextInCommentratings.setCommentratingtype(null);
            iterCommentratings.remove();
            this.entityManager.merge(nextInCommentratings);
         }
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
    * Support searching Commentratingtype entities with pagination
    */

   private int page;
   private long count;
   private List<Commentratingtype> pageItems;

   private Commentratingtype example = new Commentratingtype();

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

   public Commentratingtype getExample()
   {
      return this.example;
   }

   public void setExample(Commentratingtype example)
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
      Root<Commentratingtype> root = countCriteria.from(Commentratingtype.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Commentratingtype> criteria = builder.createQuery(Commentratingtype.class);
      root = criteria.from(Commentratingtype.class);
      TypedQuery<Commentratingtype> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Commentratingtype> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      String name = this.example.getName();
      if (name != null && !"".equals(name))
      {
         predicatesList.add(builder.like(root.<String> get("name"), '%' + name + '%'));
      }
      String description = this.example.getDescription();
      if (description != null && !"".equals(description))
      {
         predicatesList.add(builder.like(root.<String> get("description"), '%' + description + '%'));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Commentratingtype> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Commentratingtype entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Commentratingtype> getAll()
   {

      CriteriaQuery<Commentratingtype> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Commentratingtype.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Commentratingtype.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final FrontCommentratingtypeBean ejbProxy = this.sessionContext.getBusinessObject(FrontCommentratingtypeBean.class);

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

            return String.valueOf(((Commentratingtype) value).getIdCommentType());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Commentratingtype add = new Commentratingtype();

   public Commentratingtype getAdd()
   {
      return this.add;
   }

   public Commentratingtype getAdded()
   {
      Commentratingtype added = this.add;
      this.add = new Commentratingtype();
      return added;
   }
}