package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedList;
import java.util.List;

public class PatientIdentifierMapper {

    private final Log log = LogFactory.getLog(this.getClass());
    //first element - local, second element - mpi, third element - type
    private List<IdentifierTypeMap> MAPPED_ID;

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    public String getMappedLocalIdentifierTypeUuid(String mpiIdentifierTypeId) {
        validateInit();
        for (IdentifierTypeMap typeMap : MAPPED_ID) {
            if (typeMap.mpiIdentifierId.equals(mpiIdentifierTypeId)) {
                log.info("mpiIdentifierTypeId " + mpiIdentifierTypeId + " " +
                        "properly mapped for "+ typeMap.localIdentifierUuid);
                return typeMap.localIdentifierUuid;
            }
        }
        throw new MpiException("No proper mapping found for mpiIdentifierTypeId=" + mpiIdentifierTypeId + ". " +
                "Check Local Identifier Uuid value for your mapping in global properties.");
    }

    public String getMappedMpiIdentifierTypeId(String localIdentifierTypeUuid) {
        validateInit();
        for (IdentifierTypeMap typeMap : MAPPED_ID) {
            if (typeMap.localIdentifierUuid.equals(localIdentifierTypeUuid)) {
                log.info("localIdentifierTypeUuid " + localIdentifierTypeUuid + " " +
                        "properly mapped for "+ typeMap.mpiIdentifierId);
                return typeMap.mpiIdentifierId;
            }
        }
        throw new MpiException("No proper mapping found for localIdentifierTypeUuid=" + localIdentifierTypeUuid + ". " +
                "Check Mpi Identifier Id value for your mapping in global properties.");
    }

    public String getMappedMpiUniversalIdType(String localIdentifierTypeUuid) {
        validateInit();
        for (IdentifierTypeMap typeMap : MAPPED_ID) {
            if (typeMap.localIdentifierUuid.equals(localIdentifierTypeUuid)) {
                log.info("localIdentifierTypeUuid " + localIdentifierTypeUuid + " " +
                        "properly mapped for "+ typeMap.universalIdType);
                return typeMap.universalIdType;
            }
        }
        throw new MpiException("No proper mapping found for localIdentifierTypeUuid=" + localIdentifierTypeUuid + ". " +
                "Check Universal Id Type value for your mapping in global properties.");
    }

    private void validateInit() {
        if (MAPPED_ID == null) {
            init();
        }
    }

    public void init() {
        MAPPED_ID = new LinkedList<IdentifierTypeMap>();
        log.info("start init method of PatientIdentifierMapper");
        List<String> properties = mpiProperties.getLocalMpiIdentifierTypeMap();
        for (String mappedIdentifiers : properties) {
            IdentifierTypeMap typeMap = parseIdentifiers(mappedIdentifiers);
            MAPPED_ID.add(typeMap);

            log.info("Initialized identifier type map - Local: " + typeMap.localIdentifierUuid
                    + " , MPI: " + typeMap.mpiIdentifierId + " , Type: " + typeMap.universalIdType);
        }
    }

    private IdentifierTypeMap parseIdentifiers(String mappedIdentifiers) {
        String local = getLocalPart(mappedIdentifiers);
        String mpi = getMpiPart(mappedIdentifiers);
        String type = getIdTypePart(mappedIdentifiers);
        return createTypeMap(local, mpi, type);
    }

    private String getLocalPart(String mappedIdentifiers) {
        String parts[] = mappedIdentifiers.split(":");
        if (parts.length > 0) {
            return parts[0];
        }
        return null;
    }

    private String getMpiPart(String mappedIdentifiers) {
        String parts[] = mappedIdentifiers.split(":");
        if (parts.length > 1) {
            return parts[1];
        }
        return null;
    }

    private String getIdTypePart(String mappedIdentifiers) {
        String parts[] = mappedIdentifiers.split(":");
        if (parts.length > 2) {
            return parts[2];
        }
        return null;
    }

    private IdentifierTypeMap createTypeMap(String localString, String mpiString, String universalIdType) {
        try {
            return new IdentifierTypeMap(localString, mpiString, universalIdType);
        } catch (NumberFormatException e) {
            throw new APIException("Can't create identifier type map for values: local= " +
                    localString + ", mpi=" + mpiString + ", type=" + universalIdType);
        }
    }

    private class IdentifierTypeMap {

        public final String localIdentifierUuid;

        public final String mpiIdentifierId;

        public final String universalIdType;

        public IdentifierTypeMap(String localIdentifierUuid, String mpiIdentifierId, String universalIdType) {
            this.localIdentifierUuid = localIdentifierUuid;
            this.mpiIdentifierId = mpiIdentifierId;
            this.universalIdType = universalIdType;
        }
    }
}