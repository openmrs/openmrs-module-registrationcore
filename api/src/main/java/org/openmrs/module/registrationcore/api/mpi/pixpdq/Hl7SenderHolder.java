package org.openmrs.module.registrationcore.api.mpi.pixpdq;


import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class Hl7SenderHolder {

    @Autowired
    private ApplicationContext context;

    public Hl7v2Sender getHl7v2Sender() {
        String propertyName = RegistrationCoreConstants.GP_MPI_HL7_IMPLEMENTATION;
        Object bean;
        try {
            String beanId = Context.getAdministrationService().getGlobalProperty(propertyName);
            bean = context.getBean(beanId);
        } catch (APIException e) {
            return null;
        }
        if (!(bean instanceof Hl7v2Sender))
            throw new IllegalArgumentException(propertyName
                    + " must point to bean implementing Hl7v2Sender");

        return (Hl7v2Sender) bean;
    }
}
