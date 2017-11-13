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

import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.User;

/**
 * Backing bean for Offer entities.
 * <p>
 * This class provides CRUD functionality for all Offer entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class OfferBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Offer entities
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

   private Offer offer;

   public Offer getOffer()
   {
      return this.offer;
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
         this.offer = this.example;
      }
      else
      {
         this.offer = findById(getId());
      }
   }

   public Offer findById(Integer id)
   {

      return this.entityManager.find(Offer.class, id);
   }

   /*
    * Support updating and deleting Offer entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.offer);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.offer);
            return "view?faces-redirect=true&id=" + this.offer.getIdOffer();
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
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
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

   public Offer getExample()
   {
      return this.example;
   }

   public void setExample(Offer example)
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
      Root<Offer> root = countCriteria.from(Offer.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Offer> criteria = builder.createQuery(Offer.class);
      root = criteria.from(Offer.class);
      TypedQuery<Offer> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Offer> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      User user = this.example.getUser();
      if (user != null)
      {
         predicatesList.add(builder.equal(root.get("user"), user));
      }
      Cachuelo cachuelo = this.example.getCachuelo();
      if (cachuelo != null)
      {
         predicatesList.add(builder.equal(root.get("cachuelo"), cachuelo));
      }
      String price = this.example.getPrice();
      if (price != null && !"".equals(price))
      {
         predicatesList.add(builder.like(root.<String> get("price"), '%' + price + '%'));
      }
      String updatedAt = this.example.getUpdatedAt();
      if (updatedAt != null && !"".equals(updatedAt))
      {
         predicatesList.add(builder.like(root.<String> get("updatedAt"), '%' + updatedAt + '%'));
      }
      Integer isAccepted = this.example.getIsAccepted();
      if (isAccepted != null && isAccepted.intValue() != 0)
      {
         predicatesList.add(builder.equal(root.get("isAccepted"), isAccepted));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Offer> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Offer entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Offer> getAll()
   {

      CriteriaQuery<Offer> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Offer.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Offer.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final OfferBean ejbProxy = this.sessionContext.getBusinessObject(OfferBean.class);

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

            return String.valueOf(((Offer) value).getIdOffer());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Offer add = new Offer();

   public Offer getAdd()
   {
      return this.add;
   }

   public Offer getAdded()
   {
      Offer added = this.add;
      this.add = new Offer();
      return added;
   }
}