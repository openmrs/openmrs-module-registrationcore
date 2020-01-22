package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpSender implements Hl7v2Sender {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RegistrationCoreProperties config;

    @Override
    public Message sendPdqMessage(Message request) throws LLPException, IOException, HL7Exception {
        URL url = new URL(config.getPdqEndpoint());
        return sendMessage(request, url);
    }

    @Override
    public Message sendPixMessage(Message request) throws LLPException, IOException, HL7Exception {
        URL url = new URL(config.getPixEndpoint());
        return sendMessage(request, url);
    }

    private Message sendMessage(Message request, URL url) throws HL7Exception, LLPException, IOException {

        PipeParser parser = new PipeParser();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/hl7-v2+er7; charset=utf-8");
        String authentication = config.getMpiUsername() + ":" + config.getMpiPassword();
        String encoded = "Basic " + Base64.encode(authentication.getBytes());
        connection.setRequestProperty("Authorization", encoded);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        try {
            log.debug(String.format("Sending to %s : %s", url, parser.encode(request)));

            String message = "\u000B" + request.toString() + "\u001C" + "\r";
            connection.getOutputStream().write(message.getBytes("UTF-8"));

            int code = connection.getResponseCode();
            Message response = null;

            if (!isSuccessfulResponseCode(code)) {
                throw new MpiException(String.format(
                        "MPI connection error. " + "Response code: %s, response message: %s, error stream: %n%s ", code,
                        connection.getResponseMessage(), IOUtils.toString(connection.getErrorStream())));
            }
            response = getResponse(connection, parser);
            log.debug(String.format("Response from %s : %s", url, parser.encode(response)));

            return response;
        } finally {
            connection.disconnect();
        }
    }

    private Message getResponse(HttpURLConnection connection, PipeParser parser) throws HL7Exception, IOException {
        InputStream inputStr = connection.getInputStream();
        String encoding = connection.getContentEncoding() == null ? "UTF-8" : connection.getContentEncoding();
        String decoded = IOUtils.toString(inputStr, encoding);
        decoded = decoded.substring(1, decoded.length() - 1);
        return parser.parse(decoded);
    }

    private boolean isSuccessfulResponseCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}
