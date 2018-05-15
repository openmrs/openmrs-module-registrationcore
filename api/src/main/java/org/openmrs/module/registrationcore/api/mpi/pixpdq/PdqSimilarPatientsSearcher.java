package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.errorhandling.ErrorHandlingService;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientsSearcher;
import org.openmrs.module.registrationcore.api.mpi.openempi.PatientIdentifierMapper;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PdqSimilarPatientsSearcher implements MpiSimilarPatientsSearcher {

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.Hl7SenderHolder")
    private Hl7SenderHolder hl7SenderHolder;

    @Autowired
    private PatientIdentifierMapper identifierMapper;
    
    @Autowired
    @Qualifier("registrationcore.coreProperties")
    private RegistrationCoreProperties registrationCoreProperties;

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public List<PatientAndMatchQuality> findSimilarMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        return find(patient, maxResults);
    }

    @Override
    public List<PatientAndMatchQuality> findExactMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        return find(patient, maxResults);
    }

    private List<PatientAndMatchQuality> find(Patient patient, Integer maxResults) {
        Map<String, String> nameQueryParams = new HashMap<String, String>();
        if(patient.getFamilyName() != null && !patient.getFamilyName().isEmpty())
            nameQueryParams.put("@PID.5.1", patient.getFamilyName());
        if(patient.getGivenName() != null && !patient.getGivenName().isEmpty())
            nameQueryParams.put("@PID.5.2", patient.getGivenName());

        PatientIdentifier identifier = getLastIdentifier(patient.getIdentifiers());

        Map<String, String> identifierQueryParams = new HashMap<String, String>();
        if(identifier != null && identifier.getIdentifierType() != null) {
            String mpiUuid = identifierMapper.getMappedMpiIdentifierTypeId(identifier.getIdentifierType().getUuid());
            if (mpiUuid != null) {
                identifierQueryParams.put("@PID.3.1", identifier.getIdentifier());
                identifierQueryParams.put("@PID.3.4", mpiUuid);
            }
        }

        List<Patient> retVal = new LinkedList<Patient>();

        try {
            if (hasGivenNameOrFamilyName(patient)) {
                Message pdqRequest = pixPdqMessageUtil.createPdqMessage(nameQueryParams);
                Message response = hl7SenderHolder.getHl7v2Sender().sendPdqMessage(pdqRequest);

                retVal = pixPdqMessageUtil.interpretPIDSegments(response);
            } else if (identifier != null) {
                Message pdqRequest = pixPdqMessageUtil.createPdqMessage(identifierQueryParams);
                Message response = hl7SenderHolder.getHl7v2Sender().sendPdqMessage(pdqRequest);

                retVal = pixPdqMessageUtil.interpretPIDSegments(response);
            }
        } catch (Exception e) {
            log.error("Error in PDQ Search", e);
            ErrorHandlingService errorHandler = registrationCoreProperties.getPdqErrorHandlingService();
            if (errorHandler != null) {
                errorHandler.handle(e.getMessage(),
                        "org.openmrs.module.registrationcore.api.mpi.pixpdq.PdqSimilarPatientsSearcher",
                        true, ExceptionUtils.getFullStackTrace(e));
            }
        }

        if (maxResults != null && retVal.size() > maxResults) {
            retVal = retVal.subList(0, maxResults);
        }

        return toMatchList(patient, retVal);
    }

    private List<PatientAndMatchQuality> toMatchList(Patient patient, List<Patient> patients) {
        List<PatientAndMatchQuality> matchList = new ArrayList<PatientAndMatchQuality>();

        for (Patient match : patients) {
            List<String> matchedFields = new ArrayList<String>();
            Double score = 0.0;

            if(patient.getBirthdate() != null && match.getBirthdate() != null) {
                long birthdateDistance = Math.abs(patient.getBirthdate().getTime() - match.getBirthdate().getTime());
                if(birthdateDistance == 0){
                    matchedFields.add("birthdate");
                    score += 0.125;
                }
            }

            if(patient.getGender() != null && match.getGender() != null
                    && patient.getGender().toLowerCase().equals(match.getGender().toLowerCase())) {
                matchedFields.add("gender");
                score += 0.125;
            }

            for (PersonName patientName : patient.getNames()) {
                for (PersonName matchName : match.getNames()) {
                    if(patientName.getFamilyName() != null && matchName.getFamilyName() != null
                            && patientName.getFamilyName().toLowerCase().equals(matchName.getFamilyName().toLowerCase())) {
                        matchedFields.add("names.familyName");
                        score += 0.125;
                    }
                    if(patientName.getMiddleName() != null && matchName.getMiddleName() != null
                            && patientName.getMiddleName().toLowerCase().equals(matchName.getMiddleName().toLowerCase())) {
                        matchedFields.add("names.middleName");
                        score += 0.125;
                    }
                    if(patientName.getGivenName() != null && matchName.getGivenName() != null
                            && patientName.getGivenName().toLowerCase().equals(matchName.getGivenName().toLowerCase())) {
                        matchedFields.add("names.givenName");
                        score += 0.125;
                    }
                }
            }

            for(PersonAddress personAddress : patient.getAddresses()) {
                for(PersonAddress matchAddress : match.getAddresses()) {
                    if(personAddress.getCountry() != null && matchAddress.getCountry() != null
                            && personAddress.getCountry().toLowerCase().equals(matchAddress.getCountry().toLowerCase())) {
                        matchedFields.add("addresses.country");
                        score += 0.125;
                    }
                    if(personAddress.getCityVillage() != null && matchAddress.getCityVillage() != null
                            && personAddress.getCityVillage().toLowerCase().equals(matchAddress.getCityVillage().toLowerCase())) {
                        matchedFields.add("addresses.cityVillage");
                        score += 0.125;
                    }
                    if(personAddress.getCountyDistrict() != null && matchAddress.getCountyDistrict() != null
                            && personAddress.getCountyDistrict().toLowerCase().equals(matchAddress.getCountyDistrict().toLowerCase())) {
                        matchedFields.add("addresses.countryDistrict");
                        score += 0.125;
                    }
                }
            }

            matchList.add(new PatientAndMatchQuality(match, score, matchedFields));
        }

        return matchList;
    }

    private boolean hasGivenNameOrFamilyName(Patient patient) {
        return (patient.getFamilyName() != null && !patient.getFamilyName().isEmpty())
                || (patient.getGivenName() != null && !patient.getGivenName().isEmpty());
    }

    private PatientIdentifier getLastIdentifier(Set<PatientIdentifier> identifiers) {

        PatientIdentifier lastIdentifier = new PatientIdentifier();

        for (PatientIdentifier patIdentifier : identifiers) {
                lastIdentifier = patIdentifier;
        }
        return  lastIdentifier;
    }
}
