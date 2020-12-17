package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.mpi.fhir.FhirSimilarPatientsSearcher;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class FhirSimilarPatientsSearcherTest extends BaseModuleContextSensitiveTest {
    private List<Patient> RET_VAL = new ArrayList<Patient>();

    @InjectMocks
    private FhirSimilarPatientsSearcher fhirSimilarPatientsSearcher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initData();
    }

    private void initData(){
        Patient patient = new Patient();
        patient.addName(new PersonName("Johny", null, "Smith"));
        Date date = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
        patient.setBirthdate(date);
        patient.setGender("M");
        PersonAddress personAddress = new PersonAddress();
        personAddress.setCountry("TesT");
        personAddress.setCityVillage("TeSt2");
        personAddress.setCountyDistrict("Test3");
        patient.addAddress(personAddress);
        RET_VAL.add(patient);
    }



    @Test
    public void findSimilarMatches() throws Exception {
        Patient patient = new Patient();
        patient.addName(new PersonName("Johny",null,"Smith"));

        List<PatientAndMatchQuality> result = fhirSimilarPatientsSearcher.findSimilarMatches(patient, null, null, 10);
        assertEquals(2, result.get(0).getMatchedFields().size());

        Patient patient2 = new Patient();
        patient2.addName(new PersonName("Johny", null, "Smith"));
        Date date = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
        patient2.setBirthdate(date);

        result = fhirSimilarPatientsSearcher.findSimilarMatches(patient2, null, null, 10);
        assertEquals(3, result.get(0).getMatchedFields().size());

        Patient patient3 = new Patient();
        patient3.addName(new PersonName("Tom", null, "Smith"));
        date = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
        patient3.setBirthdate(date);
        patient3.setGender("M");
        PersonAddress personAddress = new PersonAddress();
        personAddress.setCountry("TesT");
        personAddress.setCityVillage("TeSt2");
        personAddress.setCountyDistrict("Test3");
        patient3.addAddress(personAddress);

        result = fhirSimilarPatientsSearcher.findSimilarMatches(patient3, null, null, 10);
        assertEquals(6, result.get(0).getMatchedFields().size());
    }

}