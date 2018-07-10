package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.XAD;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.datatype.XTN;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.QBP_Q21;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.util.Terser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.mpi.openempi.PatientIdentifierMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.Set;


public class PixPdqMessageUtil {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private RegistrationCoreProperties config;

    @Autowired
    private PatientIdentifierMapper identifierMapper;

    @Autowired
    private PatientService patientService;

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    public Message createPdqMessage(Map<String, String> queryParameters) throws HL7Exception {
        QBP_Q21 message = new QBP_Q21();
        updateMSH(message.getMSH(), "QBP", "Q22");

        Terser terser = new Terser(message);

        // Set the query parameters
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

    public Message createPdqMessage(Patient patient) throws HL7Exception {
        return createPdqMessage(patientToQPD3Params(patient));
    }

    public Map<String, String> patientToQPD3Params(Patient patient){
        Map<String, String> queryParams = new HashMap<String, String>();
        // Add Names to query
        if(patient.getFamilyName() != null && !patient.getFamilyName().isEmpty())
            queryParams.put("@PID.5.1", patient.getFamilyName());
        if(patient.getGivenName() != null && !patient.getGivenName().isEmpty())
            queryParams.put("@PID.5.2", patient.getGivenName());
        // Add Identifiers to query
        Set<PatientIdentifier> identifiers = patient.getIdentifiers();
        if(!identifiers.isEmpty()){
            for (PatientIdentifier patIdentifier : identifiers) {
                String mappedMpiUuid = identifierMapper.getMappedMpiIdentifierTypeId(patIdentifier.getIdentifierType().getUuid());
                if (mappedMpiUuid != null) {
                    queryParams.put("@PID.3.1", patIdentifier.getIdentifier());
                    queryParams.put("@PID.3.4", mappedMpiUuid);
                }
            }
        }
        // Add Gender to query

        // Add Date of Birth to query (?) what if its an estimate?

        // Add Address to query

        return queryParams;
    }



    private void updateMSH(MSH msh, String messageCode, String triggerEvent) throws DataTypeException {
        msh.getFieldSeparator().setValue("|");
        msh.getEncodingCharacters().setValue("^~\\&");
        msh.getAcceptAcknowledgmentType().setValue("AL"); // Always send response
        msh.getDateTimeOfMessage().getTime().setValue(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())); // DateTime of message
        msh.getMessageControlID().setValue(UUID.randomUUID().toString()); // Unique id for message
        msh.getMessageType().getMessageStructure().setValue(msh.getMessage().getName()); // Message Structure Type
        msh.getMessageType().getMessageCode().setValue(messageCode); // Message Structure Code
        msh.getMessageType().getTriggerEvent().setValue(triggerEvent); // Trigger Event
        msh.getProcessingID().getProcessingID().setValue("P"); // Production
        msh.getReceivingApplication().getNamespaceID().setValue(config.getReceivingApplication());
        msh.getReceivingFacility().getNamespaceID().setValue(config.getReceivingFacility());

        String implementation = config.getSendingApplication();
        msh.getSendingApplication().getNamespaceID().setValue(implementation);

        String location = config.getSendingFacility();
        msh.getSendingFacility().getNamespaceID().setValue(location);

        msh.getVersionID().getVersionID().setValue("2.5");
    }

