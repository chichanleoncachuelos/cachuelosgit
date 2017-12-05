package com.example.cachuelosfrontend.view;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
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

import com.example.cachuelosfrontend.model.Digitalizeddocument;
import com.example.cachuelosfrontend.model.Typecachuelo;
import com.example.cachuelosfrontend.model.User;

import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

/**
 * Backing bean for Digitalizeddocument entities.
 * <p>
 * This class provides CRUD functionality for all Digitalizeddocument entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@ManagedBean
@Named
@Stateful
@ConversationScoped
public class FrontDigitalizeddocumentBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Digitalizeddocument entities
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

   private Digitalizeddocument digitalizeddocument;

   public Digitalizeddocument getDigitalizeddocument()
   {
      return this.digitalizeddocument;
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
         this.digitalizeddocument = this.example;
         digitalizeddocument.setName(" ");
      }
      else
      {
         this.digitalizeddocument = findById(getId());
      }
   }

   public Digitalizeddocument findById(Integer id)
   {

      return this.entityManager.find(Digitalizeddocument.class, id);
   }

   /*
    * Support updating and deleting Digitalizeddocument entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.digitalizeddocument);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.digitalizeddocument);
            return "view?faces-redirect=true&id=" + this.digitalizeddocument.getIdDigitalizedDocument();
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
         Digitalizeddocument deletableEntity = findById(getId());
         Typecachuelo typecachuelo = deletableEntity.getTypecachuelo();
         typecachuelo.getDigitalizeddocuments().remove(deletableEntity);
         deletableEntity.setTypecachuelo(null);
         this.entityManager.merge(typecachuelo);
         User user = deletableEntity.getUser();
         user.getDigitalizeddocuments().remove(deletableEntity);
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
    * Support searching Digitalizeddocument entities with pagination
    */

   private int page;
   private long count;
   private List<Digitalizeddocument> pageItems;

   private Digitalizeddocument example = new Digitalizeddocument();

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

   public Digitalizeddocument getExample()
   {
      return this.example;
   }

   public void setExample(Digitalizeddocument example)
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
      Root<Digitalizeddocument> root = countCriteria.from(Digitalizeddocument.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Digitalizeddocument> criteria = builder.createQuery(Digitalizeddocument.class);
      root = criteria.from(Digitalizeddocument.class);
      TypedQuery<Digitalizeddocument> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Digitalizeddocument> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      Typecachuelo typecachuelo = this.example.getTypecachuelo();
      if (typecachuelo != null)
      {
         predicatesList.add(builder.equal(root.get("typecachuelo"), typecachuelo));
      }
      User user = this.example.getUser();
      if (user != null)
      {
         predicatesList.add(builder.equal(root.get("user"), user));
      }
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
      String urlImage = this.example.getUrlImage();
      if (urlImage != null && !"".equals(urlImage))
      {
         predicatesList.add(builder.like(root.<String> get("urlImage"), '%' + urlImage + '%'));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Digitalizeddocument> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Digitalizeddocument entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Digitalizeddocument> getAll()
   {

      CriteriaQuery<Digitalizeddocument> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Digitalizeddocument.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Digitalizeddocument.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final FrontDigitalizeddocumentBean ejbProxy = this.sessionContext.getBusinessObject(FrontDigitalizeddocumentBean.class);

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

            return String.valueOf(((Digitalizeddocument) value).getIdDigitalizedDocument());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Digitalizeddocument add = new Digitalizeddocument();

   public Digitalizeddocument getAdd()
   {
      return this.add;
   }

   public Digitalizeddocument getAdded()
   {
      Digitalizeddocument added = this.add;
      this.add = new Digitalizeddocument();
      return added;
   }
   
	private ArrayList<File> files = new ArrayList<File>();

	public void paint(OutputStream stream, Object object) throws IOException {
		stream.write(getFiles().get((Integer) object).getData());
		stream.close();
	}

	public void listener(FileUploadEvent event) throws Exception {
		String secretKey = UUID.randomUUID().toString();
		UploadedFile item = event.getUploadedFile();
		File file = new File();
		file.setLength(item.getData().length);
		file.setName(secretKey+item.getName());
		file.setData(item.getData());
		files.add(file);
		//cambbios		
		FileOutputStream os = new FileOutputStream("/cachdata/" + secretKey+item.getName());
		os.write(file.getData());
       os.flush();
       os.close();
       digitalizeddocument.setUrlImage(secretKey+item.getName());
       //fin camnbios
	}

	public String clearUploadData() {	
		files.clear();		
        //delete if exists
		Path path = FileSystems.getDefault().getPath("/cachdata/", digitalizeddocument.getUrlImage());
        boolean success;
		try {
			success = Files.deleteIfExists(path);
			System.out.println("Delete status: " + success);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}                		
		digitalizeddocument.setUrlImage("");		
		return "";
	}

	public int getSize() {
		if (getFiles().size() > 0) {
			return getFiles().size();
		} else {
			return 0;
		}
	}

	public long getTimeStamp() {
		return System.currentTimeMillis();
	}

	public ArrayList<File> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<File> files) {
		this.files = files;
	}
   

//   public void change(ValueChangeEvent event) {
//	   String oldValue = (String) event.getOldValue();
//	   String newValue = (String) event.getNewValue();
//       System.out.println("Change: " + oldValue + " to " + newValue);
//       digitalizeddocument.setUrlImage(newValue);       
//   }
}