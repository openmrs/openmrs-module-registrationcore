package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.List;

/**
 * Perform patients filtering.
 */
public interface MpiPatientFilter {

    void filter(List<PatientAndMatchQuality> patients);
}
