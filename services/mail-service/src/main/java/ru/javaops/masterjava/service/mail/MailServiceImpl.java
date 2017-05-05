package ru.javaops.masterjava.service.mail;

import ru.javaops.web.WebStateException;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.util.List;
import java.util.Set;

@WebService(endpointInterface = "ru.javaops.masterjava.service.mail.MailService", targetNamespace = "http://mail.javaops.ru/"
//          , wsdlLocation = "WEB-INF/wsdl/mailService.wsdl"
)
//@StreamingAttachment(parseEagerly=true, memoryThreshold=40000L)
//@MTOM
@HandlerChain(file = "mailWsHandlers.xml")
public class MailServiceImpl implements MailService {

    @Resource
    private WebServiceContext wsContext;

    @Override
    public String sendToGroup(Set<Addressee> to, Set<Addressee> cc, String subject, String body, List<Attach> attaches) throws WebStateException {
        return MailSender.sendToGroup(to, cc, subject, body, attaches);
    }

    @Override
    public GroupResult sendBulk(Set<Addressee> to, String subject, String body, List<Attach> attaches) throws WebStateException {
        return MailServiceExecutor.sendBulk(to, subject, body, attaches);
    }
}