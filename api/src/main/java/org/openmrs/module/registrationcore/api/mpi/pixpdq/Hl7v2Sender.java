package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;

import java.io.IOException;

public interface Hl7v2Sender {
	
	Message sendPdqMessage(Message request) throws LLPException, IOException, HL7Exception;
	
	Message sendPixMessage(Message request) throws LLPException, IOException, HL7Exception;
}
