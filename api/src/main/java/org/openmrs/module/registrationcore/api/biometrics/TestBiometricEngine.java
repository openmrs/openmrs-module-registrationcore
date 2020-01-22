/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api.biometrics;

import org.openmrs.module.registrationcore.api.biometrics.model.BiometricEngineStatus;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricMatch;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.biometrics.model.Fingerprint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Not meant for actual production usage, this is a simple implementation of the Biometrics Engine
 * that is intended to enable testing or demonstration of functionality by storing biometric
 * subjects in memory in a Map, and doing simple text matching on template contents
 */
@Component
public class TestBiometricEngine implements BiometricEngine {
	
	private Map<String, BiometricSubject> enrolledSubjects = new HashMap<String, BiometricSubject>();
	
	@Override
	public BiometricEngineStatus getStatus() {
		BiometricEngineStatus status = new BiometricEngineStatus();
		status.setEnabled(true);
		status.setDescription("Test biometrics engine for demonstration purposes");
		return status;
	}
	
	@Override
	public BiometricSubject enroll(BiometricSubject subject) {
		if (subject.getSubjectId() == null) {
			subject.setSubjectId(UUID.randomUUID().toString());
		}
		enrolledSubjects.put(subject.getSubjectId(), subject);
		return subject;
	}
	
	@Override
	public BiometricSubject update(BiometricSubject subject) {
		if (subject.getSubjectId() == null) {
			return enroll(subject);
		}
		BiometricSubject existing = lookup(subject.getSubjectId());
		if (existing == null) {
			throw new IllegalArgumentException("No subject with id <" + subject.getSubjectId() + "> found.");
		}
		existing.setFingerprints(subject.getFingerprints());
		return existing;
	}
	
	@Override
	public BiometricSubject updateSubjectId(String oldId, String newId) {
		BiometricSubject s = enrolledSubjects.remove(oldId);
		s.setSubjectId(newId);
		enroll(s);
		return s;
	}
	
	@Override
	public List<BiometricMatch> search(BiometricSubject subject) {
		List<BiometricMatch> ret = new ArrayList<BiometricMatch>();
		for (BiometricSubject s : enrolledSubjects.values()) {
			double score = 0;
			if (s.getSubjectId().equals(subject.getSubjectId())) {
				score += 10000;
			}
			for (Fingerprint fp : s.getFingerprints()) {
				for (Fingerprint fp2 : subject.getFingerprints()) {
					if (fp.getTemplate().equals(fp2.getTemplate())) {
						score += 1000;
					}
				}
			}
			if (score > 0) {
				ret.add(new BiometricMatch(s.getSubjectId(), score));
			}
		}
		Collections.sort(ret);
		return ret;
	}
	
	@Override
	public BiometricSubject lookup(String subjectId) {
		return enrolledSubjects.get(subjectId);
	}
	
	@Override
	public void delete(String subjectId) {
		enrolledSubjects.remove(subjectId);
	}
}
