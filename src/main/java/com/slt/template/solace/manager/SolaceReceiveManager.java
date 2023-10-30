package com.slt.template.solace.manager;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import com.slt.template.solace.worker.ReceiveWorker;
import com.slt.template.config.solace.SolaceSessionConfiguration;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;

import com.slt.template.solace.utill.ImportQueueList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SolaceReceiveManager implements Runnable {
	private JCSMPProperties properties = new JCSMPProperties();
	
    private JCSMPSession session;

    private int thread_count = 2;
    private CountDownLatch latch = new CountDownLatch(thread_count);
	private long timeout = 1000;
	
	private ImportQueueList iql;
	private ArrayList<String> queueList;
	
	//Exclusive Queue
	private String queue_name;

    @Override
	public void run() {
    	log.info("ReceiverMain Thread Start");
    	
    	try {
    		iql = new ImportQueueList();
    		queueList = iql.getQueueList();
//    		queue_name = queueList.get(0);
    		
    		//실행 모듈명이 들어간 QList만 받아옴
    		for(String queue_name : queueList) {
    			log.debug("Queue # "+queue_name);
    		}
    		
    		properties = SolaceSessionConfiguration.getSessionConfiguration().getProperty("SUB");

    		//SpringJCSMPFactory를 이용한 JCSMPSession 생성(JCSMPFactory 사용하는 것과 동일 -> session = JCSMPFactory.onlyInstance().createSession(properties);)
			session = JCSMPFactory.onlyInstance().createSession(properties);
			//session 연결 - Application별로 최소 연결 권장(쓰레드를 사용할 경우 공유 사용 권장)
			session.connect();
			
//			//Queue - SolAdmin에서 생성한 queue에 접속, SolAdmin에 생성되지 않은 경우 Application에서 생성
//			final Queue queue = JCSMPFactory.onlyInstance().createQueue(queue_name);
			
	    	for(int i=0; i<queueList.size(); i++) {
				new Thread(new ReceiveWorker(latch, session, queueList.get(i), SolaceSessionConfiguration.getSessionConfiguration().getModuleName(),
						"Receiver-"+ SolaceSessionConfiguration.getSessionConfiguration().getModuleName()+"-"+i)).start();
			}
   	
	    	latch.await();
    	} catch(Exception e) {
    		e.printStackTrace();
    		if (!session.isClosed()) try { session.closeSession(); } catch (Exception e1) {}
    	}
    }
    
    public boolean cleanUp(JCSMPSession session) {
//      if(!this.flowQueueReceiver.isClosed()) {
//          this.flowQueueReceiver.close();
//      }

      if(!session.isClosed()) {
          session.closeSession();
      }

      return true;
  }
}
