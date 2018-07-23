/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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

	public List<Class<? extends OpenmrsObject>> subscribeToObjects(){
		List objects = new ArrayList<Class<? extends OpenmrsObject>>();
		objects.add(Patient.class);
		return objects;
	}

	/**
	 * @return a list of Actions this listener can deal with
	 */
	public List<String> subscribeToActions(){
		List actions = new ArrayList<String>();
		actions.add(Event.Action.CREATED.name());
		actions.add(Event.Action.UPDATED.name());
		return actions;
	}

}
