package org.openmrs.module.registrationcore.api.mpi.openempi;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "person")
public class OpenEmpiPatientQuery {

    private Integer personId;

    private String familyName;

    private String givenName;

    private String middleName;

    private Date dateOfBirth;

    private String address1;

    private Gender gender;

    private PersonIdentifiers personIdentifiers;

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    @XmlElement
    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @XmlElement
    public PersonIdentifiers getPersonIdentifiers() {
        return personIdentifiers;
    }

    public void setPersonIdentifiers(PersonIdentifiers personIdentifiers) {
        this.personIdentifiers = personIdentifiers;
    }
}

@XmlRootElement
class Gender{

    private String genderName;

    public String getGenderName() {
        return genderName;
    }

    public void setGenderName(String genderName) {
        this.genderName = genderName;
    }
}

@XmlRootElement
class PersonIdentifiers{

    private String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}