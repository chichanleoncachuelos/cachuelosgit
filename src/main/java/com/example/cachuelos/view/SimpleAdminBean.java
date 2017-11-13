package com.example.cachuelos.view;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.event.map.MarkerDragEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

@Named
@SessionScoped
public class SimpleAdminBean implements Serializable {

	private static final long serialVersionUID = 1L;

	String loggedInStatus = "false";

	// Timer timer;

	private Marker marker;
	private MapModel draggableModel;

	private Double lat = 0.0;
	private Double lng = 0.0;
	private Double latTmp = 0.0;// latitud y longitud del api de google (por ip y redes)
	private Double lngTmp = 0.0;

	public Double getLngTmp() {
		return lngTmp;
	}

	public void setLngTmp(Double lngTmp) {
		this.lngTmp = lngTmp;
	}

	public Double getLatTmp() {
		return latTmp;
	}

	public void setLatTmp(Double latTmp) {
		this.latTmp = latTmp;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public MapModel getDraggableModel() {
		return draggableModel;
	}

	public void setDraggableModel(MapModel draggableModel) {
		this.draggableModel = draggableModel;
	}

	@PostConstruct
	private void init() {
		System.out.println("constructor simple admin bean");
		draggableModel = new DefaultMapModel();
		LatLng coord1 = new LatLng(12.071886098955085, 77.08684869110584);
		draggableModel.addOverlay(new Marker(coord1, "Nuevo"));
		for (Marker premarker : draggableModel.getMarkers()) {
			premarker.setDraggable(true);
		}
	}

	public String getLoggedInStatus() {
		return loggedInStatus;
	}

	public void setLoggedInStatus(String loggedInStatus) {
		this.loggedInStatus = loggedInStatus;
	}

	public String logOut() {
		loggedInStatus = "false";
		return "success";
	}

	public void onMarkerDrag(MarkerDragEvent event) {
		marker = event.getMarker();
		System.out.println("inside dragging: lat:" + marker.getLatlng().getLat()
				+ " lng:" + marker.getLatlng().getLng());
		lat = marker.getLatlng().getLat();
		lng = marker.getLatlng().getLng();
	}

	public void addMarker(Double latParam,Double lngParam) {
		System.out.println("Adding Marker: lat:" + latTmp + " lng:" + lngTmp);
		System.out.println("Adng Mrk params: lat:" + latParam + " lng:" + lngParam);		
		for (Marker premarker : draggableModel.getMarkers()) {
			premarker.setLatlng(new LatLng(latParam, lngParam));
			premarker.setDraggable(true);
		}
		lat = latParam;
		lng = lngParam;
	}

}