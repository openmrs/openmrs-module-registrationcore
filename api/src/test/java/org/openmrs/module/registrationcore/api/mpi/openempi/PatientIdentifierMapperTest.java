package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PatientIdentifierMapperTest {

    private static final String PROPERTY = "1:2";
    private static final Integer PROPERTY_LOCAL_PART = 1;
    private static final Integer PROPERTY_MPI_PART = 2;

    @InjectMocks private PatientIdentifierMapper identifierMapper;
    @Mock private AdministrationService administrationService;

    @Before
    public void setUp() throws Exception {
        identifierMapper = new PatientIdentifierMapper();
        MockitoAnnotations.initMocks(this);
        mockAdminService();
    }

    @Test
    public void testInit() throws Exception {
        identifierMapper.init();
        verify(administrationService).getGlobalPropertiesByPrefix(RegistrationCoreConstants.GP_LOCAL_MPI_IDENTIFIER_TYPE_MAP);
    }

    @Test
    public void testReturnCorrectMpiIdentifierTypeId() throws Exception {
        identifierMapper.init();

        Integer mpiIdentifierTypeId = identifierMapper.getMappedMpiIdentifierTypeId(PROPERTY_LOCAL_PART);

        assertEquals(mpiIdentifierTypeId, PROPERTY_MPI_PART);
    }

    @Test
    public void testReturnCorrectLocalIdentifierTypeId() throws Exception {
        identifierMapper.init();

        Integer localIdentifierTypeId = identifierMapper.getMappedLocalIdentifierTypeId(PROPERTY_MPI_PART);

        assertEquals(localIdentifierTypeId, PROPERTY_LOCAL_PART);
    }

    private void mockAdminService() {
        List<GlobalProperty> globalProperties = new LinkedList<GlobalProperty>();

        GlobalProperty property1 = mock(GlobalProperty.class);
        when(property1.getPropertyValue()).thenReturn(PROPERTY);
        globalProperties.add(property1);

        when(administrationService.getGlobalPropertiesByPrefix(RegistrationCoreConstants.GP_LOCAL_MPI_IDENTIFIER_TYPE_MAP))
                .thenReturn(globalProperties);
    }
}