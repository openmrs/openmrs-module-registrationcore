package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientsSearcher;
import org.openmrs.module.registrationcore.api.mpi.pixpdq.PixPdqMessageUtil;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OpenEmpiPatientsSearcher implements MpiSimilarPatientsSearcher {
	
	@Autowired
	@Qualifier("registrationcore.restQueryExecutor")
	private RestQueryExecutor queryExecutor;
	
	@Autowired
	@Qualifier("registrationcore.patientBuilder")
	private PatientBuilder patientBuilder;
	
	@Autowired
	@Qualifier("registrationcore.mpiAuthenticator")
	private MpiAuthenticator authenticator;
	
	private FindPatientQueryBuilder queryMapper = new FindPatientQueryBuilder();
	
	@Autowired
	@Qualifier("registrationcore.mpiPixPdqMessageUtil")
	private PixPdqMessageUtil pixPdqMessageUtil;
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public List<PatientAndMatchQuality> findSimilarMatches(Patient patient, Map<String, Object> otherDataPoints,
	        Double cutoff, Integer maxResults) {
		OpenEmpiPatientResult patientQuery = queryMapper.create(patient);
		
		List<OpenEmpiPatientResult> mpiPatients = queryExecutor.findProbablySimilarPatients(authenticator.getToken(),
		    patientQuery);
		
		mpiPatients = limitResults(maxResults, mpiPatients);
		
		return convertMpiPatients(mpiPatients, cutoff);
	}
	
	@Override
	public List<PatientAndMatchQuality> findExactMatches(Patient patient, Map<String, Object> otherDataPoints,
	        Double cutoff, Integer maxResults) {
		OpenEmpiPatientResult patientQuery = queryMapper.create(patient);
		
		List<OpenEmpiPatientResult> mpiPatients = queryExecutor.findPreciseSimilarPatients(authenticator.getToken(),
		    patientQuery);
		
		mpiPatients = limitResults(maxResults, mpiPatients);
		
		return convertMpiPatients(mpiPatients, cutoff);
	}
	
	private List<PatientAndMatchQuality> convertMpiPatients(List<OpenEmpiPatientResult> mpiPatients, Double cutoff) {
		List<String> matchedFields = Arrays.asList("personName", "gender", "birthdate");
		List<PatientAndMatchQuality> result = new LinkedList<PatientAndMatchQuality>();
		for (OpenEmpiPatientResult mpiPatient : mpiPatients) {
			Patient convertedPatient = convertMpiPatient(mpiPatient);
			PatientAndMatchQuality resultPatient = new PatientAndMatchQuality(convertedPatient, cutoff, matchedFields);
			result.add(resultPatient);
		}
		return result;
	}
	
	private List<OpenEmpiPatientResult> limitResults(Integer maxResults, List<OpenEmpiPatientResult> mpiPatients) {
		return mpiPatients.size() > maxResults ? mpiPatients.subList(0, maxResults) : mpiPatients;
	}
	
	private Patient convertMpiPatient(OpenEmpiPatientResult mpiPatient) {
		patientBuilder.setPatient(new MpiPatient());
		Patient patient = patientBuilder.buildPatient(mpiPatient);
		patient.setUuid(String.valueOf(mpiPatient.getPersonId()));
		return patient;
	}
}
