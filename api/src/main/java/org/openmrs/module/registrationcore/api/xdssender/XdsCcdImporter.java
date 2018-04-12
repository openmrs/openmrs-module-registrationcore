package org.openmrs.module.registrationcore.api.xdssender;

import org.dcm4chee.xds2.common.exception.XDSException;
import org.openmrs.Patient;
import org.openmrs.module.xdssender.api.domain.Ccd;

import java.io.IOException;

public interface XdsCcdImporter {

    Ccd getLocallyStoredCcd(Patient patient);

    Ccd downloadAndSaveCcd(Patient patient) throws XDSException, IOException;
}
