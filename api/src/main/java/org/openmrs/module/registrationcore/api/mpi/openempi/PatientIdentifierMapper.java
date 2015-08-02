package org.openmrs.module.registrationcore.api.mpi.openempi;

import javafx.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedList;
import java.util.List;

public class PatientIdentifierMapper {

    private final Log log = LogFactory.getLog(this.getClass());

    //first element ("key") - local, second element ("value") - mpi.
    private static final List<Pair<Integer, Integer>> MAPPED_ID = new LinkedList<Pair<Integer, Integer>>();
    private static final String SPLITTER_SIGN = ":";

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    public void init() {
        List<String> properties = mpiProperties.getLocalMpiIdentifierTypeMap();
        for (String mappedIdentifiers : properties) {
            Pair<Integer, Integer> pair = parseIdentifiers(mappedIdentifiers);
            MAPPED_ID.add(pair);

            log.error("Initialized local:mpi identifier type pair. Local: " + pair.getKey()
                    + " , MPI: " + pair.getValue());
        }
    }

    private Pair<Integer, Integer> parseIdentifiers(String mappedIdentifiers) {
        String local = getLocalPart(mappedIdentifiers);
        String mpi = getMpiPart(mappedIdentifiers);
        return createPair(local, mpi);
    }

    private String getLocalPart(String mappedIdentifiers) {
        return mappedIdentifiers.substring(0, mappedIdentifiers.indexOf(SPLITTER_SIGN));
    }

    private String getMpiPart(String mappedIdentifiers) {
        return mappedIdentifiers.substring(mappedIdentifiers.indexOf(SPLITTER_SIGN) + 1);
    }

    private Pair<Integer, Integer> createPair(String localString, String mpiString) {
        try {
            Integer local = Integer.valueOf(localString);
            Integer mpi = Integer.valueOf(mpiString);
            return new Pair<Integer, Integer>(local, mpi);
        } catch (NumberFormatException e) {
            throw new APIException("Can't create identifier pair for values: local= " +
                    localString + ", mpi=" + mpiString);
        }
    }

    public Integer getMappedLocalIdentifierTypeId(Integer mpiIdentifierTypeId) {
        for (Pair<Integer, Integer> pair : MAPPED_ID) {
            if (pair.getValue().equals(mpiIdentifierTypeId))
                return pair.getKey();
        }
        return null;
    }

    public Integer getMappedMpiIdentifierTypeId(Integer localIdentifierTypeId) {
        for (Pair<Integer, Integer> pair : MAPPED_ID) {
            if (pair.getKey().equals(localIdentifierTypeId))
                return pair.getValue();
        }
        return null;
    }
}