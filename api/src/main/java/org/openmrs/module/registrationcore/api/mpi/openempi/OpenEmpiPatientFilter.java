package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFilter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Performs filter by OpenEmpi person identifier.
 */
public class OpenEmpiPatientFilter implements MpiPatientFilter {

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    public void filter(List<PatientAndMatchQuality> patients) {
        String filterIdentifierTypeUuid = mpiProperties.getMpiPersonIdentifierTypeUuid();
        Set<PatientAndMatchQuality> patientsToRemove = new HashSet<PatientAndMatchQuality>();

        for (PatientAndMatchQuality patientWrapper : patients) {
            if (hasIdentifierTypeUuid(patientWrapper.getPatient(), filterIdentifierTypeUuid)) {
                patientsToRemove
                        .addAll(getPatientsWithSameIdentifier(filterIdentifierTypeUuid, patientWrapper, patients));
            }
        }
        patients.removeAll(patientsToRemove);
    }

    private boolean hasIdentifierTypeUuid(Patient patient, String patientIdentifierTypeUuid) {
        return getPatientIdentifierByIdentifierTypeUuid(patient, patientIdentifierTypeUuid) != null;
    }

    private String getPatientIdentifierByIdentifierTypeUuid(Patient patient, String patientIdentifierTypeUuid) {
        for (PatientIdentifier patientIdentifier : patient.getIdentifiers()) {
            if (patientIdentifier.getIdentifierType() != null
                    && patientIdentifier.getIdentifierType().getUuid().equals(patientIdentifierTypeUuid)) {
                return patientIdentifier.getIdentifier();
            }
        }
        return null;
    }

    private List<PatientAndMatchQuality> getPatientsWithSameIdentifier(String patientIdentifierTypeUuid,
            PatientAndMatchQuality initialPatient, List<PatientAndMatchQuality> patients) {
        String initialPatientIdentifier = getPatientIdentifierByIdentifierTypeUuid(initialPatient.getPatient(),
                patientIdentifierTypeUuid);

        List<PatientAndMatchQuality> patientsToRemove = new ArrayList<PatientAndMatchQuality>();

        for (PatientAndMatchQuality secondaryPatient : patients) {
            if (secondaryPatient == initialPatient)
                continue;

            String secondaryPatientIdentifier = getPatientIdentifierByIdentifierTypeUuid(secondaryPatient.getPatient(),
                    patientIdentifierTypeUuid);

            if (secondaryPatientIdentifier != null && secondaryPatientIdentifier.equals(initialPatientIdentifier)) {
                addPatientToRemove(initialPatient, secondaryPatient, patientsToRemove);
            }
        }

        return patientsToRemove;
    }

    private void addPatientToRemove(PatientAndMatchQuality initialPatient, PatientAndMatchQuality secondaryPatient,
            List<PatientAndMatchQuality> patientsToRemove) {
        if (initialPatient.getPatient() instanceof MpiPatient) {
            patientsToRemove.add(initialPatient);
        } else if (secondaryPatient.getPatient() instanceof MpiPatient) {
            patientsToRemove.add(secondaryPatient);
        }
    }
}
