package org.openmrs.module.registrationcore.api.mpi.common;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.RegistrationCoreSensitiveTestBase;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;

public class MpiFacadeImplTest extends RegistrationCoreSensitiveTestBase {

    private static final String PATIENT_GIVEN_NAME = "Roman";
    private static final String PATIENT_FAMILY_NAME = "Zayats";
    @Autowired
    private MpiFacade mpiFacade;

    @Test
    public void testPerformCorrectSearchSimilar() throws Exception {
        Patient patient = createPatient();
        List<PatientAndMatchQuality> similarPatients = mpiFacade.findSimilarPatients(patient, null, null, 10);
        System.out.println(similarPatients.size());
    }

    @Test
    public void testPerformCorrectImport() throws Exception {
        MpiPatient mpiPatient = mpiFacade.importMpiPatient("2");
        System.out.println(mpiPatient.getFamilyName());
    }

    private Patient createPatient() {
        Patient patient = new Patient();
        HashSet<PersonName> names = new HashSet<PersonName>();
        PersonName name = new PersonName(PATIENT_GIVEN_NAME, null, PATIENT_FAMILY_NAME);
        names.add(name);
        patient.setNames(names);
        return patient;
    }
}