package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.lang.StringUtils;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

public class PatientQueryMapper {

    private LocationService locationService;

    private Location identifierLocation;

    private IdentifierSource openEmpiIdentifierSource;

    private void validateInitialization() {
        if (identifierLocation == null || openEmpiIdentifierSource == null)
            init();
    }

    private void init() {
        IdentifierSourceService iss = Context.getService(IdentifierSourceService.class);
        //TODO move it to global settings.
        // adminService.getGlobalProperty(RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID);
        String idSourceId = "2";
        if (StringUtils.isBlank(idSourceId))
            throw new APIException("Please set the id of the identifier source to use to generate patient identifiers");
        try {
            openEmpiIdentifierSource = iss.getIdentifierSource(Integer.valueOf(idSourceId));
            if (openEmpiIdentifierSource == null)
                throw new APIException("cannot find identifier source with id:" + idSourceId);
        } catch (NumberFormatException e) {
            throw new APIException("Identifier source id should be a number");
        }
        //TODO change it to correct location
        identifierLocation = locationService.getDefaultLocation();
    }

    public OpenEmpiPatientQuery convert(Patient patient) {
        OpenEmpiPatientQuery patientQuery = new OpenEmpiPatientQuery();

        patientQuery.setFamilyName(patient.getFamilyName());
        patientQuery.setGivenName(patient.getGivenName());
//        patientQuery.setMiddleName(patient.getMiddleName());
//        patientQuery.setDateOfBirth(patient.getBirthdate());

        return patientQuery;
    }

    public MpiPatient convert(OpenEmpiPatientQuery patientQuery) {
        validateInitialization();
        MpiPatient patient = new MpiPatient();
        patient.setMpiPatient(true);

        patient.setPatientId(patientQuery.getPersonId());

        setNames(patientQuery, patient);

        setOpenEmpiId(patientQuery, patient);

        patient.setGender(patientQuery.getGender().getGenderCode());

        setBirthdate(patientQuery, patient);

        setAddresses(patientQuery, patient);
        return patient;
    }

    private void setNames(OpenEmpiPatientQuery patientQuery, Patient patient) {
        PersonName names = new PersonName();
        names.setFamilyName(patientQuery.getFamilyName());
        names.setGivenName(patientQuery.getGivenName());
        patient.setNames(new HashSet<PersonName>(Collections.singleton(names)));
    }

    private void setOpenEmpiId(OpenEmpiPatientQuery patientQuery, Patient patient) {
        Integer mpiPersonId = patientQuery.getPersonId();

        PatientIdentifier identifier =
                new PatientIdentifier(String.valueOf(mpiPersonId),
                        openEmpiIdentifierSource.getIdentifierType(), identifierLocation);

        patient.addIdentifier(identifier);
    }

    private void setAddresses(OpenEmpiPatientQuery patientQuery, Patient patient) {
        HashSet<PersonAddress> addresses = new HashSet<PersonAddress>();
        PersonAddress address = new PersonAddress();
        address.setAddress1(patientQuery.getAddress1());
        addresses.add(address);
        patient.setAddresses(addresses);
    }

    private void setBirthdate(OpenEmpiPatientQuery patientQuery, Patient patient) {
        if (patientQuery.getDateOfBirth() == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(patientQuery.getDateOfBirth());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date clearDate = calendar.getTime();
        patient.setBirthdate(clearDate);
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }
}
