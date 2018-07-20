package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.openmrs.Patient;
import org.openmrs.event.Event;

import javax.jms.Message;
import org.openmrs.module.registrationcore.api.errorhandling.ErrorHandlingService;
import org.openmrs.module.registrationcore.api.errorhandling.PixErrorHandlingService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class listen for patient edit event.
 * If MPI is enabled it performs update patient in MPI.
 */
public class PatientUpdatedListener extends PatientActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatientUpdatedListener.class);

    /**
     * @return a list of Actions this listener can deal with
     */
    public List<String> subscribeToActions(){
        List actions = new ArrayList<String>();
        actions.add(Event.Action.UPDATED.name());
        return actions;
    }

    /**
     * Update patient at MPI server.
     *
     * @param message message with properties.
     */
    @Override
    public void performMpiAction(Message message) {
        Patient patient = extractPatient(message);
        // if patient voided is true then don't update the MPI
        if (patient.getVoided()){
            return;
        }else{
            try {
                coreProperties.getMpiProvider().updatePatient(patient);
            } catch (Exception e) {
                ErrorHandlingService errorHandler = coreProperties.getPixErrorHandlingService();
                if (errorHandler == null) {
                    throw new MpiException("PIX patient update exception occurred "
                            + "with not configured PIX error handler", e);
                } else {
                    LOGGER.error("PIX patient update exception occurred", e);
                    errorHandler.handle(prepareParameters(patient),
                            PixErrorHandlingService.SENDING_PATIENT_AFTER_PATIENT_UPDATE_DESTINATION,
                            true,
                            ExceptionUtils.getFullStackTrace(e));
                }
            }
        }
    }
}
