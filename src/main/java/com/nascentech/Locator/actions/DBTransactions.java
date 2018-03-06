package com.nascentech.Locator.actions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import com.nascentech.Locator.model.RegisteredContacts;
import com.nascentech.Locator.model.RegisteredUsers;
import com.nascentech.Locator.model.UserContacts;
import com.nascentech.Locator.singletonfactory.HibernateUtil;
import com.nascentech.Locator.utils.PhoneNumberUtils;

public class DBTransactions {
	public static void addUser(String name, String phoneNumber, String gender, String deviceId, String tokenId) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			RegisteredUsers registeredUsers = getUserFromDeviceId(deviceId);
			if (registeredUsers == null) {
				registeredUsers = new RegisteredUsers();
				registeredUsers.setName(name);
				registeredUsers.setPhoneNumber(phoneNumber);
				registeredUsers.setGender(gender);
				registeredUsers.setDeviceId(deviceId);
				registeredUsers.setTokenId(tokenId);
				registeredUsers
						.setLastSeen(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()));
				session.save(registeredUsers);
			} else {
				registeredUsers.setName(name);
				registeredUsers.setPhoneNumber(phoneNumber);
				registeredUsers.setGender(gender);
				registeredUsers.setDeviceId(deviceId);
				registeredUsers.setTokenId(tokenId);
				registeredUsers.setLastSeen(new Date().toString());
				session.update(registeredUsers);
			}

			session.getTransaction().commit();
		}
	}

	public static RegisteredUsers getUserFromDeviceId(String deviceId) {
		RegisteredUsers user = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DetachedCriteria criteria = DetachedCriteria.forClass(RegisteredUsers.class);
			criteria.add(Restrictions.eq("deviceId", deviceId));
			@SuppressWarnings("unchecked")
			List<RegisteredUsers> user_list = criteria.getExecutableCriteria(session).list();
			if (user_list.size() > 0) {
				user = user_list.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	public static RegisteredUsers getUserFromPhoneNumber(String phoneNumber) {
		RegisteredUsers user = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DetachedCriteria criteria = DetachedCriteria.forClass(RegisteredUsers.class);
			@SuppressWarnings("unchecked")
			List<RegisteredUsers> user_list = criteria.getExecutableCriteria(session).list();
			if (user_list.size() > 0) {
				for (RegisteredUsers users : user_list) {
					if (PhoneNumberUtils.compare(users.getPhoneNumber(), phoneNumber)) {
						user = users;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	public static String getTokenFromPhoneNumber(String phoneNumber) {
		String token = "";
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DetachedCriteria criteria = DetachedCriteria.forClass(RegisteredUsers.class);
			@SuppressWarnings("unchecked")
			List<RegisteredUsers> userList = criteria.getExecutableCriteria(session).list();

			if (userList.size() > 0) {
				for (RegisteredUsers user : userList) {
					if (PhoneNumberUtils.compare(user.getPhoneNumber(), phoneNumber)) {
						token = user.getTokenId();
						break;
					}
				}
			}
		} catch (Exception e) {

		}

		return token;
	}

	public static void updateLastSeen(String lastSeen, String deviceId) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			RegisteredUsers users = getUserFromDeviceId(deviceId);
			if (users != null) {
				users.setLastSeen(lastSeen);
				session.update(users);
				session.getTransaction().commit();
			}
		}
	}

	public static List<RegisteredContacts> getRegisteredContacts(List<UserContacts> userContactList) {
		List<RegisteredContacts> registeredContactsList = new ArrayList<>();
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			for (UserContacts contacts : userContactList) {
				RegisteredUsers users = getUserFromPhoneNumber(contacts.getContact_number());
				if (users != null) {
					RegisteredContacts registeredContacts = new RegisteredContacts();
					registeredContacts.setName(users.getName());
					registeredContacts.setPhoneNumber(users.getPhoneNumber());
					registeredContacts.setLastSeen(users.getLastSeen());
					registeredContacts.setId(users.getId());
					registeredContacts.setGender(users.getGender());
					registeredContactsList.add(registeredContacts);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return registeredContactsList;
	}
}
