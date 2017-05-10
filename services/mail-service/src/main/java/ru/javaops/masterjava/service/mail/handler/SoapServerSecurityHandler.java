package ru.javaops.masterjava.service.mail.handler;

import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.typesafe.config.Config;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.web.AuthUtil;
import ru.javaops.web.Statistics;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by val on 2017-05-03.
 */
public class SoapServerSecurityHandler implements MessageHandler<MessageHandlerContext> {
    private final static Config MAIL = Configs.getConfig("hosts.conf", "hosts", "mail");
    private final static String AUTH_HEADER;
    static {
        AUTH_HEADER =  AuthUtil.encodeBasicAuthHeader(MAIL.getString("user"), MAIL.getString("password"));
    }

    @Override
    public boolean handleMessage(MessageHandlerContext context) {
        if (!(Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)){
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

    @Override
    public void close(MessageContext context) {}

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
