package org.openmrs.module.registrationcore.api.mpi.openempi;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "people")
public class OpenEmpiPeopleWrapper {

    private List<OpenEmpiPatientQuery> people;

    @XmlElement(name = "person")
    public List<OpenEmpiPatientQuery> getPeople() {
        return people;
    }

    public void setPeople(List<OpenEmpiPatientQuery> people) {
        this.people = people;
    }
}
