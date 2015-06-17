package org.openmrs.module.registrationcore.api.mpi;

import org.springframework.stereotype.Service;

import java.util.List;

@Service("registrationcore.EMPISimilarPatientSearch")
public class EMPIPatientNameSearch implements MPIPatientNameSearch {

    @Override
    public List<String> findSimilarGivenNames(String searchPhrase) {
        return null;
    }

    @Override
    public List<String> findSimilarFamilyNames(String searchPhrase) {
        return null;
    }
}
