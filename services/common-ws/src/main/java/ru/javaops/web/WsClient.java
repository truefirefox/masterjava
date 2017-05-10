package ru.javaops.web;

import ru.javaops.masterjava.ExceptionType;
import ru.javaops.web.handler.SoapClientLoggingHandler;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static ru.javaops.web.HostConfig.HOST;

public class WsClient<T> {
    private final Class<T> serviceClass;
    private final Service service;
    private String endpointAddress;

    public WsClient(URL wsdlUrl, QName qname, Class<T> serviceClass) {
        this.serviceClass = serviceClass;
        this.service = Service.create(wsdlUrl, qname);
    }

    public void init(String endpointAddress) {
        this.endpointAddress = HOST.getEndpoint() + endpointAddress;
    }

    //  Post is not thread-safe (http://stackoverflow.com/a/10601916/548473)
    public T getPort(WebServiceFeature... features) {
        T port = service.getPort(serviceClass, features);
        BindingProvider bp = (BindingProvider) port;
        Map<String, Object> requestContext = bp.getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
        return port;
    }

    public static <T> void setAuth(T port) {
        Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
        requestContext.put(BindingProvider.USERNAME_PROPERTY, HOST.getUser());
        requestContext.put(BindingProvider.PASSWORD_PROPERTY, HOST.getPassword());
    }

    public static <T> void setHandler(T port) {
        Binding binding = ((BindingProvider) port).getBinding();
        List<Handler> handlerList = binding.getHandlerChain();
        handlerList.add(new SoapClientLoggingHandler(HOST.getDebugLevel()));
        binding.setHandlerChain(handlerList);
    }

    public static WebStateException getWebStateException(Exception e) {
        return (e instanceof WebStateException) ?
                (WebStateException) e : new WebStateException(ExceptionType.NETWORK, e);
    }
}
