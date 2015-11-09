package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedList;
import java.util.List;

public class PatientIdentifierMapper {

    private final Log log = LogFactory.getLog(this.getClass());
    private static final String SPLITTER_SIGN = ":";
    //first element ("key") - local, second element ("value") - mpi.
    private static List<IdentifierMapPair> MAPPED_ID;

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    public Integer getMappedLocalIdentifierTypeId(Integer mpiIdentifierTypeId) {
        validateInit();
        for (IdentifierMapPair pair : MAPPED_ID) {
            if (pair.mpiIdentifierId.equals(mpiIdentifierTypeId))
                return pair.localIdentifierId;
        }
        return null;
    }

    public Integer getMappedMpiIdentifierTypeId(Integer localIdentifierTypeId) {
        validateInit();
        for (IdentifierMapPair pair : MAPPED_ID) {
            if (pair.localIdentifierId.equals(localIdentifierTypeId))
                return pair.mpiIdentifierId;
        }
        return null;
    }

    private void validateInit() {
        if (MAPPED_ID == null) {
            init();
        }
    }

    public void init() {
        MAPPED_ID = new LinkedList<IdentifierMapPair>();
        log.info("start init method of PatientIdentifierMapper");
        List<String> properties = mpiProperties.getLocalMpiIdentifierTypeMap();
        for (String mappedIdentifiers : properties) {
            IdentifierMapPair pair = parseIdentifiers(mappedIdentifiers);
            MAPPED_ID.add(pair);

            log.info("Initialized local:mpi identifier type pair. Local: " + pair.localIdentifierId
                    + " , MPI: " + pair.mpiIdentifierId);
        }
    }

    private IdentifierMapPair parseIdentifiers(String mappedIdentifiers) {
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

    private IdentifierMapPair createPair(String localString, String mpiString) {
        try {
            Integer local = Integer.valueOf(localString);
            Integer mpi = Integer.valueOf(mpiString);
            return  new IdentifierMapPair(local, mpi);
        } catch (NumberFormatException e) {
            throw new APIException("Can't create identifier pair for values: local= " +
                    localString + ", mpi=" + mpiString);
        }
    }

    private class IdentifierMapPair{

        public final Integer localIdentifierId;

        public final Integer mpiIdentifierId;

        public IdentifierMapPair(Integer localIdentifierId, Integer mpiIdentifierId) {
            this.localIdentifierId = localIdentifierId;
            this.mpiIdentifierId = mpiIdentifierId;
        }
    }
}