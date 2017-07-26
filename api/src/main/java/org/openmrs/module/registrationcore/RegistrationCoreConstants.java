/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.registrationcore;

public final class RegistrationCoreConstants {

	/**
	 * Specifies the identifier source to use when generating patient identifiers
	 */
	public static final String GP_OPENMRS_IDENTIFIER_SOURCE_ID = "registrationcore.identifierSourceId";

	public static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

	public static final String PATIENT_REGISTRATION_EVENT_TOPIC_NAME = "org.openmrs.module.registrationcore.PatientRegistrationEvent";

	public static final String PATIENT_EDIT_EVENT_TOPIC_NAME = "org.openmrs.module.registrationcore.PatientEditEvent";

	public static final String KEY_PATIENT_UUID = "patientUuid";

	public static final String KEY_RELATIONSHIP_UUIDS = "relationshipUuids";

	public static final String KEY_REGISTERER_UUID = "registererUuid";

	public static final String KEY_REGISTERER_ID = "registererId";

	public static final String KEY_DATE_REGISTERED = "dateRegistered";

	public static final String KEY_WAS_A_PERSON = "wasAPerson";

    public static final String GP_PATIENT_NAME_SEARCH = "registrationcore.patientNameSearch";

    public static final String GP_FAST_SIMILAR_PATIENT_SEARCH_ALGORITHM = "registrationcore.fastSimilarPatientSearchAlgorithm";

	public static final String GP_PRECISE_SIMILAR_PATIENT_SEARCH_ALGORITHM = "registrationcore.preciseSimilarPatientSearchAlgorithm";

    public static final String LOCATION_TAG_IDENTIFIER_ASSIGNMENT_LOCATION = "Identifier Assignment Location";

	public static final String GP_MPI_GLOBAL_IDENTIFIER_DOMAIN_ID = "registrationcore.openempi.globalIdentifierDomainId";

	public static final String GP_MPI_PERSON_IDENTIFIER_ID = "registrationcore.mpi.personIdentifierId";

	public static final String GP_LOCAL_MPI_IDENTIFIER_TYPE_MAP = "registrationcore.local_mpi_identifierTypeMap.";

	public static final String GP_MPI_IMPLEMENTATION = "registrationcore.mpi.implementation";

	public static final String GP_MPI_HL7_IMPLEMENTATION = "registrationcore.mpi.Hl7implementation";

	public static final String GP_MPI_URL = "registrationcore.mpi.url";

	public static final String GP_MPI_ACCESS_USERNAME = "registrationcore.mpi.username";

	public static final String GP_MPI_ACCESS_PASSWORD = "registrationcore.mpi.password";

	public static final String GP_PROBABLY_MATCH_ENABLED = "registrationcore.openempi.enableProbabilisticMatching";

	public static final String GP_MPI_PDQ_ENDPOINT= "registrationcore.mpi.pdqEndpoint";

	public static final String GP_MPI_PDQ_PORT = "registrationcore.mpi.pdqPort";

	public static final String GP_MPI_PIX_ENDPOINT= "registrationcore.mpi.pixEndpoint";

	public static final String GP_MPI_PIX_PORT = "registrationcore.mpi.pixPort";

	public static final String GP_MPI_SENDING_APPLICATION = "registrationcore.mpi.sendingApplication";

	public static final String GP_MPI_SENDING_FACILITY = "registrationcore.mpi.sendingFacility";

	public static final String GP_MPI_PATIENT_ROOT = "registrationcore.mpi.patientRoot";

	public static final String GP_MPI_UNI_ID_TYPE = "registrationcore.mpi.universalIdType";

	public static final String GP_MPI_RECEIVING_APPLICATION = "registsrationcore.mpi.receivingApplication";

	public static final String GP_MPI_RECEIVING_FACILITY = "registrationcore.mpi.receivingFacility";

	public static final String GP_BIOMETRICS_IMPLEMENTATION = "registrationcore.biometrics.implementation";

	public static final String GP_ERROR_HANDLER_IMPLEMENTATION = "registrationcore.errorHandler.implementation";
}