package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.api.errorhandling.ErrorHandlingService;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientsSearcher;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.santedb.mpiclient.api.MpiClientService;
import org.openmrs.module.santedb.mpiclient.api.impl.MpiClientServiceImpl;
import org.openmrs.module.santedb.mpiclient.model.MpiPatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

public class FhirSimilarPatientsSearcher implements MpiSimilarPatientsSearcher {

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


        List<Patient> retVal = new LinkedList<Patient>();
        try {
            MpiClientService mpiClientService = new MpiClientServiceImpl();
            List<MpiPatient> mpiPatients = mpiClientService.searchPatient(patient);
//            MpiClientService mpiClientService = Context.getService(MpiClientService.class);
//            List<MpiPatient> mpiPatients = mpiClientService.searchPatient(patient);

            for (MpiPatient mp : mpiPatients) {
                org.openmrs.module.registrationcore.api.mpi.common.MpiPatient mpiPatientExtract = new org.openmrs.module.registrationcore.api.mpi.common.MpiPatient();
                mpiPatientExtract.setId(mp.getId());
                mpiPatientExtract.setUuid(mp.getUuid());
                mpiPatientExtract.setBirthdate(mp.getBirthdate());
                mpiPatientExtract.setDead(mp.isDead());
                mpiPatientExtract.setGender(mp.getGender());
                log.error("Setting the location===========================================>"+mp.getSourceLocation());
                mpiPatientExtract.setSourceLocation(mp.getSourceLocation());
                PersonName pn = new PersonName(mp.getGivenName(),mp.getMiddleName(),mp.getFamilyName());
                mpiPatientExtract.addName(pn);
                retVal.add(mpiPatientExtract);
            }
//
        } catch (Exception e) {
//            e.printStackTrace();
            log.error("Error in FHIR Search", e);
            log.error(ExceptionUtils.getFullStackTrace(e));
//            ErrorHandlingService errorHandler = registrationCoreProperties.getPdqErrorHandlingService();
//            if (errorHandler != null) {
//                errorHandler.handle(e.getMessage(),
//                        "org.openmrs.module.registrationcore.api.mpi.pixpdq.FhirSimilarPatientsSearcher",
//                        true, ExceptionUtils.getFullStackTrace(e));
//            }
        }
//
        log.error("Mapping the records....In total we have : "+retVal.size());

        if (maxResults != null && retVal.size() > maxResults) {
            retVal = retVal.subList(0, maxResults);
        }

        return toMatchList(patient, retVal);
    }

    private List<PatientAndMatchQuality> toMatchList(Patient patient, List<Patient> patients) {
        log.error("Inside toMatch List method................................................................................");

        List<PatientAndMatchQuality> matchList = new ArrayList<>();

        for (Patient match : patients) {
            log.error(match.toString());
            List<String> matchedFields = new ArrayList<String>();
            Double score = 0.0;

            if (patient.getBirthdate() != null && match.getBirthdate() != null) {
                long birthdateDistance = Math.abs(patient.getBirthdate().getTime() - match.getBirthdate().getTime());
                if (birthdateDistance == 0) {
                    matchedFields.add("birthdate");
                    score += 0.125;
                }
            }

            if (patient.getGender() != null && match.getGender() != null
                    && patient.getGender().toLowerCase().equals(match.getGender().toLowerCase())) {
                matchedFields.add("gender");
                score += 0.125;
            }

            for (PersonName patientName : patient.getNames()) {
                for (PersonName matchName : match.getNames()) {
                    if (patientName.getFamilyName() != null && matchName.getFamilyName() != null
                            && patientName.getFamilyName().toLowerCase().equals(matchName.getFamilyName().toLowerCase())) {
                        matchedFields.add("names.familyName");
                        score += 0.125;
                    }
                    if (patientName.getMiddleName() != null && matchName.getMiddleName() != null
                            && patientName.getMiddleName().toLowerCase().equals(matchName.getMiddleName().toLowerCase())) {
                        matchedFields.add("names.middleName");
                        score += 0.125;
                    }
                    if (patientName.getGivenName() != null && matchName.getGivenName() != null
                            && patientName.getGivenName().toLowerCase().equals(matchName.getGivenName().toLowerCase())) {
                        matchedFields.add("names.givenName");
                        score += 0.125;
                    }
                }
            }

            for (PersonAddress personAddress : patient.getAddresses()) {
                for (PersonAddress matchAddress : match.getAddresses()) {
                    if (personAddress.getCountry() != null && matchAddress.getCountry() != null
                            && personAddress.getCountry().toLowerCase().equals(matchAddress.getCountry().toLowerCase())) {
                        matchedFields.add("addresses.country");
                        score += 0.125;
                    }
                    if (personAddress.getCityVillage() != null && matchAddress.getCityVillage() != null
                            && personAddress.getCityVillage().toLowerCase().equals(matchAddress.getCityVillage().toLowerCase())) {
                        matchedFields.add("addresses.cityVillage");
                        score += 0.125;
                    }
                    if (personAddress.getCountyDistrict() != null && matchAddress.getCountyDistrict() != null
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

}
