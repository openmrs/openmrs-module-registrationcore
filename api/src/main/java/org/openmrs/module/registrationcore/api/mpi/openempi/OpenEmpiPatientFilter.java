package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.PatientIdentifier;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFilter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs filter by OpenEmpi person identifier.
 */
public class OpenEmpiPatientFilter implements MpiPatientFilter {

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    public void filter(List<PatientAndMatchQuality> patients) {
        Integer filterIdentifierId = mpiProperties.getMpiPersonIdentifierTypeId();

        for (PatientAndMatchQuality patientWrapper : patients) {
            if (patientWrapper.getPatient().getPatientIdentifier(filterIdentifierId) != null) {
                removePatientsWithSameIdentifier(filterIdentifierId, patientWrapper, patients);
            }
        }
    }

    private void removePatientsWithSameIdentifier(Integer globalIdentifierDomainId, PatientAndMatchQuality initialPatient,
                                                  List<PatientAndMatchQuality> patients) {
        String initialPatientIdentifier = initialPatient.getPatient().getPatientIdentifier(globalIdentifierDomainId)
                .getIdentifier();

        List<PatientAndMatchQuality> patientsToRemove = new ArrayList<PatientAndMatchQuality>();

        for (PatientAndMatchQuality secondaryPatient : patients) {
            if (secondaryPatient == initialPatient)
                continue;

            PatientIdentifier secondaryPatientIdentifier = secondaryPatient.getPatient()
                    .getPatientIdentifier(globalIdentifierDomainId);

            if (secondaryPatientIdentifier != null &&
                    secondaryPatientIdentifier.getIdentifier().equals(initialPatientIdentifier)) {
                addPatientToRemove(initialPatient, secondaryPatient, patientsToRemove);
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
