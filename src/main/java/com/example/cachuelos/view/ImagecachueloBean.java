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

import com.example.cachuelosfrontend.model.Imagecachuelo;
import com.example.cachuelosfrontend.model.Cachuelo;

/**
 * Backing bean for Imagecachuelo entities.
 * <p>
 * This class provides CRUD functionality for all Imagecachuelo entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class ImagecachueloBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Imagecachuelo entities
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

   private Imagecachuelo imagecachuelo;

   public Imagecachuelo getImagecachuelo()
   {
      return this.imagecachuelo;
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
         this.imagecachuelo = this.example;
      }
      else
      {
         this.imagecachuelo = findById(getId());
      }
   }

   public Imagecachuelo findById(Integer id)
   {

      return this.entityManager.find(Imagecachuelo.class, id);
   }

   /*
    * Support updating and deleting Imagecachuelo entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.imagecachuelo);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.imagecachuelo);
            return "view?faces-redirect=true&id=" + this.imagecachuelo.getIdImageCachuelo();
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
         Imagecachuelo deletableEntity = findById(getId());
         Cachuelo cachuelo = deletableEntity.getCachuelo();
         cachuelo.getImagecachuelos().remove(deletableEntity);
         deletableEntity.setCachuelo(null);
         this.entityManager.merge(cachuelo);
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
    * Support searching Imagecachuelo entities with pagination
    */

   private int page;
   private long count;
   private List<Imagecachuelo> pageItems;

   private Imagecachuelo example = new Imagecachuelo();

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

   public Imagecachuelo getExample()
   {
      return this.example;
   }

   public void setExample(Imagecachuelo example)
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
      Root<Imagecachuelo> root = countCriteria.from(Imagecachuelo.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Imagecachuelo> criteria = builder.createQuery(Imagecachuelo.class);
      root = criteria.from(Imagecachuelo.class);
      TypedQuery<Imagecachuelo> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Imagecachuelo> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      Cachuelo cachuelo = this.example.getCachuelo();
      if (cachuelo != null)
      {
         predicatesList.add(builder.equal(root.get("cachuelo"), cachuelo));
      }
      String urlImage = this.example.getUrlImage();
      if (urlImage != null && !"".equals(urlImage))
      {
         predicatesList.add(builder.like(root.<String> get("urlImage"), '%' + urlImage + '%'));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Imagecachuelo> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Imagecachuelo entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Imagecachuelo> getAll()
   {

      CriteriaQuery<Imagecachuelo> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Imagecachuelo.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Imagecachuelo.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final ImagecachueloBean ejbProxy = this.sessionContext.getBusinessObject(ImagecachueloBean.class);

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

            return String.valueOf(((Imagecachuelo) value).getIdImageCachuelo());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Imagecachuelo add = new Imagecachuelo();

   public Imagecachuelo getAdd()
   {
      return this.add;
   }

   public Imagecachuelo getAdded()
   {
      Imagecachuelo added = this.add;
      this.add = new Imagecachuelo();
      return added;
   }
}