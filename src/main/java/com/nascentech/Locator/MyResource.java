package com.nascentech.Locator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nascentech.Locator.actions.DBTransactions;
import com.nascentech.Locator.actions.FCMNotification;
import com.nascentech.Locator.model.RegisteredContacts;
import com.nascentech.Locator.model.RegisteredUsers;
import com.nascentech.Locator.model.UserContacts;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("api")
public class MyResource {
	@POST
	@Path("register")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String register(@FormParam("name") String name, @FormParam("phoneNumber") String phoneNumber,
			@FormParam("country") String country, @FormParam("gender") String gender,
			@FormParam("deviceId") String deviceId, @FormParam("tokenId") String tokenId) {
		System.out.println("Name: " + name);
		System.out.println("Phone number: " + phoneNumber);
		System.out.println("Country: " + country);
		System.out.println("Gender: " + gender);
		System.out.println("Device ID: " + deviceId);
		System.out.println("Token ID: " + tokenId);
		DBTransactions.addUser(name, phoneNumber, gender, deviceId, tokenId);
		return "success";
	}

	@POST
	@Path("updateLastSeen")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String updateLastSeen(@FormParam("lastSeen") String lastSeen, @FormParam("deviceId") String deviceId) {
		System.out.println(lastSeen);
		DBTransactions.updateLastSeen(lastSeen, deviceId);
		return "success";
	}

	@POST
	@Path("getUserState")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserState(@FormParam("phoneNumber") String phoneNumber) {
		String userState = "";
		System.out.println(phoneNumber);
		RegisteredUsers user = DBTransactions.getUserFromPhoneNumber(phoneNumber);
		if (user != null) {
			userState = user.getLastSeen();
		}
		return userState;
	}

	@GET
	@Path("checkServer")
	public Response check() {
		return Response.ok("Server OK HTTP/200", MediaType.TEXT_PLAIN).build();
	}

	@POST
	@Path("send")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String sendMessage(@FormParam("message") String message, @FormParam("phoneNumber") String phoneNumber,
			@FormParam("deviceId") String deviceId) {
		System.out.println(message);
		System.out.println(phoneNumber);
		try {
			String token = DBTransactions.getTokenFromPhoneNumber(phoneNumber);
			if (!token.isEmpty()) {
				FCMNotification.push(token, DBTransactions.getUserFromDeviceId(deviceId).getPhoneNumber(), message);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "success";
	}

	@POST
	@Path("registeredContacts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String register(String list, @Context HttpHeaders httpHeaders) {
		String key = httpHeaders.getRequestHeader("api-key").get(0);
		System.out.println(key);
		System.out.println(list);
		Gson gson = new Gson();
		Type listType = new TypeToken<List<UserContacts>>() {
		}.getType();
		List<UserContacts> userContacts = gson.fromJson(list, listType);
		System.out.println(userContacts.size());
		List<RegisteredContacts> registeredContacts = DBTransactions.getRegisteredContacts(userContacts);
		String element = gson.toJson(registeredContacts, new TypeToken<ArrayList<RegisteredContacts>>() {
		}.getType());
		System.out.println("Element: " + element);
		return element;
	}
}
