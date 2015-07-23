package org.openmrs.module.registrationcore.api.mpi.openempi;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "person")
public class OpenEmpiPatientQuery {

    private Integer personId;

    private String familyName;

    private String givenName;

    private String middleName;

    private Date dateOfBirth;

    private String address1;

    private Gender gender;

    private List<PersonIdentifier> personIdentifiers;

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
    public List<PersonIdentifier> getPersonIdentifiers() {
        return personIdentifiers;
    }

    public void setPersonIdentifiers(List<PersonIdentifier> personIdentifiers) {
        this.personIdentifiers = personIdentifiers;
    }
}

@XmlRootElement
class Gender{

    private String genderCode;

    public String getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(String genderCode) {
        this.genderCode = genderCode;
    }
}

@XmlRootElement(name = "personIdentifiers")
class PersonIdentifier {
    private String identifier;

    private Integer personIdentifierId;

    private IdentifierDomain identifierDomain;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getPersonIdentifierId() {
        return personIdentifierId;
    }

    public void setPersonIdentifierId(Integer personIdentifierId) {
        this.personIdentifierId = personIdentifierId;
    }

    public IdentifierDomain getIdentifierDomain() {
        return identifierDomain;
    }

    public void setIdentifierDomain(IdentifierDomain identifierDomain) {
        this.identifierDomain = identifierDomain;
    }
}
@XmlRootElement
class IdentifierDomain {
    private Integer identifierDomainId;

    private String identifierDomainName;

    private String namespaceIdentifier;

    private String universalIdentifier;

    private String universalIdentifierTypeCode;

    public Integer getIdentifierDomainId() {
        return identifierDomainId;
    }

    public void setIdentifierDomainId(Integer identifierDomainId) {
        this.identifierDomainId = identifierDomainId;
    }

    public String getIdentifierDomainName() {
        return identifierDomainName;
    }

    public void setIdentifierDomainName(String identifierDomainName) {
        this.identifierDomainName = identifierDomainName;
    }

    public String getNamespaceIdentifier() {
        return namespaceIdentifier;
    }

    public void setNamespaceIdentifier(String namespaceIdentifier) {
        this.namespaceIdentifier = namespaceIdentifier;
    }

    public String getUniversalIdentifier() {
        return universalIdentifier;
    }

    public void setUniversalIdentifier(String universalIdentifier) {
        this.universalIdentifier = universalIdentifier;
    }

    public String getUniversalIdentifierTypeCode() {
        return universalIdentifierTypeCode;
    }

    public void setUniversalIdentifierTypeCode(String universalIdentifierTypeCode) {
        this.universalIdentifierTypeCode = universalIdentifierTypeCode;
    }
}