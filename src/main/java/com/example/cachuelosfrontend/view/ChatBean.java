package com.example.cachuelosfrontend.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.criteria.CriteriaQuery;

import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Message;
import com.example.cachuelosfrontend.model.User;
import com.example.cachuelosfrontend.utils.Constants;
import com.example.cachuelos.gcmserver.GCMSenderJson;
import com.example.cachuelos.utils.Utility;

/**
 * Backing bean for Cachuelo entities.
 * <p>
 * This class provides CRUD functionality for all Cachuelo entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class ChatBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String apikey = "";

	@Inject
	SimpleLoginBean simpleLoginBean;

	User userLogged;

	@PostConstruct
	private void initialize() {
		String str = simpleLoginBean.getLoggedInStatus() + " / "
				+ simpleLoginBean.getLoginname();
		userLogged = simpleLoginBean.getUserLogged();
		System.out.println("chatBean: " + str);
		apikey = simpleLoginBean.getApiKey();
	}

	/*
	 * Support creating and retrieving Message entities
	 */

	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Message message;
	private Message newMessage;
	private User userToReply;

	public User getUserToReply() {
		return userToReply;
	}

	public void setUserToReply(User userToReply) {
		this.userToReply = userToReply;
	}

	public Message getMessage() {
		return this.message;
	}

	public Message getNewMessage() {
		return this.newMessage;
	}

	@Inject
	private Conversation conversation;

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
			this.message = this.example;
		} else {
			this.newMessage = this.example;
			newMessage.setMine(true);
			this.message = findById(getId());
		}
	}

	public Message findById(Integer id) {
		Message msgResul;
		msgResul = this.entityManager.find(Message.class, id);
		msgResul.setUserToReply(findUserToReply(msgResul, userLogged));
		return msgResul;
	}

	/*
	 * Support updating and deleting Message entities
	 */

	public String update() {
		String str =newMessage.getMessage();
		System.out.println("Str:"+str);
		this.conversation.end();

		try {
			if (this.id == null) {
				// this.entityManager.persist(this.message);
				return "search?faces-redirect=true";
			} else {
				// this.entityManager.merge(this.newMessage);
				newMessage.setCachuelo(message.getCachuelo());
				newMessage.setUserByIdReceiver(findUserToReply(message,
						userLogged));
				newMessage.setUserByIdUserSender(userLogged);
				this.entityManager.persist(this.newMessage);
				sendMessageToPhone(newMessage.getUserByIdReceiver(),
						newMessage.getUserByIdUserSender(),
						newMessage.getMessage(), newMessage.getCachuelo()
								.getIdCachuelo().toString());
				simpleLoginBean.getAllMyMessages().add(newMessage);
				return "view?faces-redirect=true&id="
						+ this.message.getIdMessage();
				// return "";
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {
		this.conversation.end();

		try {
			Message deletableEntity = findById(getId());
			User userByIdReceiver = deletableEntity.getUserByIdReceiver();
			userByIdReceiver.getMessagesForIdReceiver().remove(deletableEntity);
			deletableEntity.setUserByIdReceiver(null);
			this.entityManager.merge(userByIdReceiver);
			User userByIdUserSender = deletableEntity.getUserByIdUserSender();
			userByIdUserSender.getMessagesForIdUserSender().remove(
					deletableEntity);
			deletableEntity.setUserByIdUserSender(null);
			this.entityManager.merge(userByIdUserSender);
			Cachuelo cachuelo = deletableEntity.getCachuelo();
			cachuelo.getMessages().remove(deletableEntity);
			deletableEntity.setCachuelo(null);
			this.entityManager.merge(cachuelo);
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
	 * Support searching Message entities with pagination
	 */

	private int page;
	private long count;
	private List<Message> pageItems;
	private List<Message> pageItemsMessages = new ArrayList<Message>();

	private Message example = new Message();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 5;
	}

	public Message getExample() {
		return this.example;
	}

	public void setExample(Message example) {
		this.example = example;
	}

	public void search() {
		this.page = 0;
	}

	public void paginate() {
		List<Message> filteredArray = filterArray(simpleLoginBean
				.getMyChatList());
		this.count = filteredArray.size();
		this.pageItems = getPageResults(filteredArray);
	}
	
	private List<Message> fullScanAllMessages(String messageParam) {
		List<Message> filterResul = new ArrayList<Message>();
		List<Message> myChatListAux = simpleLoginBean.getAllMyMessages();
		for (Message msg : myChatListAux) {
			if (like(msg.getMessage(), "%"+messageParam+"%") ){
				filterResul.add(msg);
			}
		}
		return filterResul;
	}
	
	public List<Message> filterFullScanAllMessages(List<Message> resultsPartial) {
		List<Message> filterResul = new ArrayList<Message>();
		
		for (Message message : resultsPartial) {
			if (message.getUserByIdReceiver().getIdUser() == userLogged.getIdUser()
				|| message.getUserByIdUserSender().getIdUser() == userLogged.getIdUser()) {
					message.setUserToReply(findUserToReply(message, userLogged));
					if (!isElementOnList(filterResul, message)) {
						filterResul.add(message);// mi lista de  chats
				}
			}
		}
		return filterResul;
	}
	
	private boolean isElementOnList(List<Message> myChatList, Message message) {
		boolean result=false;
		for (Message messageItem : myChatList) {
			User userToReplyFromChat=findUserToReply(messageItem, userLogged);
			User userToReplyFromMessage=findUserToReply(message, userLogged);
			if (message.getCachuelo().getIdCachuelo()==messageItem.getCachuelo().getIdCachuelo()&&
				userToReplyFromChat.getIdUser()==userToReplyFromMessage.getIdUser()) {
				return true;
			}
		}
		return result;
	}

	private List<Message> filterArray(List<Message> myChatList) {
		List<Message> filterResul = new ArrayList<Message>();
		List<Message> myChatListAux = new ArrayList<Message>();
		User userToReply = this.example.getUserToReply();
		Cachuelo cachuelo = this.example.getCachuelo();
		String message = this.example.getMessage();
		
		if (userToReply==null&&cachuelo==null&&(message==null||"".equals(message))){
			return myChatList;//filtros vacios
		}
		
		if (!"".equals(message)) {
			myChatListAux=filterFullScanAllMessages(fullScanAllMessages(message));
			if (userToReply==null&&cachuelo==null){
				return myChatListAux;
			}
		}else{
			myChatListAux=myChatList;
		}
		
		
		for (Message msg : myChatListAux) {
			if (userToReply != null) {
				if (userToReply.getIdUser()==findUserToReply(msg, userLogged).getIdUser()){
					if (cachuelo != null) {
						if (cachuelo.getIdCachuelo()==msg.getCachuelo().getIdCachuelo()){
							filterResul.add(msg); //cachuelo y user coincidente
						}
					}else{
						filterResul.add(msg);//cachuelo null y user coincidente
					}					
				}
			}else{
				if (cachuelo != null) {
					if (cachuelo.getIdCachuelo()==msg.getCachuelo().getIdCachuelo()){
						filterResul.add(msg);//user null y cachuelo coincidente
					}
				}
			}

		}
		return filterResul;
	}



	private List<Message> getPageResults(List<Message> myChatList) {
		List<Message> pageResul = new ArrayList<Message>();
		int initialPosition = 0;
		int finalPosition = 0;
		initialPosition = this.page * getPageSize();
		finalPosition = initialPosition + getPageSize();
		for (int i = initialPosition; i < finalPosition; i++) {
			if (i < myChatList.size()) {
				pageResul.add(myChatList.get(i));
			}
		}
		return pageResul;
	}
	
	public static boolean like(String str, String expr) {
	    expr = expr.toLowerCase(); // ignoring locale for now
	    expr = expr.replace(".", "\\."); // "\\" is escaped to "\" (thanks, Alan M)
	    // ... escape any other potentially problematic characters here
	    expr = expr.replace("?", ".");
	    expr = expr.replace("%", ".*");
	    str = str.toLowerCase();
	    return str.matches(expr);
	}

	public List<Message> getPageItems() {
		return this.pageItems;
	}

	public List<Message> getPageItemsMessages() {
		return this.pageItemsMessages;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Message entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Message> getAll() {

		CriteriaQuery<Message> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Message.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Message.class))).getResultList();
	}

	@Resource
	private SessionContext sessionContext;

	public Converter getConverter() {

		final FrontMessageBean ejbProxy = this.sessionContext
				.getBusinessObject(FrontMessageBean.class);

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

				return String.valueOf(((Message) value).getIdMessage());
			}
		};
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Message add = new Message();

	public Message getAdd() {
		return this.add;
	}

	public Message getAdded() {
		Message added = this.add;
		this.add = new Message();
		return added;
	}

	public List<Message> listMyMessagesForChat() {
//		if (simpleLoginBean.newMessageArrived()) {
//			simpleLoginBean.listMyChats();
//		}
		this.pageItemsMessages.clear();
		System.out.println("Loading Messages from memory");
		for (Message msg : simpleLoginBean.getAllMyMessages()) {
			User userToReplyFromChat = findUserToReply(this.message, userLogged);
			User userToReplyFromMessage = findUserToReply(msg, userLogged);
			if (msg.getCachuelo().getIdCachuelo() == this.message.getCachuelo()
					.getIdCachuelo()
					&& userToReplyFromChat.getIdUser() == userToReplyFromMessage
							.getIdUser()) {
				if (msg.getUserByIdReceiver().getIdUser() == userLogged
						.getIdUser()) {
					msg.setMine(true);
				} else {
					msg.setMine(false);
				}
				this.pageItemsMessages.add(msg);
			}
		}
		return this.pageItemsMessages;
	}

	private User findUserToReply(Message message, User userLogged) {
		User userResul;
		if (message.getUserByIdReceiver().getIdUser() == userLogged.getIdUser()) {
			userResul = message.getUserByIdUserSender();
		} else {
			userResul = message.getUserByIdReceiver();
		}
		return userResul;
	}

	private int sendMessageToPhone(User userReceiver, User userSender,
			String message, String idCachuelo) {
		if (userReceiver.getGcmid()==null){
			System.out.println("GCMID is NULL");
			return 2;			
		}
		else{
			if (userReceiver.getGcmid().compareTo("")==0){
				System.out.println("GCMID is empty");
				return 3;
			}
		}
		String strJson = Utility.constructJSON(Constants.TAG_SEND_MESSAGE,
				message, idCachuelo, userSender.getIdUser().toString(),
				userSender);		
		List<String> partialDevices = new ArrayList<String>();
		partialDevices.add(userReceiver.getGcmid());
		GCMSenderJson senderJson = new GCMSenderJson();
		senderJson.sendMessage(partialDevices, strJson, apikey);
		System.out.println("Sending Message out " + strJson);
		return 1;
	}
}