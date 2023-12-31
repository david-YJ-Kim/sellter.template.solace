package com.slt.template.activator;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApShutdownListener implements ApplicationListener<ContextClosedEvent> {
    public void onApplicationEvent(ContextClosedEvent event) {
        if(System.getProperty("type").equalsIgnoreCase("sender")) {
            log.info("************************ Sender Shutdown ************************");
        } else {
            log.info("************************ Receiver Shutdown ************************");
        }
    }
}
