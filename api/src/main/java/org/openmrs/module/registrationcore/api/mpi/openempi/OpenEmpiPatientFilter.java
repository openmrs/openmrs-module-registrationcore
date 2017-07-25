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

        for (PatientAndMatchQuality patientWrapper : patients) {
            if (hasIdentifierTypeUuid(patientWrapper.getPatient(), filterIdentifierTypeUuid)) {
                removePatientsWithSameIdentifier(filterIdentifierTypeUuid, patientWrapper, patients);
            }
        }
    }

    private boolean hasIdentifierTypeUuid(Patient patient, String patientIdentifierTypeUuid) {
        return getPatientIdentifiersByIdentifiersTypeUuid(patient, patientIdentifierTypeUuid).size() > 0;
    }

    private Set<String> getPatientIdentifiersByIdentifiersTypeUuid(Patient patient, String patientIdentifierTypeUuid) {
        HashSet<String> patientIdentifiers = new HashSet<String>();
        for(PatientIdentifier patientIdentifier : patient.getIdentifiers()) {
            if (patientIdentifier.getIdentifierType() != null
                    && patientIdentifier.getIdentifierType().getUuid().equals(patientIdentifierTypeUuid)) {
                patientIdentifiers.add(patientIdentifier.getIdentifier());
            }
        }
        return patientIdentifiers;
    }

    private void removePatientsWithSameIdentifier(String patientIdentifierTypeUuid, PatientAndMatchQuality initialPatient,
                                                  List<PatientAndMatchQuality> patients) {
        Set<String> initialPatientIdentifiers = getPatientIdentifiersByIdentifiersTypeUuid(initialPatient.getPatient(),
                patientIdentifierTypeUuid);

        List<PatientAndMatchQuality> patientsToRemove = new ArrayList<PatientAndMatchQuality>();

        for (PatientAndMatchQuality secondaryPatient : patients) {
            if (secondaryPatient == initialPatient)
                continue;

            Set<String> secondaryPatientIdentifiers = getPatientIdentifiersByIdentifiersTypeUuid(
                    secondaryPatient.getPatient(), patientIdentifierTypeUuid);

            for (String initialPatientIdentifier : initialPatientIdentifiers) {
                if (secondaryPatientIdentifiers.contains(initialPatientIdentifier)) {
                    addPatientToRemove(initialPatient, secondaryPatient, patientsToRemove);
                    break;
                }
            }
        }

        patients.removeAll(patientsToRemove);
    }

    private void addPatientToRemove(PatientAndMatchQuality initialPatient,
                                    PatientAndMatchQuality secondaryPatient, List<PatientAndMatchQuality> patientsToRemove) {
        if (initialPatient.getPatient() instanceof MpiPatient) {
            patientsToRemove.add(initialPatient);
        } else if (secondaryPatient.getPatient() instanceof MpiPatient) {
            patientsToRemove.add(secondaryPatient);
        }
    }
}
