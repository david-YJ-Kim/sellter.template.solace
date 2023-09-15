package com.slt.template.solace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.slt.template.solace.config.SessionConfiguration;
import com.slt.template.solace.controller.InterfaceSolacePub;
import com.slt.template.solace.controller.InterfaceSolaceSub;
import com.solacesystems.jcsmp.JCSMPException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SolaceApplicationRunner implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private Environment env;

    static SessionConfiguration sessionConfiguration = null;

    public SolaceApplicationRunner() {
    }

    public void onApplicationEvent(ApplicationStartedEvent event) {

        try {
//			SessionConfiguration sessionConfiguration = new SessionConfiguration(env);
//			InterfaceSolacePub interfaceSolacePub = InterfaceSolacePub.getInstance();
//			interfaceSolacePub.run();
//			InterfaceSolaceSub interfaceSolaceSub = new InterfaceSolaceSub();
//			interfaceSolaceSub.run();
//			사용시 JCSMPException 으로 catch 변경
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        startMessageExchanger();
    }

    private void startMessageExchanger() {

        if(System.getProperty("type").equalsIgnoreCase("S")) {
            log.info("************************ Sender Startup ************************");
            new Thread(new InterfaceSolacePub()).start();
        } else if(System.getProperty("type").equalsIgnoreCase("R")) {
            log.info("************************ Receiver Startup ************************");
            new Thread(new InterfaceSolaceSub()).start();
        } else {
            log.error("************************ Invalid VM argument. System Shutdown. ************************");
            System.exit(-1);
        }
    }
}
