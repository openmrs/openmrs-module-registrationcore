package org.openmrs.module.registrationcore.util;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.QBP_Q21;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ImplementationId;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class PDQMessageUtil {

    private final Log log = LogFactory.getLog(this.getClass());

    private final static Object s_lockObject = new Object();

    private static PDQMessageUtil s_instance = null;

    public static PDQMessageUtil getInstance() {
        if(s_instance == null)
            synchronized (s_lockObject) {
                if(s_instance == null)
                    s_instance = new PDQMessageUtil();
            }
        return s_instance;
    }


    public Message createPdqMessage(Map<String, String> queryParameters) throws HL7Exception
    {
        QBP_Q21 message = new QBP_Q21();
        updateMSH(message.getMSH(), "QBP", "Q22");
        // What do these statements do?
        Terser terser = new Terser(message);

        // Set the query parmaeters
        int qpdRep = 0;
        for(Map.Entry<String, String> entry : queryParameters.entrySet())
        {
            terser.set(String.format("/QPD-3(%d)-1", qpdRep), entry.getKey());
            terser.set(String.format("/QPD-3(%d)-2", qpdRep++), entry.getValue());
        }

        terser.set("/QPD-1-1", "Q22");
        terser.set("/QPD-1-2", "Find Candidates");
        terser.set("/QPD-1-3", "HL7");
        terser.set("/QPD-2-1", UUID.randomUUID().toString());

        return message;
    }

    public void updateMSH(MSH msh, String messageCode, String triggerEvent) throws DataTypeException {
        msh.getFieldSeparator().setValue("|");
        msh.getEncodingCharacters().setValue("^~\\&");
        msh.getAcceptAcknowledgmentType().setValue("AL"); // Always send response
        msh.getDateTimeOfMessage().getTime().setValue(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())); // DateTime of message
        msh.getMessageControlID().setValue(UUID.randomUUID().toString()); // Unique id for message
        msh.getMessageType().getMessageStructure().setValue(msh.getMessage().getName()); // Message Structure Type
        msh.getMessageType().getMessageCode().setValue(messageCode); // Message Structure Code
        msh.getMessageType().getTriggerEvent().setValue(triggerEvent); // Trigger Event
        msh.getProcessingID().getProcessingID().setValue("P"); // Production
        msh.getReceivingApplication().getNamespaceID().setValue("CR"); // Client Registry
        msh.getReceivingFacility().getNamespaceID().setValue("MOH_CAAT"); // Mohawk College of Applied Arts and Technology

        ImplementationId implementation = Context.getAdministrationService().getImplementationId();
        if(implementation != null)
            msh.getSendingApplication().getNamespaceID().setValue(implementation.getName()); // What goes here?
        else
            msh.getSendingApplication().getNamespaceID().setValue("UNNAMEDOPENMRS");

        Location defaultLocale = Context.getLocationService().getDefaultLocation();
        if(defaultLocale != null)
            msh.getSendingFacility().getNamespaceID().setValue(defaultLocale.getName()); // You're at the college... right?
        else
            msh.getSendingFacility().getNamespaceID().setValue("LOCATION");

        msh.getVersionID().getVersionID().setValue("2.5");
    }

    public Message sendMessage(Message request, String endpoint, int port) throws HL7Exception, LLPException, IOException
    {
        PipeParser parser = new PipeParser();
        ConnectionHub hub = ConnectionHub.getInstance();
        Connection connection = null;
        try
        {
            if(log.isDebugEnabled())
                log.debug(String.format("Sending to %s:%s : %s", endpoint, port, parser.encode(request)));

            connection = hub.attach(endpoint, port, parser, MinLowerLayerProtocol.class);
            Initiator initiator = connection.getInitiator();
            Message response = initiator.sendAndReceive(request);

            if(log.isDebugEnabled())
                log.debug(String.format("Response from %s:%s : %s", endpoint, port, parser.encode(response)));

            return response;
        }
        finally
        {
            if(connection != null)
                hub.discard(connection);
        }
    }

    /**
     * Get the PDQ endpoint
     * @return
     */
    public String getPdqEndpoint() {
        return Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_PDQ_ENDPOINT, "localhost");
    }

    /**
     * Get the PDQ port
     * @return
     */
    public Integer getPdqPort() {
        return Integer.valueOf(Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_PDQ_PORT, "8989"));
    }

}
