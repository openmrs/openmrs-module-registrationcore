package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PatientIdentifierMapperTest {

    private static final String PROPERTY = "1:2";
    private static final Integer PROPERTY_LOCAL_PART = 1;
    private static final Integer PROPERTY_MPI_PART = 2;

    @InjectMocks
    private PatientIdentifierMapper identifierMapper;
    @Mock
    private MpiProperties mpiProperties;

    @Before
    public void setUp() throws Exception {
        identifierMapper = new PatientIdentifierMapper();
        MockitoAnnotations.initMocks(this);
        mockMpiProperties();
    }

    private void mockMpiProperties() {
        when(mpiProperties.getLocalMpiIdentifierTypeMap()).thenReturn(Collections.singletonList(PROPERTY));
    }

    @Test
    public void testInit() throws Exception {
        identifierMapper.init();
        verify(mpiProperties).getLocalMpiIdentifierTypeMap();
    }

    @Test
    public void testReturnCorrectMpiIdentifierTypeId() throws Exception {
        identifierMapper.init();

        String mpiIdentifierTypeId = identifierMapper.getMappedMpiIdentifierTypeId(PROPERTY_LOCAL_PART.toString());

        assertEquals(mpiIdentifierTypeId, PROPERTY_MPI_PART.toString());
    }

    @Test
    public void testReturnCorrectLocalIdentifierTypeId() throws Exception {
        identifierMapper.init();

        String localIdentifierTypeId = identifierMapper.getMappedLocalIdentifierTypeUuid(PROPERTY_MPI_PART.toString());

        assertEquals(localIdentifierTypeId, PROPERTY_LOCAL_PART.toString());
    }
}