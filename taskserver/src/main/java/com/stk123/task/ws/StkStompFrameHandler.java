package com.stk123.task.ws;

import com.stk123.model.RequestResult;
import com.stk123.model.ws.ClientMessage;
import com.stk123.model.ws.ServerMessage;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;

@CommonsLog
@Component
public class StkStompFrameHandler implements StompFrameHandler {

    @Value("${stk.service.ip}")
    private String serviceIp;
    @Value("${stk.service.port}")
    private String servicePort;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StkWebSocketClient stkWebSocketClient;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return ServerMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
//        System.err.println(payload.getClass()); //com.stk123.model.ws.ServerMessage
        System.err.println("subscribeTopic:"+payload.toString());
        ServerMessage sm = (ServerMessage)payload;
        if(sm.getType() != null) {
            RequestResult requestResult = null;
            if(sm.getRequestMethod() == RequestMethod.GET) {
                String url = "http://"+serviceIp+":"+servicePort+"/"+sm.getType();
                if(sm.getData() != null){
                    url = url + "?" + sm.getData();
                }
                try {
                    requestResult = restTemplate.getForObject(url, RequestResult.class);
                    log.info("restTemplate url:" + url + ", result:" + requestResult);
                }catch(Exception e){
                    requestResult = RequestResult.failure(e.getMessage());
                }
            }else if(sm.getRequestMethod() == RequestMethod.POST) {
                requestResult = RequestResult.failure("Http method post TODO...");
            }
            ClientMessage cm = new ClientMessage();
            cm.setType(sm.getType());
            cm.setMessageId(sm.getMessageId());
            cm.setData(requestResult);
            stkWebSocketClient.send(cm);

        }
    }
}
