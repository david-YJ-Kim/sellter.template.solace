package com.slt.template.solace.controller;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PubEventHandler implements JCSMPStreamingPublishCorrelatingEventHandler {
	@Override
	public void handleErrorEx(Object messageID, JCSMPException cause, long timestamp) {
		log.error("Producer received error for msg.");
	}

	@Override
	public void responseReceivedEx(Object messageID) {
		log.info("Producer received response for msg.");
		
	}
}
