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

	public static final String GP_OPENMRS_IDENTIFIER_UUID = "registrationcore.openmrsIdenitfier.uuid";

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

	public static final String GP_MPI_PERSON_IDENTIFIER_TYPE_UUID = "registrationcore.mpi.personIdentifierTypeUuid";

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

	public static final String GP_MPI_PIX_IDENTTIFIER_TYPE_UUID_LIST = "registrationcore.mpi.pixIdentifierTypeUuidList";

	public static final String GP_MPI_PIX_ERROR_HANDLER_IMPLEMENTATION = "registrationcore.mpi.pix.errorHandler.implementation";

	public static final String GP_MPI_PDQ_ERROR_HANDLER_IMPLEMENTATION = "registrationcore.mpi.pdq.errorHandler.implementation";

	public static final String MPI_IDENTIFIER_TYPE_ECID_NAME = "ECID";

	public static final String GP_BIRTHDATE_ESTIMATION_START_MONTH = "registrationcore.birthdateEstimationStartMonth";

	public static final String GP_BIOMETRICS_IMPLEMENTATION = "registrationcore.biometrics.implementation";

	/*
	iSantePlus Addition:

	public static final String GP_BIOMETRICS_PERSON_IDENTIFIER_TYPE_UUID = "registrationcore.biometrics.personIdentifierTypeUuid";

	public static final String GP_BIOMETRICS_NATIONAL_PERSON_IDENTIFIER_TYPE_UUID =
			"registrationcore.biometrics.nationalPersonIdentifierTypeUuid";

	public static final String LOCAL_FINGERPRINT_NAME = "localFingerprint";

	public static final String NATIONAL_FINGERPRINT_NAME = "nationalFingerprint";
	*/

	public static final String REGISTRATIONCORE_LOCAL_MPI_IDENTIFIERTYPEMAP_ECID = "registrationcore.local_mpi_identifierTypeMap.ECID";

	public static final String REGISTRATIONCORE_LOCAL_MPI_IDENTIFIERTYPEMAP_iSantePlus_Code_National = "registrationcore.local_mpi_identifierTypeMap.Code National";

	public static final String REGISTRATIONCORE_LOCAL_MPI_IDENTIFIERTYPEMAP_iSantePlus_ID = "registrationcore.local_mpi_identifierTypeMap.iSantePlus ID";

	public static final String REGISTRATIONCORE_LOCAL_MPI_IDENTIFIERTYPEMAP_iSantePlus_Code_ST = "registrationcore.local_mpi_identifierTypeMap.Code ST";

	// CCSY EDITED
	//	public static final String REGISTRATIONCORE_LOCAL_MPI_IDENTIFIERTYPEMAP_Biometrics_Reference_Code = "registrationcore.local_mpi_identifierTypeMap.Biometrics Reference Code";

	public static final String REGISTRATIONCORE_MPI_ECID_UNIVERSAL_IDENTIFIER = "2.16.840.1.113883.4.56";

	public static final String REGISTRATIONCORE_MPI_ECID_UNIVERSAL_IDENTIFIER_TYPE = "NI";

	public static final String REGISTRATIONCORE_MPI_iSantePlus_Code_National_UNIVERSAL_IDENTIFIER = "2.25.212283553061960040061731875660599129565";

	public static final String REGISTRATIONCORE_MPI_iSantePlus_Code_National_UNIVERSAL_IDENTIFIER_TYPE = "PI";

	public static final String REGISTRATIONCORE_MPI_iSantePlus_ID_UNIVERSAL_IDENTIFIER = "2.25.71280592878078638113873461180761116318";

	public static final String REGISTRATIONCORE_MPI_iSantePlus_ID_UNIVERSAL_IDENTIFIER_TYPE = "PI";

	public static final String REGISTRATIONCORE_MPI_iSantePlus_Code_ST_UNIVERSAL_IDENTIFIER = "2.25.276946543544871160225835991160192746993";

	public static final String REGISTRATIONCORE_MPI_iSantePlus_Code_ST_UNIVERSAL_IDENTIFIER_TYPE = "PI";

	public static final String REGISTRATIONCORE_MPI_PASSWORD = "registrationcore.mpi.password";

	// CCSY EDITED
	//	public static final String REGISTRATIONCORE_MPI_Biometrics_Reference_Code_UNIVERSAL_IDENTIFIER = "2.25.300969590489438061583573695579607328089";

	//	public static final String REGISTRATIONCORE_MPI_M2Sys_Fingerprint_Registration_ID_UNIVERSAL_IDENTIFIER_TYPE = "NI";

	public static final String REGISTRATIONCORE_MPI_PASSWORD_VALUE = "pixc";

	public static final String REGISTRATIONCORE_MPI_PDQ_ENDPOINT = "registrationcore.mpi.pdqEndpoint";

	public static final String REGISTRATIONCORE_MPI_PDQ_ENDPOINT_VALUE = "http://sedish.net:5001/pdq";

	public static final String REGISTRATIONCORE_MPI_PDQ_IDENTIFIERTYPEUUIDLIST = "registrationcore.mpi.pdqIdentifierTypeUuidList";

	public static final String REGISTRATIONCORE_MPI_PDQ_PORT = "registrationcore.mpi.pdqPort";

	public static final String REGISTRATIONCORE_MPI_PDQ_PORT_VALUE = "3600";

	public static final String REGISTRATIONCORE_MPI_PERSONIDENTIFIERTYPEUUID = "registrationcore.mpi.personIdentifierTypeUuid";

	public static final String REGISTRATIONCORE_MPI_PIX_ENDPOINT = "registrationcore.mpi.pixEndpoint";

	public static final String REGISTRATIONCORE_MPI_PIX_ENDPOINT_VALUE = "http://sedish.net:5001/pix";

	public static final String REGISTRATIONCORE_MPI_PIX_IDENTIFIERTYPEUUIDLIST = "registrationcore.mpi.pixIdentifierTypeUuidList";

	public static final String REGISTRATIONCORE_MPI_PIX_PORT = "registrationcore.mpi.pixPort";

	public static final String REGISTRATIONCORE_MPI_PIX_PORT_VALUE = "3700";

	public static final String REGISTRATIONCORE_MPI_SENDINGAPPLICATION = "registrationcore.mpi.sendingApplication";

	public static final String REGISTRATIONCORE_MPI_SENDINGAPPLICATION_VALUE = "iSantePlus";

	public static final String REGISTRATIONCORE_MPI_SENDINGFACILITY = "registrationcore.mpi.sendingFacility";

	public static final String REGISTRATIONCORE_MPI_SENDINGFACILITY_VALUE = "Demo";

	public static final String REGISTRATIONCORE_MPI_RECEIVINGAPPLICATION = "registrationcore.mpi.receivingApplication";

	public static final String REGISTRATIONCORE_MPI_RECEIVINGAPPLICATION_VALUE = "SEDISH Demo MPI";

	public static final String REGISTRATIONCORE_MPI_RECEIVINGFACILITY = "registrationcore.mpi.receivingFacility";

	public static final String REGISTRATIONCORE_MPI_RECEIVINGFACILITY_VALUE = "OpenEMPI Demo";

	public static final String REGISTRATIONCORE_MPI_USERNAME = "registrationcore.mpi.username";

	public static final String REGISTRATIONCORE_MPI_USERNAME_VALUE = "pixc";

	// XDS-Sender global properties for interacting with the SEDISH CR Demo

	/*
	public static final String XDSSENDER_REPOSITORY_ENDPOINT = "xdssender.repositoryEndpoint";

	public static final String XDSSENDER_REPOSITORY_ENDPOINT_VALUE = "http://sedish.net:5001/xdsrepository";

	public static final String XDSSENDER_REPOSITORY_USERNAME = "xdssender.xdsrepository.username";

	public static final String XDSSENDER_REPOSITORY_USERNAME_VALUE = "xds";

	public static final String XDSSENDER_REPOSITORY_PASSWORD = "xdssender.xdsrepository.password";

	public static final String XDSSENDER_REPOSITORY_PASSWORD_VALUE = "1234";
	*/


}


