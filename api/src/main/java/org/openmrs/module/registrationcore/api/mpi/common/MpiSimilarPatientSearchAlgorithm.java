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
 * An MPI interface, which provide finding similar and exact matched patients, and also provide importing
 * patient by Id.
 */
public interface MpiSimilarPatientSearchAlgorithm {

    List<PatientAndMatchQuality> findSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff,
                                                     Integer maxResults);

    List<PatientAndMatchQuality> findExactSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff,
                                                          Integer maxResults);

    Patient importMpiPatient(String patientId);
}
