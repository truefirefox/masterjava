package ru.javaops.web.handler;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.typesafe.config.Config;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.web.AuthUtil;
import ru.javaops.web.Statistics;

import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

/**
 * Created by val on 2017-05-03.
 */
public class SoapServerSecurityHandler extends SoapBaseHandler {
    private final Config auth = Configs.getConfig("hosts.conf", "hosts", "mail");

    public SoapServerSecurityHandler() {
        super();
    }

    @Override
    public boolean handleMessage(MessageHandlerContext context) {
        if (!isOutbound(context)){
            final String AUTH_HEADER = AuthUtil.encodeBasicAuthHeader(auth.getString("user"), auth.getString("password"));

            Map<String, List<String>> headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
            int code = AuthUtil.checkBasicAuth(headers, AUTH_HEADER);
            if (code != 0) {
                context.put(MessageContext.HTTP_RESPONSE_CODE, code);
                Statistics.count(context.getMessage().getPayloadLocalPart(), System.currentTimeMillis(), Statistics.RESULT.FAIL);
                throw new SecurityException();
            }
        }

        return true;
    }

    @Override
    public boolean handleFault(MessageHandlerContext context) {
        return true;
    }
}
