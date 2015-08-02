package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;

public class PatientQueryMapper {

    protected final Log log = LogFactory.getLog(this.getClass());

    public OpenEmpiPatientQuery convert(Patient patient) {
        OpenEmpiPatientQuery patientQuery = new OpenEmpiPatientQuery();

        //perform search by Family and Given names
        patientQuery.setFamilyName(patient.getFamilyName());
        patientQuery.setGivenName(patient.getGivenName());
        return patientQuery;
    }
}