    /**
     * Interpret PID segments
     * @param response
     * @return
     * @throws HL7Exception
     */
    public List<Patient> interpretPIDSegments (
            Message response) throws HL7Exception, ParseException {
        List<Patient> retVal = new ArrayList<Patient>();

        Terser terser = new Terser(response);
        // Check for AA and OK in QAK
        if (terser.get("/MSA-1") != null && terser.get("/MSA-1").equals("AE")) {
            throw new HL7Exception("Server Error");
        } else if (terser.get("/QAK-2") != null && terser.get("/QAK-2").equals("NF")) {
            return retVal;
        }

        Location defaultLocation = Context.getLocationService().getDefaultLocation();

        for (Structure queryResponseStruct : response.getAll("QUERY_RESPONSE")) {
            Group queryResponseGroup = (Group)queryResponseStruct;
            for (Structure pidStruct : queryResponseGroup.getAll("PID")) {
                PID pid = (PID)pidStruct;
                Patient patient = new MpiPatient();
                // Attempt to load a patient by identifier
                for (CX id : pid.getPatientIdentifierList()) {

                    PatientIdentifierType pit = null;

                    if (id.getAssigningAuthority().getUniversalID().getValue() != null &&
                            !id.getAssigningAuthority().getUniversalID().getValue().isEmpty()) {
                        pit = Context.getPatientService().getPatientIdentifierTypeByUuid(
                                identifierMapper.getMappedLocalIdentifierTypeUuid(id.getAssigningAuthority().getUniversalID().getValue()));
                    }
                    if (pit == null && id.getAssigningAuthority().getNamespaceID().getValue() != null &&
                            !id.getAssigningAuthority().getNamespaceID().getValue().isEmpty()) {
                        pit = Context.getPatientService().getPatientIdentifierTypeByUuid(
                                identifierMapper.getMappedLocalIdentifierTypeUuid(id.getAssigningAuthority().getNamespaceID().getValue()));
                    }
                    if (pit == null) {
                        continue;
                    }

                    PatientIdentifier patId = new PatientIdentifier(
                            id.getIDNumber().getValue(),
                            pit,
                            defaultLocation
                    );

                    if (patId.getIdentifierType().equals(patientService.getPatientIdentifierTypeByUuid(
                            mpiProperties.getMpiPersonIdentifierTypeUuid()))) {
                        patId.setPreferred(true);
                    }

                    patient.addIdentifier(patId);
                }

                // Attempt to copy names
                for (XPN xpn : pid.getPatientName()) {
                    PersonName pn = new PersonName();

                    // Family name
                    if (xpn.getFamilyName().getSurname().getValue() == null || xpn.getFamilyName().getSurname().getValue().isEmpty()) {
                        pn.setFamilyName("(none)");
                    } else {
                        pn.setFamilyName(xpn.getFamilyName().getSurname().getValue());
                    }
                    pn.setFamilyName2(xpn.getFamilyName().getSurnameFromPartnerSpouse().getValue());

                    // Given name
                    if (xpn.getGivenName().getValue() == null || xpn.getGivenName().getValue().isEmpty()) {
                        pn.setGivenName("(none)");
                    } else {
                        pn.setGivenName(xpn.getGivenName().getValue());
                    }
                    pn.setMiddleName(xpn.getSecondAndFurtherGivenNamesOrInitialsThereof().getValue());
                    pn.setPrefix(xpn.getPrefixEgDR().getValue());

                    if ("L".equals(xpn.getNameTypeCode().getValue())) {
                        pn.setPreferred(true);
                    }

                    patient.addName(pn);
                }

                if (patient.getNames().size() == 0) {
                    patient.addName(new PersonName("(none)", null, "(none)"));
                }
                // Copy gender
                patient.setGender(pid.getAdministrativeSex().getValue());
                patient.setDead("Y".equals(pid.getPatientDeathIndicator().getValue()));

                DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);

                // Copy DOB
                if (pid.getDateTimeOfBirth().getTime().getValue() != null) {
                    Date birthDate = format.parse(pid.getDateTimeOfBirth().getTime().getValue());
                    patient.setBirthdate(birthDate);
                }

                // Death details
                if (pid.getPatientDeathDateAndTime().getTime().getValue() != null) {
                    Date deadDate = format.parse(pid.getPatientDeathDateAndTime().getTime().getValue());
                    patient.setDeathDate(deadDate);
                }
                patient.setDead("Y".equals(pid.getPatientDeathIndicator().getValue()));

                // Addresses
                for (XAD xad : pid.getPatientAddress()) {
                    PersonAddress pa = new PersonAddress();
                    pa.setAddress1(xad.getStreetAddress().getStreetOrMailingAddress().getValue());
                    pa.setAddress2(xad.getOtherDesignation().getValue());
                    pa.setCityVillage(xad.getCity().getValue());
                    pa.setCountry(xad.getCountry().getValue());
                    pa.setCountyDistrict(xad.getCountyParishCode().getValue());
                    pa.setPostalCode(xad.getZipOrPostalCode().getValue());
                    pa.setStateProvince(xad.getStateOrProvince().getValue());
                    if ("H".equals(xad.getAddressType().getValue())) {
                        pa.setPreferred(true);
                    }

                    patient.addAddress(pa);
                }

                // Mother's name
                XPN momsName = pid.getMotherSMaidenName(0);
                if (momsName != null) {
                    PersonAttributeType momNameAtt = Context.getPersonService().getPersonAttributeTypeByName("Mother's Name");
                    if (momNameAtt != null) {
                        PersonAttribute pa = new PersonAttribute(momNameAtt, String.format("%s, %s", momsName.getFamilyName().getSurname().getValue(), momsName.getGivenName().getValue()));
                        patient.addAttribute(pa);
                    }
                }
                retVal.add(patient);
            }
        }

