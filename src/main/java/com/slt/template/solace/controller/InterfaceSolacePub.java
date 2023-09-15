package com.slt.template.solace.controller;

import org.springframework.stereotype.Component;

import com.slt.template.seqlib.SequenceManager;
import com.slt.template.seqlib.util.SequenceManageUtil;
import com.slt.template.solace.config.SessionConfiguration;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InterfaceSolacePub implements Runnable {
	
    private JCSMPProperties properties = new JCSMPProperties();

    private static InterfaceSolacePub instance;
    private JCSMPSession session;    
    private Topic topic;
    
    //PubCallback Event class
    private com.slt.template.solace.controller.PubEventHandler pubEventHandler = new com.slt.template.solace.controller.PubEventHandler();
//    private String queue_name = "SVM_DEV_EAP_CMN_10";
    private String msg_id;
    private String eqp_id;
    private final String msg = "{\r\n"
    		+ "  \"squadName\": \"Super hero squad\",\r\n"
    		+ "  \"homeTown\": \"Metro City\",\r\n"
    		+ "  \"formed\": 2023,\r\n"
    		+ "  \"secretBase\": \"Super tower\",\r\n"
    		+ "  \"active\": true,\r\n"
    		+ "  \"members\": [\r\n"
    		+ "    {\r\n"
    		+ "      \"name\": \"Molecule Man\",\r\n"
    		+ "      \"age\": 29,\r\n"
    		+ "      \"secretIdentity\": \"Dan Jukes\",\r\n"
    		+ "      \"powers\": [\"Radiation resistance\", \"Turning tiny\", \"Radiation blast\"]\r\n"
    		+ "    },\r\n"
    		+ "    {\r\n"
    		+ "      \"name\": \"Madame Uppercut\",\r\n"
    		+ "      \"age\": 39,\r\n"
    		+ "      \"secretIdentity\": \"Jane Wilson\",\r\n"
    		+ "      \"powers\": [\r\n"
    		+ "        \"Million tonne punch\",\r\n"
    		+ "        \"Damage resistance\",\r\n"
    		+ "        \"Superhuman reflexes\"\r\n"
    		+ "      ]\r\n"
    		+ "    },\r\n"
    		+ "    {\r\n"
    		+ "      \"name\": \"Eternal Flame\",\r\n"
    		+ "      \"age\": 1000000,\r\n"
    		+ "      \"secretIdentity\": \"Unknown\",\r\n"
    		+ "      \"powers\": [\r\n"
    		+ "        \"Immortality\",\r\n"
    		+ "        \"Heat Immunity\",\r\n"
    		+ "        \"Inferno\",\r\n"
    		+ "        \"Teleportation\",\r\n"
    		+ "        \"Interdimensional travel\"\r\n"
    		+ "      ]\r\n"
    		+ "    }\r\n"
    		+ "  ]\r\n"
    		+ "}";
    
    
	public void run() {
		
		if(initialize()) sendMessage(msg);//sendMessage(topic_list);
	}
	
	private boolean initialize() {
		try {			
			//SpringJCSMPFactory 생성
//			springJcsmpFactory = new SpringJCSMPFactory(properties);
			properties = SessionConfiguration.getSessionConfiguration().getProperty("PUB");
			//SpringJCSMPFactory를 이용한 JCSMPSession 생성(JCSMPFactory 사용하는 것과 동일 -> session = JCSMPFactory.onlyInstance().createSession(properties);)
			session = JCSMPFactory.onlyInstance().createSession(properties);
			//session 연결 - Application별로 최소 연결 권장(쓰레드를 사용할 경우 공유 사용 권장)
			session.connect();
			
			//Queue - SolAdmin에서 생성한 queue에 접속, SolAdmin에 생성되지 않은 경우 Application에서 생성
//			Queue queue = JCSMPFactory.onlyInstance().createQueue(queue_name);
			
			return true;
		} catch(Exception e)
		{
			log.error("Sender.initialize() Exception # ",e);
			
			return false;
		}
	}
	
	public static InterfaceSolacePub getInstance() throws JCSMPException {
        // Create the instance if it doesn't exist
        if (instance == null) {
            instance = new InterfaceSolacePub();
        }
        return instance;
    }
	
	private void sendMessage(String msg) {
		try {
			XMLMessageProducer prod = session.getMessageProducer(pubEventHandler);

			//Text Messsage
            TextMessage jcsmpMsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            
            //Custom Property
            SDTMap map = JCSMPFactory.onlyInstance().createMap();
           
            // 테스트 topic 획득
            topic = JCSMPFactory.onlyInstance().createTopic( SequenceManager.getTargetName("WFS") );
            // 운영시 사용 코드 
//            topic = JCSMPFactory.onlyInstance().createTopic( SequenceManager.getTargetName("targetSystem", "cid", "obj.toString()", "ownerSystem" ) );
            
            while(true) {	// Messaging Test 시에만 사용, 운영에서는 while 삭제
//            	count++;
            	eqp_id = "EQP-0001";			// 임의설정
            	
            	//Custom Property 설정
            	map.putString("MSG_ID", msg_id);
                map.putString("EQP_ID", eqp_id);

                jcsmpMsg.setProperties(map);
	            jcsmpMsg.setText(msg);
	            //Application messageId 설정
	            jcsmpMsg.setApplicationMessageId(SequenceManageUtil.generateMessageID());
	            jcsmpMsg.setDeliveryMode(DeliveryMode.PERSISTENT);
	
	            log.info("message sending.");
	            
	            //Multi Application이 동일한 큐에 매핑된 Multi Topic에 송신한다고 가정한 코드
	            prod.send(jcsmpMsg, topic);
	            	           
            }
		} catch(Exception e) {
			log.error("Sender.sendMessage() Exception # ",e);
			if (session != null && !session.isClosed()) try { session.closeSession(); } catch (Exception e1) {}
		}
	}
}
