package org.openmrs.module.registrationcore.api.biometrics.model;

public class EnrollmentResult {

    private BiometricSubject nationalBiometricSubject;

    private BiometricSubject localBiometricSubject;

    private EnrollmentStatus enrollmentStatus;

    public EnrollmentResult(BiometricSubject localBiometricSubject, BiometricSubject nationalBiometricSubject,
            EnrollmentStatus enrollmentStatus) {
        this.localBiometricSubject = localBiometricSubject;
        this.nationalBiometricSubject = nationalBiometricSubject;
        this.enrollmentStatus = enrollmentStatus;
    }

    public BiometricSubject getNationalBiometricSubject() {
        return nationalBiometricSubject;
    }

    public BiometricSubject getLocalBiometricSubject() {
        return localBiometricSubject;
    }

    public EnrollmentStatus getEnrollmentStatus() {
        return enrollmentStatus;
    }
}
