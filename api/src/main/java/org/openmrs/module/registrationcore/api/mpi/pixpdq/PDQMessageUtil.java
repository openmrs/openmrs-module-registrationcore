package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.XAD;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.message.QBP_Q21;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

        String implementation = Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_PDQ_SENDING_APPLICATION);
        if(implementation != null)
            msh.getSendingApplication().getNamespaceID().setValue(implementation);
        else
            msh.getSendingApplication().getNamespaceID().setValue("UNNAMEDOPENMRS");

        String defaultLocale = Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_PDQ_SENDING_LOCATION);
        if(defaultLocale != null)
            msh.getSendingFacility().getNamespaceID().setValue(defaultLocale);
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
     * Interpret PID segments
     * @param response
     * @return
     * @throws HL7Exception
     */
    public List<Patient> interpretPIDSegments(
            Message response) throws HL7Exception {
        List<Patient> retVal = new ArrayList<Patient>();

        Terser terser = new Terser(response);
        // Check for AA and OK in QAK
        if(terser.get("/MSA-1") != null &&
                terser.get("/MSA-1").equals("AE"))
            throw new HL7Exception("Server Error");
        else if(terser.get("/QAK-2") != null &&
                terser.get("/QAK-2").equals("NF"))
            return retVal;

        Location defaultLocation = Context.getLocationService().getDefaultLocation();

        for(Structure queryResponseStruct : response.getAll("QUERY_RESPONSE"))
        {
            Group queryResponseGroup = (Group)queryResponseStruct;
            for(Structure pidStruct : queryResponseGroup.getAll("PID"))
            {
                PID pid = (PID)pidStruct;
                Patient patient = new Patient();
                // Attempt to load a patient by identifier
                for(CX id : pid.getPatientIdentifierList())
                {

                    PatientIdentifierType pit = null;

                    if(id.getAssigningAuthority().getUniversalID().getValue() != null &&
                            !id.getAssigningAuthority().getUniversalID().getValue().isEmpty())
                    {
                        pit = Context.getPatientService().getPatientIdentifierTypeByName(id.getAssigningAuthority().getUniversalID().getValue());
                        if(pit == null)
                            pit = Context.getPatientService().getPatientIdentifierTypeByUuid(id.getAssigningAuthority().getUniversalID().getValue());
                        else if(!pit.getUuid().equals(id.getAssigningAuthority().getUniversalID().getValue())) // fix the UUID
                        {
                            log.debug(String.format("Updating %s to have UUID %s", pit.getName(), id.getAssigningAuthority().getUniversalID().getValue()));
                            pit.setUuid(id.getAssigningAuthority().getUniversalID().getValue());
                            Context.getPatientService().savePatientIdentifierType(pit);
                        }
                    }
                    if(pit == null && id.getAssigningAuthority().getNamespaceID().getValue() != null &&
                            !id.getAssigningAuthority().getNamespaceID().getValue().isEmpty())
                    {
                        pit = Context.getPatientService().getPatientIdentifierTypeByName(id.getAssigningAuthority().getNamespaceID().getValue());
                        if(pit != null && !pit.getUuid().equals(id.getAssigningAuthority().getUniversalID().getValue())) // fix the UUID
                        {
                            log.debug(String.format("Updating %s to have UUID %s", pit.getName(), id.getAssigningAuthority().getUniversalID().getValue()));
                            pit.setUuid(id.getAssigningAuthority().getUniversalID().getValue());
                            Context.getPatientService().savePatientIdentifierType(pit);
                        }

                    }
                    if(pit == null)
                        continue;

                    PatientIdentifier patId = new PatientIdentifier(
                            id.getIDNumber().getValue(),
                            pit,
                            defaultLocation
                    );

                    patient.addIdentifier(patId);
                }

                // Attempt to copy names
                for(XPN xpn : pid.getPatientName())
                {
                    PersonName pn = new PersonName();

                    if(xpn.getFamilyName().getSurname().getValue() == null || xpn.getFamilyName().getSurname().getValue().isEmpty())
                        pn.setFamilyName("(none)");
                    else
                        pn.setFamilyName(xpn.getFamilyName().getSurname().getValue());
                    pn.setFamilyName2(xpn.getFamilyName().getSurnameFromPartnerSpouse().getValue());

                    // Given name
                    if(xpn.getGivenName().getValue() == null || xpn.getGivenName().getValue().isEmpty())
                        pn.setGivenName("(none)");
                    else
                        pn.setGivenName(xpn.getGivenName().getValue());
                    pn.setMiddleName(xpn.getSecondAndFurtherGivenNamesOrInitialsThereof().getValue());
                    pn.setPrefix(xpn.getPrefixEgDR().getValue());

                    if("L".equals(xpn.getNameTypeCode().getValue()))
                        pn.setPreferred(true);

                    patient.addName(pn);
                }

                if(patient.getNames().size() == 0)
                    patient.addName(new PersonName("(none)", null, "(none)"));
                // Copy gender
                patient.setGender(pid.getAdministrativeSex().getValue());
                patient.setDead("Y".equals(pid.getPatientDeathIndicator().getValue()));


                // Addresses
                for(XAD xad : pid.getPatientAddress())
                {
                    PersonAddress pa = new PersonAddress();
                    pa.setAddress1(xad.getStreetAddress().getStreetOrMailingAddress().getValue());
                    pa.setAddress2(xad.getOtherDesignation().getValue());
                    pa.setCityVillage(xad.getCity().getValue());
                    pa.setCountry(xad.getCountry().getValue());
                    pa.setCountyDistrict(xad.getCountyParishCode().getValue());
                    pa.setPostalCode(xad.getZipOrPostalCode().getValue());
                    pa.setStateProvince(xad.getStateOrProvince().getValue());
                    if("H".equals(xad.getAddressType().getValue()))
                        pa.setPreferred(true);

                    patient.addAddress(pa);

                }

                // Mother's name
                XPN momsName = pid.getMotherSMaidenName(0);
                if(momsName != null)
                {
                    PersonAttributeType momNameAtt = Context.getPersonService().getPersonAttributeTypeByName("Mother's Name");
                    if(momNameAtt != null)
                    {
                        PersonAttribute pa = new PersonAttribute(momNameAtt, String.format("%s, %s", momsName.getFamilyName().getSurname().getValue(), momsName.getGivenName().getValue()));
                        patient.addAttribute(pa);
                    }
                }
                retVal.add(patient);
            }
        }

        return retVal;
    }


    /**
     * Get the PDQ endpoint
     * @return
     */
    public String getPdqEndpoint() {
        return Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_PDQ_ENDPOINT, "localhost");
    }

    /**
     * Get the PDQ port
     * @return
     */
    public Integer getPdqPort() {
        return Integer.valueOf(Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_PDQ_PORT, "8989"));
    }

}
