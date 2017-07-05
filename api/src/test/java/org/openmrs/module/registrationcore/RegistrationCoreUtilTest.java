package org.openmrs.module.registrationcore;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class RegistrationCoreUtilTest {
	
	private AdministrationService administrationService;
	
	@Before
	public void setUp() {
		PowerMockito.mockStatic(Context.class);
		when(Context.getAdministrationService()).thenReturn(mock(AdministrationService.class));
		
		administrationService = Context.getAdministrationService();
		when(administrationService.getGlobalProperty(RegistrationCoreConstants.GP_BIRTHDATE_ESTIMATION_START_MONTH)).thenReturn("");
	}
	
	@Test
	public void calculateBirthdateFromAge_shouldUseGPMonthWhenCalculateProperBirthdateWhenYearsSpecified() {
		
		when(administrationService.getGlobalProperty(RegistrationCoreConstants.GP_BIRTHDATE_ESTIMATION_START_MONTH)).thenReturn("6");
		
		DateTime ageOnDate = new DateTime(2015, 04, 03, 0, 0, 0);
		DateTime result = new DateTime(RegistrationCoreUtil.calculateBirthdateFromAge(10, null, null, ageOnDate.toDate()));
		
		assertThat(result.getYear(), is(2005));
		assertThat(result.getMonthOfYear(), is(7));
		assertThat(result.getDayOfMonth(), is(1));
	}
	
    @Test
    public void calculateBirthdateFromAge_shouldCalculateProperBirthdateWhenYearsSpecified(){
        DateTime ageOnDate= new DateTime(2015, 04, 03, 0, 0, 0);
        DateTime result = new DateTime(RegistrationCoreUtil.calculateBirthdateFromAge(10, null, null, ageOnDate.toDate()));
        assertThat(result.getYear(), is(2005));
        assertThat(result.getMonthOfYear(), is(1));
        assertThat(result.getDayOfMonth(), is(1));
    }

    @Test
    public void calculateBirthdateFromAge_shouldCalculateProperBirthdateWhenYearAndMonthSpecified(){
        DateTime ageOnDate= new DateTime(2015, 04, 03, 0, 0, 0);
        DateTime result = new DateTime(RegistrationCoreUtil.calculateBirthdateFromAge(10, 2, null, ageOnDate.toDate()));
        assertThat(result.getYear(), is(2005));
        assertThat(result.getMonthOfYear(), is(2));
        assertThat(result.getDayOfMonth(), is(1));
    }

    @Test
    public void calculateBirthdateFromAge_shouldCalculateProperBirthdateWhenYearAndMonthAndDaySpecified(){
        DateTime ageOnDate= new DateTime(2015, 04, 03, 0, 0, 0);
        DateTime result = new DateTime(RegistrationCoreUtil.calculateBirthdateFromAge(10, 2, 1, ageOnDate.toDate()));
        assertThat(result.getYear(), is(2005));
        assertThat(result.getMonthOfYear(), is(2));
        assertThat(result.getDayOfMonth(), is(2));
    }

    @Test
    public void calculateBirthdateFromAge_shouldCalculateProperBirthdateWhenMonthSpecified(){
        DateTime ageOnDate= new DateTime(2015, 04, 03, 0, 0, 0);
        DateTime result = new DateTime(RegistrationCoreUtil.calculateBirthdateFromAge(null, 2, null, ageOnDate.toDate()));
        assertThat(result.getYear(), is(2015));
        assertThat(result.getMonthOfYear(), is(2));
        assertThat(result.getDayOfMonth(), is(1));
    }

    @Test
    public void calculateBirthdateFromAge_shouldCalculateProperBirthdateWhenMonthAndDaySpecified(){
        DateTime ageOnDate= new DateTime(2015, 04, 03, 0, 0, 0);
        DateTime result = new DateTime(RegistrationCoreUtil.calculateBirthdateFromAge(null, 2, 1, ageOnDate.toDate()));
        assertThat(result.getYear(), is(2015));
        assertThat(result.getMonthOfYear(), is(2));
        assertThat(result.getDayOfMonth(), is(2));
    }
}
