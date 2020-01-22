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

    private Message sendMessage(Message request, String endpoint, int port)
            throws HL7Exception, LLPException, IOException {
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
        } finally {
            if (connection != null)
                hub.discard(connection);
        }
    }
}
