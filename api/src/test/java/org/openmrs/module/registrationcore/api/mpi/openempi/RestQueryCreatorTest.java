package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;

import java.util.HashSet;

public class RestQueryCreatorTest {

    private RestQueryCreator restQueryCreator;

    @Before
    public void setUp() throws Exception {
        restQueryCreator = createRequestCreator();
    }

    //Used for development
    //@Test
    public void testCorrectImport() throws Exception {
        restQueryCreator.getPatientById("2");
    }

    //Used for development
    //@Test
    public void testCorrectFindPatients() throws Exception {
        Patient patient = new Patient();
        HashSet<PersonName> names = new HashSet<PersonName>();
        PersonName name = new PersonName("Roman", null, "Zayats");
        names.add(name);
        patient.setNames(names);

        restQueryCreator.findPatients(PatientQueryMapper.convert(patient));
    }

    private RestQueryCreator createRequestCreator() {
        RestQueryCreator queryCreator = new RestQueryCreator();
        MpiCredentials credentials = createCredentials();
        queryCreator.setCredentials(credentials);
        return queryCreator;
    }

    private MpiCredentials createCredentials() {
        MpiCredentials mpiCredentials = new MpiCredentials("admin", "admin123");
        mpiCredentials.setToken("DCBE246033E134DE2CA58163C7F5A1E6");
        return mpiCredentials;
    }
}