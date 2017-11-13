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

import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Cachuelostate;

import java.util.Iterator;

/**
 * Backing bean for Cachuelostate entities.
 * <p>
 * This class provides CRUD functionality for all Cachuelostate entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class FrontCachuelostateBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Cachuelostate entities
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

   private Cachuelostate cachuelostate;

   public Cachuelostate getCachuelostate()
   {
      return this.cachuelostate;
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
         this.cachuelostate = this.example;
      }
      else
      {
         this.cachuelostate = findById(getId());
      }
   }

   public Cachuelostate findById(Integer id)
   {

      return this.entityManager.find(Cachuelostate.class, id);
   }

   /*
    * Support updating and deleting Cachuelostate entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.cachuelostate);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.cachuelostate);
            return "view?faces-redirect=true&id=" + this.cachuelostate.getIdCachueloState();
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
         Cachuelostate deletableEntity = findById(getId());
         Iterator<Cachuelo> iterCachuelos = deletableEntity.getCachuelos().iterator();
         for (; iterCachuelos.hasNext();)
         {
            Cachuelo nextInCachuelos = iterCachuelos.next();
            nextInCachuelos.setCachuelostate(null);
            iterCachuelos.remove();
            this.entityManager.merge(nextInCachuelos);
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
    * Support searching Cachuelostate entities with pagination
    */

   private int page;
   private long count;
   private List<Cachuelostate> pageItems;

   private Cachuelostate example = new Cachuelostate();

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

   public Cachuelostate getExample()
   {
      return this.example;
   }

   public void setExample(Cachuelostate example)
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
      Root<Cachuelostate> root = countCriteria.from(Cachuelostate.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Cachuelostate> criteria = builder.createQuery(Cachuelostate.class);
      root = criteria.from(Cachuelostate.class);
      TypedQuery<Cachuelostate> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Cachuelostate> root)
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

   public List<Cachuelostate> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Cachuelostate entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Cachuelostate> getAll()
   {

      CriteriaQuery<Cachuelostate> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Cachuelostate.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Cachuelostate.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final FrontCachuelostateBean ejbProxy = this.sessionContext.getBusinessObject(FrontCachuelostateBean.class);

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

            return String.valueOf(((Cachuelostate) value).getIdCachueloState());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Cachuelostate add = new Cachuelostate();

   public Cachuelostate getAdd()
   {
      return this.add;
   }

   public Cachuelostate getAdded()
   {
      Cachuelostate added = this.add;
      this.add = new Cachuelostate();
      return added;
   }
}