package org.openmrs.module.registrationcore.api.mpi.openempi;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "people")
public class OpenEmpiPeopleWrapper {
	
	private List<OpenEmpiPatientResult> people;
	
	@XmlElement(name = "person")
	public List<OpenEmpiPatientResult> getPeople() {
		return people;
	}
	
	public void setPeople(List<OpenEmpiPatientResult> people) {
		this.people = people;
	}
}
