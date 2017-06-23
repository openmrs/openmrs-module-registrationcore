package org.openmrs.module.registrationcore.model;

public class PatientSearchModel {

    // Family name
    private String familyName;
    // Given name
    private String givenName;

    private String identifier;
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

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
