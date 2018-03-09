package org.openmrs.module.registrationcore.api.biometrics.model;

public class EnrollmentResult {

    private BiometricSubject biometricSubject;

    private EnrollmentStatus enrollmentStatus;

    public EnrollmentResult(BiometricSubject biometricSubject, EnrollmentStatus enrollmentStatus) {
        this.biometricSubject = biometricSubject;
        this.enrollmentStatus = enrollmentStatus;
    }

    public BiometricSubject getBiometricSubject() {
        return biometricSubject;
    }

    public EnrollmentStatus getEnrollmentStatus() {
        return enrollmentStatus;
    }
}
