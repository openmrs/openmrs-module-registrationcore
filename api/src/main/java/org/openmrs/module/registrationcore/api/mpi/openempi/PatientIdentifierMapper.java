package org.openmrs.module.registrationcore.api.mpi.openempi;

import com.sun.tools.javac.util.Pair;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import java.util.LinkedList;
import java.util.List;

public class PatientIdentifierMapper {
    //first element ("key") - local, second element ("value") - mpi.
    private static final List<Pair<Integer, Integer>> MAPPED_ID = new LinkedList<Pair<Integer, Integer>>();
    private static final String SPLITTER_SIGN = ":";

    private AdministrationService administrationService;

    public void setAdministrationService(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    public void init() {
        List<GlobalProperty> properties = administrationService.getGlobalPropertiesByPrefix(RegistrationCoreConstants.GP_LOCAL_MPI_IDENTIFIER_TYPE_MAP);
        for (GlobalProperty property : properties) {
            String mappedIdentifiers = property.getPropertyValue();
            Pair<Integer, Integer> pair = parseIdentifiers(mappedIdentifiers);
            MAPPED_ID.add(pair);
        }
    }

    public Integer getMappedLocalIdentifierTypeId(Integer mpiIdentifierTypeId) {
        for (Pair<Integer, Integer> pair : MAPPED_ID) {
            if (pair.snd.equals(mpiIdentifierTypeId))
                return pair.fst;
        }
        return null;
    }

    public Integer getMappedMpiIdentifierTypeId(Integer localIdentifierTypeId) {
        for (Pair<Integer, Integer> pair : MAPPED_ID) {
            if (pair.fst.equals(localIdentifierTypeId))
                return pair.snd;
        }
        return null;
    }

    private Pair<Integer, Integer> parseIdentifiers(String mappedIdentifiers) {
        String local = getLocalPart(mappedIdentifiers);
        String mpi = getMpiPart(mappedIdentifiers);
        return createPair(local, mpi);
    }

    private Pair<Integer, Integer> createPair(String localString, String mpiString) {
        try {
            Integer local = Integer.valueOf(localString);
            Integer mpi = Integer.valueOf(mpiString);
            return new Pair<Integer, Integer>(local, mpi);
        } catch (NumberFormatException e) {
            throw new APIException("Can't create identifier pair for values: local= " + localString + ", mpi=" + mpiString);
        }
    }

    private String getLocalPart(String mappedIdentifiers) {
        return mappedIdentifiers.substring(0, mappedIdentifiers.indexOf(SPLITTER_SIGN));
    }

    private String getMpiPart(String mappedIdentifiers) {
        return mappedIdentifiers.substring(mappedIdentifiers.indexOf(SPLITTER_SIGN) + 1);
    }
}