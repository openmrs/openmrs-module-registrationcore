/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.registrationcore.api.mpi.common;


import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.List;
import java.util.Map;

/**
 * This service provide finding precise similar and probably similar matched patients.
 */
public interface MpiSimilarPatientsSearcher {

    /**
     * Perform search on MPI server for probably similar patients.
     *
     * @param patient
     * @param otherDataPoints
     * @param cutoff
     * @param maxResults
     * @return list of possible matched patients
     */
    List<PatientAndMatchQuality> findProbablySimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff,
                                                             Integer maxResults);

    /**
     * Perform search on MPI server for precise similar patients.
     * You would use this to perform one final check before actually creating a patient, after all data has been filled.
     *
     * @param patient
     * @param otherDataPoints
     * @param cutoff
     * @param maxResults
     * @return list of possible matched patients
     */
    List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff,
                                                            Integer maxResults);
}
