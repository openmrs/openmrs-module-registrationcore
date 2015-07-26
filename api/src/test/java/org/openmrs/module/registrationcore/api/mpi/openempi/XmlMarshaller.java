package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.springframework.oxm.Unmarshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;

public class XmlMarshaller {

    private Unmarshaller unmarshaller;

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public OpenEmpiPatientQuery getQuery(String path) throws IOException {
        FileInputStream is = null;
        OpenEmpiPatientQuery query;
        try {
            is = new FileInputStream(path);
            query = (OpenEmpiPatientQuery) this.unmarshaller.unmarshal(new StreamSource(is));
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return query;
    }
}
