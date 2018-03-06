package com.nascentech.Locator.actions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FCMNotification {
	private static final String FCM_API_KEY = "YOUR_API_KEY";
	private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

	@SuppressWarnings("unchecked")
	public static void push(String tokenId, String title, String body) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(FCM_URL);
		post.setHeader("Content-type", "application/json");
		post.setHeader("Authorization", "key=" + FCM_API_KEY);

		JSONArray jsonArray = new JSONArray();
		jsonArray.add(tokenId);

		JSONObject message = new JSONObject();
		message.put("registration_ids", jsonArray);
		message.put("priority", "high");

		JSONObject data = new JSONObject();
		String[] msgArray = body.split(",");
		if (msgArray.length == 3 && msgArray[0].equals("url")) {
			String latitude = msgArray[1];
			String longitude = msgArray[2];
			String url = "https://www.google.com/maps/?q=" + latitude + "," + longitude;
			String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + "," + longitude
					+ "&zoom=18&size=640x640&markers=color:red%7C" + latitude + "," + longitude
					+ "&key=AIzaSyCqA9_bAgR5PcXZIiiTZcuGAo514UKUMD4";
			data.put("title", title);// appears in heading
			data.put("message", "location_update");
			data.put("image", imageUrl);
			data.put("action", "url");
			data.put("action_destination", url);
		} else {
			data.put("title", title);// appears in heading
			data.put("message", body);
			data.put("action", "text");
			data.put("action_destination", "");
		}

		message.put("data", data);

		post.setEntity(new StringEntity(message.toString(), "UTF-8"));
		HttpResponse response = client.execute(post);
		System.out.println(response);
		System.out.println(message);
	}
}
