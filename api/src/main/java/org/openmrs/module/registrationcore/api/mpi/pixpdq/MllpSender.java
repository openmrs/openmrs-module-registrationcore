/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class MllpSender implements Hl7v2Sender {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private RegistrationCoreProperties config;
	
	@Override
	public Message sendPdqMessage(Message request) throws LLPException, IOException, HL7Exception {
		String endpoint = config.getPdqEndpoint();
		int port = config.getPdqPort();
		return sendMessage(request, endpoint, port);
	}
	
	@Override
	public Message sendPixMessage(Message request) throws LLPException, IOException, HL7Exception {
		String endpoint = config.getPixEndpoint();
		int port = config.getPixPort();
		return sendMessage(request, endpoint, port);
	}
	
	private Message sendMessage(Message request, String endpoint, int port) throws HL7Exception, LLPException, IOException {
		PipeParser parser = new PipeParser();
		ConnectionHub hub = ConnectionHub.getInstance();
		Connection connection = null;
		
		try {
			if (log.isDebugEnabled())
				log.debug(String.format("Sending to %s:%s : %s", endpoint, port, parser.encode(request)));
			
			connection = hub.attach(endpoint, port, parser, MinLowerLayerProtocol.class);
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(request);
			
			if (log.isDebugEnabled())
				log.debug(String.format("Response from %s:%s : %s", endpoint, port, parser.encode(response)));
			
			return response;
		}
		finally {
			if (connection != null)
				hub.discard(connection);
		}
	}
}
