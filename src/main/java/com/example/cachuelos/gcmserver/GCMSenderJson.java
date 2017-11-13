package com.example.cachuelos.gcmserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.example.cachuelos.utils.Utility;


public class GCMSenderJson {
	
	private static final Executor threadPool = Executors.newFixedThreadPool(5);
	private String ret="";

	public void sendMessage(List<String> partialDevices, String msg, String apiKey) {//"AIzaSyC8bUOiV-x8ne2UbMkSaFLQZp-kOwcZTZk"
		final List<String> devices = new ArrayList<String>(partialDevices);
		final String strMsg= new String(msg);
		final String strapiKey= new String(apiKey);
		threadPool.execute(new Runnable() {
			public void run() {

				HttpClient client = HttpClientBuilder.create().build();
				// "  \"registration_ids\": [\"4\", \"8\", \"15\", \"16\", \"23\", \"42\"]"

				HttpPost httpost = new HttpPost(
						"https://android.googleapis.com/gcm/send");
				httpost.setHeader("Content-Type", "application/json");
				httpost.setHeader("Authorization", "key="
						+ strapiKey);

				HttpResponse response = null;

//				String json2 = "{\"data\":{"
//						+ "\"message\":\"Hola Cachuelos\""
//						+ "},"
//						+ "\"registration_ids\":[\""
//						+ "ejU4EhN7B_w:APA91bGqnn_Ho33OPH9gYZ5UXrg6_NtUIH3S7C0F9vKeSdds9CQ5NAMds-OeJ09eOnVur2CLY4d0udrM9ajxYPBieG8MFl36845z8p1f3_r3OASBu993VSsvNLfEpezy9HiRZjX8RFV_"
//						+ "\"]" + "}";
				String json = Utility.constructJSON(
						devices, strMsg);
				try {
					httpost.setEntity(new StringEntity(json));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					response = client.execute(httpost);if (response != null) {
						if (response.getStatusLine() != null) {
							if (response.getStatusLine().getStatusCode() == 200) {
								ret = EntityUtils.toString(response.getEntity());
							}
						} else
							ret = EntityUtils.toString(response.getEntity());
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("GCM Sender response" + response.toString());
				System.out.println("GCM Sender return" + ret);
			}
		});
//		return ret;// siempre retorna nulo por los hilos
	}
	
}
