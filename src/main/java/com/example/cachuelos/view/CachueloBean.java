package com.example.cachuelos.view;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.ValueChangeEvent;
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
import com.example.cachuelosfrontend.model.Commentrating;
import com.example.cachuelosfrontend.model.Imagecachuelo;
import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.Message;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.Typecachuelo;
import com.example.cachuelosfrontend.model.User;

import java.util.Iterator;

/**
 * Backing bean for Cachuelo entities.
 * <p>
 * This class provides CRUD functionality for all Cachuelo entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class CachueloBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Cachuelo entities
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

   private Cachuelo cachuelo;

   public Cachuelo getCachuelo()
   {
      return this.cachuelo;
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
         this.cachuelo = this.example;
      }
      else
      {
         this.cachuelo = findById(getId());
      }
      
      locale = Locale.US;
      popup = true;
      pattern = "MMM d, yyyy";
      jointPoint = "bottomleft";
      direction = "bottomright";
      readonly = true;
      enableManualInput=false;
      showInput=true;
      boundary = "inactive";
   }

   public Cachuelo findById(Integer id)
   {

      return this.entityManager.find(Cachuelo.class, id);
   }

   /*
    * Support updating and deleting Cachuelo entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.cachuelo);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.cachuelo);
            return "view?faces-redirect=true&id=" + this.cachuelo.getIdCachuelo();
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
         Cachuelo deletableEntity = findById(getId());
         Typecachuelo typecachuelo = deletableEntity.getTypecachuelo();
         typecachuelo.getCachuelos().remove(deletableEntity);
         deletableEntity.setTypecachuelo(null);
         this.entityManager.merge(typecachuelo);
         Cachuelostate cachuelostate = deletableEntity.getCachuelostate();
         cachuelostate.getCachuelos().remove(deletableEntity);
         deletableEntity.setCachuelostate(null);
         this.entityManager.merge(cachuelostate);
         Location location = deletableEntity.getLocation();
         location.getCachuelos().remove(deletableEntity);
         deletableEntity.setLocation(null);
         this.entityManager.merge(location);
         User user = deletableEntity.getUser();
         user.getCachuelos().remove(deletableEntity);
         deletableEntity.setUser(null);
         this.entityManager.merge(user);
         Iterator<Commentrating> iterCommentratings = deletableEntity.getCommentratings().iterator();
         for (; iterCommentratings.hasNext();)
         {
            Commentrating nextInCommentratings = iterCommentratings.next();
            nextInCommentratings.setCachuelo(null);
            iterCommentratings.remove();
            this.entityManager.merge(nextInCommentratings);
         }
         Iterator<Offer> iterOffers = deletableEntity.getOffers().iterator();
         for (; iterOffers.hasNext();)
         {
            Offer nextInOffers = iterOffers.next();
            nextInOffers.setCachuelo(null);
            iterOffers.remove();
            this.entityManager.merge(nextInOffers);
         }
         Iterator<Imagecachuelo> iterImagecachuelos = deletableEntity.getImagecachuelos().iterator();
         for (; iterImagecachuelos.hasNext();)
         {
            Imagecachuelo nextInImagecachuelos = iterImagecachuelos.next();
            nextInImagecachuelos.setCachuelo(null);
            iterImagecachuelos.remove();
            this.entityManager.merge(nextInImagecachuelos);
         }
         Iterator<Message> iterMessages = deletableEntity.getMessages().iterator();
         for (; iterMessages.hasNext();)
         {
            Message nextInMessages = iterMessages.next();
            nextInMessages.setCachuelo(null);
            iterMessages.remove();
            this.entityManager.merge(nextInMessages);
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
    * Support searching Cachuelo entities with pagination
    */

   private int page;
   private long count;
   private List<Cachuelo> pageItems;

   private Cachuelo example = new Cachuelo();

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

   public Cachuelo getExample()
   {
      return this.example;
   }

   public void setExample(Cachuelo example)
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
      Root<Cachuelo> root = countCriteria.from(Cachuelo.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Cachuelo> criteria = builder.createQuery(Cachuelo.class);
      root = criteria.from(Cachuelo.class);
      TypedQuery<Cachuelo> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Cachuelo> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      Typecachuelo typecachuelo = this.example.getTypecachuelo();
      if (typecachuelo != null)
      {
         predicatesList.add(builder.equal(root.get("typecachuelo"), typecachuelo));
      }
      Cachuelostate cachuelostate = this.example.getCachuelostate();
      if (cachuelostate != null)
      {
         predicatesList.add(builder.equal(root.get("cachuelostate"), cachuelostate));
      }
      Location location = this.example.getLocation();
      if (location != null)
      {
         predicatesList.add(builder.equal(root.get("location"), location));
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

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Cachuelo> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Cachuelo entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Cachuelo> getAll()
   {

      CriteriaQuery<Cachuelo> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Cachuelo.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Cachuelo.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final CachueloBean ejbProxy = this.sessionContext.getBusinessObject(CachueloBean.class);

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

            return String.valueOf(((Cachuelo) value).getIdCachuelo());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Cachuelo add = new Cachuelo();

   public Cachuelo getAdd()
   {
      return this.add;
   }

   public Cachuelo getAdded()
   {
      Cachuelo added = this.add;
      this.add = new Cachuelo();
      return added;
   }
   
   private static final String[] WEEK_DAY_LABELS = new String[] { "Sun *",
       "Mon +", "Tue +", "Wed +", "Thu +", "Fri +", "Sat *" };
private Locale locale;

private boolean popup;
private boolean readonly;
private boolean showInput;
private boolean enableManualInput;    
private String pattern;
private Date currentDate;
private Date selectedDate;
private String jointPoint;
private String direction;
private String boundary;

private boolean useCustomDayLabels;

public Locale getLocale() {
   return locale;
}

public void setLocale(Locale locale) {
   this.locale = locale;
}

public boolean isPopup() {
   return popup;
}

public void setPopup(boolean popup) {
   this.popup = popup;
}

public String getPattern() {
   return pattern;
}

public void setPattern(String pattern) {
   this.pattern = pattern;
}

//public CalendarBean() {
//
//   locale = Locale.US;
//   popup = true;
//   pattern = "MMM d, yyyy";
//   jointPoint = "bottomleft";
//   direction = "bottomright";
//   readonly = true;
//   enableManualInput=false;
//   showInput=true;
//   boundary = "inactive";
//}


public boolean isShowInput() {
   return showInput;
}

public void setShowInput(boolean showInput) {
   this.showInput = showInput;
}

public boolean isEnableManualInput() {
   return enableManualInput;
}

public void setEnableManualInput(boolean enableManualInput) {
   this.enableManualInput = enableManualInput;
}

public boolean isReadonly() {
   return readonly;
}

public void setReadonly(boolean readonly) {
   this.readonly = readonly;
}

public void selectLocale(ValueChangeEvent event) {

   String tLocale = (String) event.getNewValue();
   if (tLocale != null) {
       String lang = tLocale.substring(0, 2);
       String country = tLocale.substring(3);
       locale = new Locale(lang, country, "");
   }
}

public boolean isUseCustomDayLabels() {
   return useCustomDayLabels;
}

public void setUseCustomDayLabels(boolean useCustomDayLabels) {
   this.useCustomDayLabels = useCustomDayLabels;
}

public Object getWeekDayLabelsShort() {
   if (isUseCustomDayLabels()) {
       return WEEK_DAY_LABELS;
   } else {
       return null;
   }
}

public String getCurrentDateAsText() {
   Date currentDate = getCurrentDate();
   if (currentDate != null) {
       return DateFormat.getDateInstance(DateFormat.FULL).format(
               currentDate);
   }

   return null;
}

public Date getCurrentDate() {
   return currentDate;
}

public void setCurrentDate(Date currentDate) {
   this.currentDate = currentDate;
}

public Date getSelectedDate() {
   return selectedDate;
}

public void setSelectedDate(Date selectedDate) {
   this.selectedDate = selectedDate;
}

public String getJointPoint() {
   return jointPoint;
}

public void setJointPoint(String jointPoint) {
   this.jointPoint = jointPoint;
}

public void selectJointPoint(ValueChangeEvent event) {
   jointPoint = (String) event.getNewValue();
}

public String getDirection() {
   return direction;
}

public void setDirection(String direction) {
   this.direction = direction;
}

public void selectDirection(ValueChangeEvent event) {
   direction = (String) event.getNewValue();
}

public String getBoundary() {
   return boundary;
}

public void setBoundary(String boundary) {
   this.boundary = boundary;
}

}