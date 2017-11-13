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

import com.example.cachuelosfrontend.model.Commentrating;
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Commentratingtype;
import com.example.cachuelosfrontend.model.User;

/**
 * Backing bean for Commentrating entities.
 * <p>
 * This class provides CRUD functionality for all Commentrating entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class CommentratingBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Commentrating entities
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

   private Commentrating commentrating;

   public Commentrating getCommentrating()
   {
      return this.commentrating;
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
         this.commentrating = this.example;
      }
      else
      {
         this.commentrating = findById(getId());
      }
   }

   public Commentrating findById(Integer id)
   {

      return this.entityManager.find(Commentrating.class, id);
   }

   /*
    * Support updating and deleting Commentrating entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.commentrating);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.commentrating);
            return "view?faces-redirect=true&id=" + this.commentrating.getIdComment();
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
         Commentrating deletableEntity = findById(getId());
         Commentratingtype commentratingtype = deletableEntity.getCommentratingtype();
         commentratingtype.getCommentratings().remove(deletableEntity);
         deletableEntity.setCommentratingtype(null);
         this.entityManager.merge(commentratingtype);
         Cachuelo cachuelo = deletableEntity.getCachuelo();
         cachuelo.getCommentratings().remove(deletableEntity);
         deletableEntity.setCachuelo(null);
         this.entityManager.merge(cachuelo);
         User user = deletableEntity.getUser();
         user.getCommentratings().remove(deletableEntity);
         deletableEntity.setUser(null);
         this.entityManager.merge(user);
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
    * Support searching Commentrating entities with pagination
    */

   private int page;
   private long count;
   private List<Commentrating> pageItems;

   private Commentrating example = new Commentrating();

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

   public Commentrating getExample()
   {
      return this.example;
   }

   public void setExample(Commentrating example)
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
      Root<Commentrating> root = countCriteria.from(Commentrating.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Commentrating> criteria = builder.createQuery(Commentrating.class);
      root = criteria.from(Commentrating.class);
      TypedQuery<Commentrating> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Commentrating> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      Commentratingtype commentratingtype = this.example.getCommentratingtype();
      if (commentratingtype != null)
      {
         predicatesList.add(builder.equal(root.get("commentratingtype"), commentratingtype));
      }
      Cachuelo cachuelo = this.example.getCachuelo();
      if (cachuelo != null)
      {
         predicatesList.add(builder.equal(root.get("cachuelo"), cachuelo));
      }
      User user = this.example.getUser();
      if (user != null)
      {
         predicatesList.add(builder.equal(root.get("user"), user));
      }
      String updatedAt = this.example.getUpdatedAt();
      if (updatedAt != null && !"".equals(updatedAt))
      {
         predicatesList.add(builder.like(root.<String> get("updatedAt"), '%' + updatedAt + '%'));
      }
      String comment = this.example.getComment();
      if (comment != null && !"".equals(comment))
      {
         predicatesList.add(builder.like(root.<String> get("comment"), '%' + comment + '%'));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Commentrating> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Commentrating entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Commentrating> getAll()
   {

      CriteriaQuery<Commentrating> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Commentrating.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Commentrating.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final CommentratingBean ejbProxy = this.sessionContext.getBusinessObject(CommentratingBean.class);

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

            return String.valueOf(((Commentrating) value).getIdComment());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Commentrating add = new Commentrating();

   public Commentrating getAdd()
   {
      return this.add;
   }

   public Commentrating getAdded()
   {
      Commentrating added = this.add;
      this.add = new Commentrating();
      return added;
   }
}