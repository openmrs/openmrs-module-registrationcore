package org.openmrs.module.registrationcore.api.impl;

import javax.jms.Message;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Handler;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.api.errorhandling.ErrorHandlingService;
import org.openmrs.module.registrationcore.api.errorhandling.PixErrorHandlingService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class listens for patient CREATED events. If MPI is enabled, it exports patient to MPI, and
 * updates the local patient with the new patient identifier generated by the MPI.
 */
@Handler
public class PatientCreatedListener extends PatientActionListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PatientCreatedListener.class);
	
	private MpiProperties mpiProperties;
	
	private IdentifierBuilder identifierBuilder;
	
	public void setMpiProperties(MpiProperties mpiProperties) {
		this.mpiProperties = mpiProperties;
	}
	
	public void setIdentifierBuilder(IdentifierBuilder identifierBuilder) {
		this.identifierBuilder = identifierBuilder;
	}
	
	/**
	 * Defines the list of Actions that this subscribable event listener class listens out for.
	 * 
	 * @return a list of Actions this listener can deal with
	 */
	public List<String> subscribeToActions() {
		List actions = new ArrayList<String>();
		actions.add(Event.Action.CREATED.name());
		return actions;
	}
	
	/**
	 * Exports patient to MPI, and updates the local patient with the new patient identifier
	 * generated by the MPI.
	 * 
	 * @param message message with properties.
	 */
	@Override
	public void performMpiAction(Message message) {
		Patient patient = extractPatient(message);
		if (patient != null) {
			PatientIdentifierType mpiPatientIdType = patientService.getPatientIdentifierTypeByUuid(mpiProperties
			        .getMpiPersonIdentifierTypeUuid());
			PatientIdentifier mpiPatientId = patient.getPatientIdentifier(mpiPatientIdType);
			if (mpiPatientId != null && StringUtils.isNotBlank(mpiPatientId.getIdentifier())) {
				/* TODO Implement a way to push the locally created Identifiers to the MPI: See Ticket RC-31
				// Patient exists in MPI and thus should not be created in the MPI
				// Check if patient has all local identifiers (that could have just been generated in importMpiPatient)
				MpiPatient mpiPatient = Context.getService(RegistrationCoreService.class).findMpiPatient( mpiPatientId.getIdentifier(),
				                                                                mpiProperties.getMpiPersonIdentifierTypeUuid());
				if (patient.getIdentifiers().size() > mpiPatient.getIdentifiers().size()){
				    // Sending an ADT_08 Update message does not work to update Patient Identifiers
				    // Need to implement a different message type/ solution
				}
				*/
				return;
			} else {
				String mpiPatientIdString = null;
				try {
					mpiPatientIdString = pushPatientToMpiServer(patient);
				}
				catch (Exception e) {
					ErrorHandlingService errorHandler = coreProperties.getPixErrorHandlingService();
					if (errorHandler == null) {
						throw new MpiException("PIX patient push exception occurred "
						        + "with not configured PIX error handler", e);
					} else {
						errorHandler.handle(prepareParameters(patient),
						    PixErrorHandlingService.SENDING_PATIENT_AFTER_PATIENT_CREATION_DESTINATION, true,
						    ExceptionUtils.getFullStackTrace(e));
						throw new MpiException("PIX patient push exception occurred", e);
					}
				}
				if (mpiPatientIdString != null) {
					updatePatient(patient, mpiPatientIdString);
				}
			}
		} else {
			throw new MpiException("PeformMpiAction error: extractPatient returned null patient");
		}
	}
	
	/**
	 * Pushes patient to MPI using MPI provider specified in global properties
	 * 
	 * @param patient patient to be pushed to MPI
	 * @return MPI global identifier of the patient, generated by the MPI
	 */
	private String pushPatientToMpiServer(Patient patient) {
		return coreProperties.getMpiProvider().exportPatient(patient);
	}
	
	/**
	 * Updates patient in local DB with MPI global identifier generated by the MPI.
	 * 
	 * @param patient patient to be updated
	 * @param mpiPatientId MPI global identifier for the provided patient
	 */
	private void updatePatient(final Patient patient, final String mpiPatientId) {
		PatientIdentifier mpiPatientIdentifier = identifierBuilder.createIdentifier(
		    mpiProperties.getMpiPersonIdentifierTypeUuid(), mpiPatientId, null);
		patient.addIdentifier(mpiPatientIdentifier);
		patientService.savePatient(patient);
	}
	
}