        return retVal;
    }

    public Message createAdmit(Patient patient) throws HL7Exception {
        ADT_A01 message = new ADT_A01();
        this.updateMSH(message.getMSH(), "ADT", "A01");
        message.getMSH().getVersionID().getVersionID().setValue("2.3.1");
        // Move patient data to PID
        this.updatePID(message.getPID(), patient);

        return message;
    }

    /**
     * Create the update message
     * @throws HL7Exception
     */
    public Message createUpdate(Patient patient) throws HL7Exception {
        ADT_A01 message = new ADT_A01();
        this.updateMSH(message.getMSH(), "ADT", "A08");
        message.getMSH().getVersionID().getVersionID().setValue("2.3.1");

        // Move patient data to PID
        this.updatePID(message.getPID(), patient);

        return message;
    }

    /**
     * Update the PID segment
     * @throws HL7Exception
     */
    private void updatePID(PID pid, Patient patient) throws HL7Exception {
        // Identifiers
        List<String> uuidList = mpiProperties.getPixIdentifierTypeUuidList();
        for (PatientIdentifier patIdentifier : patient.getIdentifiers()) {
            if (uuidList.contains(patIdentifier.getIdentifierType().getUuid())) {
                CX patientId = pid.getPatientIdentifierList(pid.getPatientIdentifierList().length);
                String mpiUuid = identifierMapper.getMappedMpiIdentifierTypeId(patIdentifier.getIdentifierType().getUuid());
                patientId.getAssigningAuthority().getUniversalID().setValue(mpiUuid);
                patientId.getAssigningAuthority().getNamespaceID().setValue(mpiUuid);
                patientId.getAssigningAuthority().getUniversalIDType().setValue(identifierMapper.getMappedMpiUniversalIdType(patIdentifier.getIdentifierType().getUuid()));
                patientId.getIDNumber().setValue(patIdentifier.getIdentifier());

            }
        }

        // Names
        for(PersonName pn : patient.getNames()) {
            if (!pn.getFamilyName().equals("(none)") && !pn.getGivenName().equals("(none)")) {
                this.updateXPN(pid.getPatientName(pid.getPatientName().length), pn);
            }
        }

        // Gender
        pid.getAdministrativeSex().setValue(patient.getGender());

        // Date of birth
        if(patient.getBirthdateEstimated())
            pid.getDateTimeOfBirth().getTime().setValue(new SimpleDateFormat("yyyy").format(patient.getBirthdate()));
        else
            pid.getDateTimeOfBirth().getTime().setValue(new SimpleDateFormat("yyyyMMdd").format(patient.getBirthdate()));

        PersonAddress pa = patient.getAddresses().iterator().next();

        // Addresses
        XAD xad = pid.getPatientAddress(pid.getPatientAddress().length);
        if(pa.getAddress1() != null)
            xad.getStreetAddress().getStreetOrMailingAddress().setValue(pa.getAddress1());
        if(pa.getAddress2() != null)
            xad.getOtherDesignation().setValue(pa.getAddress2());
        if(pa.getAddress3() != null)
            xad.getOtherDesignation().setValue(xad.getOtherDesignation() + " " + pa.getAddress3());
        if(pa.getCityVillage() != null)
            xad.getCity().setValue(pa.getCityVillage());
        if(pa.getCountry() != null)
            xad.getCountry().setValue(pa.getCountry());
        if(pa.getCountyDistrict() != null)
            xad.getCountyParishCode().setValue(pa.getCountyDistrict());
        if(pa.getPostalCode() != null)
            xad.getZipOrPostalCode().setValue(pa.getPostalCode());
        if(pa.getStateProvince() != null)
            xad.getStateOrProvince().setValue(pa.getStateProvince());

        if(pa.getPreferred())
            xad.getAddressType().setValue("L");

        // Death
        if (patient.getDead()) {
            pid.getPatientDeathIndicator().setValue("Y");
            pid.getPatientDeathDateAndTime().getTime().setDatePrecision(patient.getDeathDate().getYear(), patient.getDeathDate().getMonth(), patient.getDeathDate().getDay());
        }

        PersonName mother = new PersonName();

        // Mother and Telephone
        for (PersonAttribute attribute : patient.getAttributes()) {
            if (isMotherAttributeType(attribute) && mother.getFamilyName() == null) {
                mother.setFamilyName(attribute.getValue());
                this.updateXPN(pid.getMotherSMaidenName(0), mother);
            } else if (isTelephoneAttributeType(attribute)) {
                XTN xtn = pid.getPhoneNumberHome(0);
                xtn.getTelephoneNumber().setValue(attribute.getValue());
                xtn.getLocalNumber().setValue(attribute.getValue());
            }
        }

    }

    public boolean isQueryError(Message message) throws HL7Exception {
        Terser terser = new Terser(message);
        return !terser.get("/MSA-1").endsWith("A");
    }

    private boolean isMotherAttributeType(PersonAttribute attribute) {
       return attribute.getAttributeType().getName().toLowerCase().contains("mother");
    }

    private boolean isTelephoneAttributeType(PersonAttribute attribute) {
        return attribute.getAttributeType().getName().toLowerCase().contains("telephone");
    }

    /**
     * Updates the PN with the XPN
     * @param xpn
     * @param pn
     * @throws DataTypeException
     */
    private void updateXPN(XPN xpn, PersonName pn) throws DataTypeException {
        if(pn.getFamilyName() != null && !pn.getFamilyName().equals("(none)"))
            xpn.getFamilyName().getSurname().setValue(pn.getFamilyName());
        if(pn.getFamilyName2() != null)
            xpn.getFamilyName().getSurnameFromPartnerSpouse().setValue(pn.getFamilyName2());
        if(pn.getGivenName() != null && !pn.getGivenName().equals("(none)"))
            xpn.getGivenName().setValue(pn.getGivenName());
        if(pn.getMiddleName() != null)
            xpn.getSecondAndFurtherGivenNamesOrInitialsThereof().setValue(pn.getMiddleName());
        if(pn.getPrefix() != null)
            xpn.getPrefixEgDR().setValue(pn.getPrefix());

        if(pn.getPreferred())
            xpn.getNameTypeCode().setValue("L");
        else
            xpn.getNameTypeCode().setValue("U");

    }

}
