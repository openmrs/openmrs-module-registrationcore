package org.openmrs.module.registrationcore.api.impl;

import javax.jms.Message;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.errorhandling.ErrorHandlingService;
import org.openmrs.module.registrationcore.api.errorhandling.PixErrorHandlingService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class listen for patient creation event.
 * If MPI is enabled it perform export patient to MPI,
 * and perform update for local patient with new one patient identifier.
 */
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
     * @return a list of Actions this listener can deal with
     */
    public List<String> subscribeToActions(){
        List actions = new ArrayList<String>();
        actions.add(Event.Action.CREATED.name());
        return actions;
    }

    /**
     * Export patient to MPI server & update local
     * patient with new one patient identifier.
     *
     * @param message message with properties.
     */
    @Override
    public void performMpiAction(Message message) {
        Patient patient = extractPatient(message);
        if (patient != null){
            PatientIdentifierType mpiPatientIdType = patientService.getPatientIdentifierTypeByUuid(mpiProperties.getMpiPersonIdentifierTypeUuid());
            PatientIdentifier mpiPatientId = patient.getPatientIdentifier(mpiPatientIdType);
            if (mpiPatientId != null && !mpiPatientId.getIdentifier().isEmpty()){
                // Patient originated in MPI and thus should not be pushed to the MPI
                return;
            }else{
                String mpiPatientIdString = null;
                try {
                    mpiPatientIdString = pushPatientToMpiServer(patient);
                } catch (Exception e) {
                    ErrorHandlingService errorHandler = coreProperties.getPixErrorHandlingService();
                    if (errorHandler == null) {
                        throw new MpiException("PIX patient push exception occurred "
                                + "with not configured PIX error handler", e);
                    } else {
                        errorHandler.handle(prepareParameters(patient),
                                PixErrorHandlingService.SENDING_PATIENT_AFTER_PATIENT_CREATION_DESTINATION,
                                true,
                                ExceptionUtils.getFullStackTrace(e));
                        throw new MpiException("PIX patient push exception occurred", e);
                    }
                }
                if (mpiPatientIdString != null) {
                    updatePatient(patient, mpiPatientIdString);
                }
            }
        }else{
            throw new MpiException("PeformMpiAction error: extractPatient returned null patient");
        }
    }

    private String pushPatientToMpiServer(Patient patient) {
        return coreProperties.getMpiProvider().exportPatient(patient);
    }

    private void updatePatient(final Patient patient, final String mpiPatientId) {
        User creator = patient.getCreator();
        patient.addIdentifier(createPatientIdentifier(creator, mpiPatientId));
        patientService.savePatient(patient);
    }

    private PatientIdentifier createPatientIdentifier(User creator, String mpiPatientId) {
        PatientIdentifier mpiPatientIdentifier = identifierBuilder
                .createIdentifier(mpiProperties.getMpiPersonIdentifierTypeUuid(), mpiPatientId, null);
        mpiPatientIdentifier.setCreator(creator);
        return mpiPatientIdentifier;
    }
}
