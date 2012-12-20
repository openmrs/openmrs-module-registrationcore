package org.openmrs.module.registrationcore.api;

import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;

import junit.framework.Assert;

import org.openmrs.event.MockEventListener;

/**
 *
 *
 */
public class MockRegistrationEventListener extends MockEventListener {
	
	private String patientUuid;
	
	private List<String> relationshipUuids;
	
	private String registererUuid;
	
	private String dateRegistered;
	
	private Boolean wasAPerson = Boolean.FALSE;
	
	MockRegistrationEventListener(int expectedEventsCount) {
		super(expectedEventsCount);
	}
	
	/**
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(Message message) {
		super.onMessage(message);
		MapMessage mapMessage = (MapMessage) message;
		try {
			patientUuid = mapMessage.getString("patientUuid");
			relationshipUuids = (List<String>) mapMessage.getObject("relationshipUuids");
			registererUuid = mapMessage.getString("registererUuid");
			dateRegistered = mapMessage.getString("dateRegistered");
			wasAPerson = mapMessage.getBoolean("wasAPerson");
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * @return the patientUuid
	 */
	public String getPatientUuid() {
		return patientUuid;
	}
	
	/**
	 * @return the relationshipUuids
	 */
	public List<String> getRelationshipUuids() {
		return relationshipUuids;
	}
	
	/**
	 * @return the registererUuid
	 */
	public String getRegistererUuid() {
		return registererUuid;
	}
	
	/**
	 * @return the dateRegistered
	 */
	public String getDateRegistered() {
		return dateRegistered;
	}
	
	/**
	 * @return the wasAPerson
	 */
	public Boolean getWasAPerson() {
		return wasAPerson;
	}
	
}
