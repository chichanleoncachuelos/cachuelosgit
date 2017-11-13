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

import com.example.cachuelosfrontend.model.Typecachuelo;
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Digitalizeddocument;
import com.example.cachuelosfrontend.model.Workerbytypecachuelo;
import java.util.Iterator;

/**
 * Backing bean for Typecachuelo entities.
 * <p>
 * This class provides CRUD functionality for all Typecachuelo entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class TypecachueloBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Typecachuelo entities
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

   private Typecachuelo typecachuelo;

   public Typecachuelo getTypecachuelo()
   {
      return this.typecachuelo;
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
         this.typecachuelo = this.example;
      }
      else
      {
         this.typecachuelo = findById(getId());
      }
   }

   public Typecachuelo findById(Integer id)
   {

      return this.entityManager.find(Typecachuelo.class, id);
   }

   /*
    * Support updating and deleting Typecachuelo entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.typecachuelo);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.typecachuelo);
            return "view?faces-redirect=true&id=" + this.typecachuelo.getIdTypeCachuelo();
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
         Typecachuelo deletableEntity = findById(getId());
         Iterator<Cachuelo> iterCachuelos = deletableEntity.getCachuelos().iterator();
         for (; iterCachuelos.hasNext();)
         {
            Cachuelo nextInCachuelos = iterCachuelos.next();
            nextInCachuelos.setTypecachuelo(null);
            iterCachuelos.remove();
            this.entityManager.merge(nextInCachuelos);
         }
         Iterator<Workerbytypecachuelo> iterWorkerbytypecachuelos = deletableEntity.getWorkerbytypecachuelos().iterator();
         for (; iterWorkerbytypecachuelos.hasNext();)
         {
            Workerbytypecachuelo nextInWorkerbytypecachuelos = iterWorkerbytypecachuelos.next();
            nextInWorkerbytypecachuelos.setTypecachuelo(null);
            iterWorkerbytypecachuelos.remove();
            this.entityManager.merge(nextInWorkerbytypecachuelos);
         }
         Iterator<Digitalizeddocument> iterDigitalizeddocuments = deletableEntity.getDigitalizeddocuments().iterator();
         for (; iterDigitalizeddocuments.hasNext();)
         {
            Digitalizeddocument nextInDigitalizeddocuments = iterDigitalizeddocuments.next();
            nextInDigitalizeddocuments.setTypecachuelo(null);
            iterDigitalizeddocuments.remove();
            this.entityManager.merge(nextInDigitalizeddocuments);
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
    * Support searching Typecachuelo entities with pagination
    */

   private int page;
   private long count;
   private List<Typecachuelo> pageItems;

   private Typecachuelo example = new Typecachuelo();

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

   public Typecachuelo getExample()
   {
      return this.example;
   }

   public void setExample(Typecachuelo example)
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
      Root<Typecachuelo> root = countCriteria.from(Typecachuelo.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Typecachuelo> criteria = builder.createQuery(Typecachuelo.class);
      root = criteria.from(Typecachuelo.class);
      TypedQuery<Typecachuelo> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Typecachuelo> root)
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

   public List<Typecachuelo> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Typecachuelo entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Typecachuelo> getAll()
   {

      CriteriaQuery<Typecachuelo> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Typecachuelo.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Typecachuelo.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final TypecachueloBean ejbProxy = this.sessionContext.getBusinessObject(TypecachueloBean.class);

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

            return String.valueOf(((Typecachuelo) value).getIdTypeCachuelo());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Typecachuelo add = new Typecachuelo();

   public Typecachuelo getAdd()
   {
      return this.add;
   }

   public Typecachuelo getAdded()
   {
      Typecachuelo added = this.add;
      this.add = new Typecachuelo();
      return added;
   }
}