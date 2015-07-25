package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


public class OpenEmpiAuthenticatorTest {

    private static final String MPI_ACCESS_USERNAME = "username";
    private static final String MPI_ACCESS_PASSWORD = "password";
    private static final String TOKEN_VALUE = "NotEmptyTokenValue";
    @Mock private RestQueryCreator queryCreator;
    @Mock private AdministrationService adminService;
    @InjectMocks private OpenEmpiAuthenticator authenticator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReturnTrueIfTokenExist() throws Exception {
        mockTokenValue();

        boolean authenticated = authenticator.isAuthenticated();

        assertTrue(authenticated);
    }

    @Test
    public void testReturnFalseIfTokenIsNull() throws Exception {
        mockTokenEmpty();

        boolean authenticated = authenticator.isAuthenticated();

        assertFalse(authenticated);
    }

    @Test
    public void testProcessAuthentication() throws Exception {
        mockValidCredentials();

        authenticator.performAuthentication();

        verify(queryCreator).processAuthentication(eq(new MpiCredentials(MPI_ACCESS_USERNAME, MPI_ACCESS_PASSWORD)));
    }

    @Test(expected = APIException.class)
    public void testThrowExceptionOnInvalidCredentials() throws Exception {
        mockInvalidCredentials();

        authenticator.performAuthentication();
    }

    @Test
    public void testPerformAuthenticationIfTokenIsNull() throws Exception {
        mockValidCredentials();
        authenticator.getToken();

        verify(queryCreator).processAuthentication(any(MpiCredentials.class));
    }

    @Test
    public void testDoNotPerformAuthenticationIfTokenIsNull() throws Exception {
        mockTokenValue();
        mockValidCredentials();
        String actualToken = authenticator.getToken();

        verify(queryCreator, never()).processAuthentication(any(MpiCredentials.class));
        assertEquals(TOKEN_VALUE, actualToken);
    }

    private void mockValidCredentials() {
        when(adminService.getGlobalProperty(RegistrationCoreConstants.GP_MPI_ACCESS_USERNAME)).thenReturn(MPI_ACCESS_USERNAME);
        when(adminService.getGlobalProperty(RegistrationCoreConstants.GP_MPI_ACCESS_PASSWORD)).thenReturn(MPI_ACCESS_PASSWORD);
    }

    private void mockInvalidCredentials() {
        when(adminService.getGlobalProperty(RegistrationCoreConstants.GP_MPI_ACCESS_USERNAME)).thenReturn(null);
        when(adminService.getGlobalProperty(RegistrationCoreConstants.GP_MPI_ACCESS_PASSWORD)).thenReturn("");
    }

    private void mockTokenValue() {
        ReflectionTestUtils.setField(authenticator, "token", TOKEN_VALUE);
    }

    private void mockTokenEmpty() {
        ReflectionTestUtils.setField(authenticator, "token", null);
    }
}