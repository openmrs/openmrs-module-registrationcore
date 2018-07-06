/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the data that may be collected and saved within a patient registration transaction
 */
public class RegistrationData implements Serializable {

    private Patient patient;
    private List<Relationship> relationships;
    private String identifier;
    private Location identifierLocation;
    private List<BiometricData> biometrics;

    public RegistrationData() {}

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<Relationship> getRelationships() {
        if (relationships == null) {
            relationships = new ArrayList<Relationship>();
        }
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Location getIdentifierLocation() {
        return identifierLocation;
    }

    public void setIdentifierLocation(Location identifierLocation) {
        this.identifierLocation = identifierLocation;
    }

    public List<BiometricData> getBiometrics() {
        if (biometrics == null) {
            biometrics = new ArrayList<BiometricData>();
        }
        return biometrics;
    }

    public void setBiometrics(List<BiometricData> biometrics) {
        this.biometrics = biometrics;
    }

    public void addBiometricData(BiometricData biometricData) {
        getBiometrics().add(biometricData);
    }
}
