package com.example.cachuelos.utils;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.cachuelosfrontend.model.Cachuelo;
import com.example.cachuelosfrontend.model.Location;
import com.example.cachuelosfrontend.model.Offer;
import com.example.cachuelosfrontend.model.User;

public class Utility {
	/**
	 * Null check Method
	 * 
	 * @param txt
	 * @return
	 */
	public static boolean isNotNull(String txt) {
		// System.out.println("Inside isNotNull");
		return txt != null && txt.trim().length() >= 0 ? true : false;
	}

	/**
	 * Method to construct JSON
	 * 
	 * @param tag
	 * @param status
	 * @return
	 */
	public static String constructJSON(String tag, boolean status) {// success
		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("status", new Boolean(status));
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	public static String constructJSON(String tag, boolean status,
			String err_msg) {// error
		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("status", new Boolean(status));
			obj.put("error_msg", err_msg);
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	public static String constructJSON(String tag, boolean status,
			Cachuelo cachuelo, String idCachuelo) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("status", new Boolean(status));
			obj.put("idCachuelo", idCachuelo);
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	/**
	 * Method to construct JSON with Error Msg
	 * 
	 * @param tag
	 * @param status
	 * @param err_msg
	 * @return
	 */

	public static String constructJSON(String tag, boolean status, User usr) {
		JSONObject obj = new JSONObject();
		User newUser;
		if (!(usr instanceof User)) {
			newUser = null;
		} else {
			newUser = (User) usr;
		}
		try {
			if (newUser != null) {
				obj.put("tag", tag);
				obj.put("status", new Boolean(status));
				JSONObject objtmp = new JSONObject();
				objtmp.put("idUser", newUser.getIdUser());
				objtmp.put("isworker", newUser.getIsworker());
				objtmp.put("name",
						newUser.getName() + " " + newUser.getLastName());
				objtmp.put("email", newUser.getEmail());
				if (newUser.getPictureFull()!=null){
					objtmp.put("pictureFull", newUser.getPictureFull());
				}else{
					objtmp.put("pictureFull", "");
				}
				if (newUser.getPictureThumb()!=null){
					objtmp.put("pictureThumb", newUser.getPictureThumb());
				}else{
					objtmp.put("pictureThumb", "");
				}								
				obj.put("user", objtmp);
			}
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	public static String constructJSON(String tag, boolean status,
			String imageName, String folderName) {
		JSONObject obj = new JSONObject();
		try {
			if (imageName != null) {
				obj.put("tag", tag);
				obj.put("status", new Boolean(status));
				obj.put("imageName", imageName);
				obj.put("folderName", folderName);

			}
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

//	public static String constructJSON(String tag, boolean status,
//			List<Location> locations) {
//		JSONObject obj = new JSONObject();
//		try {
//			obj.put("tag", tag);
//			obj.put("status", new Boolean(status));
//			JSONArray jsonArr = new JSONArray();
//			for (Location loc : locations) {
//				JSONObject locJson = new JSONObject();
//				locJson.put("idLocation", loc.getIdLocation());
//				locJson.put("lat", loc.getLat());
//				locJson.put("lng", loc.getLng());
//				locJson.put("name", loc.getName());
//				jsonArr.put(locJson);
//			}
//			obj.put("list", jsonArr);
//		} catch (JSONException e) {
//			return Constants.JSON_PARSER_ERROR;
//		}
//		return obj.toString();
//	}

//	public static String constructJSONType(String tag, boolean status,
//			List<Typecachuelo> types) {
//		JSONObject obj = new JSONObject();
//		try {
//			obj.put("tag", tag);
//			obj.put("status", new Boolean(status));
//			JSONArray jsonArr = new JSONArray();
//			for (Typecachuelo loc : types) {
//				JSONObject locJson = new JSONObject();
//				locJson.put("idTypeCachuelo", loc.getIdTypeCachuelo());
//				locJson.put("name", loc.getName());
//				jsonArr.put(locJson);
//			}
//			obj.put("list", jsonArr);
//		} catch (JSONException e) {
//			return Constants.JSON_PARSER_ERROR;
//		}
//		return obj.toString();
//	}

	public static String constructJSON(String tag, boolean status, Location loc) {
		JSONObject obj = new JSONObject();
		Location newLocation;
		if (!(loc instanceof Location)) {
			newLocation = null;
		} else {
			newLocation = (Location) loc;
		}
		try {
			if (newLocation != null) {
				obj.put("tag", tag);
				obj.put("status", new Boolean(status));
				obj.put("idLocation", loc.getIdLocation());
			}
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	public static String constructJSON(String tag, boolean status, Offer offer) {
		JSONObject obj = new JSONObject();
		Offer newOffer;
		if (!(offer instanceof Offer)) {
			newOffer = null;
		} else {
			newOffer = (Offer) offer;
		}
		try {
			if (newOffer != null) {
				obj.put("tag", tag);
				obj.put("status", new Boolean(status));
				obj.put("idOffer", offer.getIdOffer());
			}
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}
	
	public static String constructJSON(String tag, boolean status, int idComm) {
		JSONObject obj = new JSONObject();
		try {
				obj.put("tag", tag);
				obj.put("status", new Boolean(status));
				obj.put("idCommentRating", idComm);
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	public static String constructJSON(String tag, String message,
			String idCachuelo, String idUserSender, User user) {// para el chat
		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("message", message);
			obj.put("idCachuelo", idCachuelo);
			obj.put("idUserSender", idUserSender);
			obj.put("userName", user.getName() + " " + user.getLastName());
			if (user.getPictureThumb()!=null){
				obj.put("pictureThumb", user.getPictureThumb());
			}else{
				obj.put("pictureThumb", "");
			}
			
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	public static String constructJSON(String tag, String idUser) {// para el GCM
																	// chat
		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("idUser", idUser);
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	public static String constructJSON(String tag, String idCachuelo,
			User user, String cachueloName, Offer offer) {// para el GCM de la oferta
		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("price", offer.getPrice());
			obj.put("idCachuelo", idCachuelo);
			obj.put("idUserSender", user.getIdUser());
			obj.put("idOffer", offer.getIdOffer());
			obj.put("userSenderName", user.getName() + " " + user.getLastName());
			if (user.getPictureThumb()!=null){
				obj.put("pictureThumb", user.getPictureThumb());
			}else{
				obj.put("pictureThumb", "");
			}			
			obj.put("cachueloName", cachueloName);
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	public static String constructJSON(String tag, User userSender,
			Cachuelo cachuelo) {// pra el GCM de la notif masiva del cachuelo

		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("idCachuelo", cachuelo.getIdCachuelo());
			obj.put("userSenderName", userSender.getName()+ " " + userSender.getLastName());
			if (userSender.getPictureThumb()!=null){
				obj.put("pictureThumb", userSender.getPictureThumb());
			}else{
				obj.put("pictureThumb", "");
			}
			obj.put("cachueloName", cachuelo.getName());
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}
	
	public static String constructJSON(String tag, User userSender,
			Cachuelo cachuelo, String idCommentType) {// pra el GCM de la notif masiva del cachuelo

		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("idCachuelo", cachuelo.getIdCachuelo());
			obj.put("userSenderName", userSender.getName()+ " " + userSender.getLastName());
			if (userSender.getPictureThumb()!=null){
				obj.put("pictureThumb", userSender.getPictureThumb());
			}else{
				obj.put("pictureThumb", "");
			}
			obj.put("cachueloName", cachuelo.getName());
			obj.put("idCommentType", idCommentType);
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}

	public static String constructJSON(final List<String> partialDevices, final String msg) {//message para nueva api GCM
		JSONObject obj = new JSONObject();
		JSONObject objData = new JSONObject();		
		try {
			objData.put("message", msg);
			JSONArray jsonArr = new JSONArray(partialDevices);
			obj.put("registration_ids", jsonArr);
			obj.put("priority", "high");			
			obj.put("data", objData);
			obj.put("time_to_live", 300);
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		String resul=obj.toString();
		return resul;

	}
	
	public static String constructJSON(String tag) {// para el GCM heartbeat
		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("message", "");
//			obj.put("idCachuelo", idCachuelo);	
		} catch (JSONException e) {
			return Constants.JSON_PARSER_ERROR;
		}
		return obj.toString();
	}
}
