package org.openmrs.module.registrationcore;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RegistrationCoreUtilTest {

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
}
