package org.openmrs.module.registrationcore.api.mpi.pixpdq;


import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpSender implements Hl7v2Sender {

    private final Log log = LogFactory.getLog(this.getClass());

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
        String encoded = "Basic "+ Base64.encode(authentication.getBytes());
        connection.setRequestProperty("Authorization", encoded);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        try
        {
            if(log.isDebugEnabled())
                log.debug(String.format("Sending to %s : %s", url, parser.encode(request)));

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes("\u000B" + request.toString() + "\u001C" + "\r");

            int code = connection.getResponseCode();
            Message response = null;
            wr.flush();
            wr.close();

            if (code == 200) {
                response = getResponse(connection, parser);
            }

           if(log.isDebugEnabled())
                log.debug(String.format("Response from %s : %s", url, parser.encode(response)));

            return response;
        }
        finally
        {
            connection.disconnect();
        }
    }

    private Message getResponse(HttpURLConnection connection,  PipeParser parser) throws HL7Exception, LLPException, IOException
    {
        InputStream inputStr = connection.getInputStream();
        String encoding = connection.getContentEncoding() == null ? "UTF-8"
                : connection.getContentEncoding();
        String decoded = IOUtils.toString(inputStr, encoding);
        decoded = decoded.substring(1, decoded.length() - 1);
        return parser.parse(decoded);
    }

}
