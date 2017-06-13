package org.openmrs.module.registrationcore.model;

public class PatientSearchModel {

    // Family name
    private String familyName;
    // Given name
    private String givenName;
    /**
     * @return the familyName
     */
    public String getFamilyName() {
        return familyName;
    }
    /**
     * @param familyName the familyName to set
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    /**
     * @return the givenName
     */
    public String getGivenName() {
        return givenName;
    }
    /**
     * @param givenName the givenName to set
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

}
