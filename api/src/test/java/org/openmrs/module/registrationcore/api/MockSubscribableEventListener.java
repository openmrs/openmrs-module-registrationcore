/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api;

import java.util.ArrayList;
import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;

import junit.framework.Assert;

import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.event.Event;
import org.openmrs.event.MockEventListener;
import org.openmrs.event.SubscribableEventListener;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

/**
 * Subclass of {@link MockEventListener} that extracts the patient registration pay load for testing
 * purposes
 */
public class MockSubscribableEventListener extends MockEventListener implements SubscribableEventListener {
	
	public MockSubscribableEventListener(int expectedEventsCount) {
		super(expectedEventsCount);
	}
	
	public List<Class<? extends OpenmrsObject>> subscribeToObjects() {
		List objects = new ArrayList<Class<? extends OpenmrsObject>>();
		objects.add(Patient.class);
		return objects;
	}
	
	/**
	 * @return a list of Actions this listener can deal with
	 */
	public List<String> subscribeToActions() {
		List actions = new ArrayList<String>();
		actions.add(Event.Action.CREATED.name());
		actions.add(Event.Action.UPDATED.name());
		return actions;
	}
	
}
