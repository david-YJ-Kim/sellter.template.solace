package com.slt.template.solace.utill;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadUtil {
	public static void start(Runnable r, String name) {
		Thread t = new Thread(r);
		t.setName(name);
		t.start();
	}

	public static void delayForSecond(int seconds) {
		new Thread();
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			log.error("ThreadUtil.delayForSecond() InterruptedException # ", e);
		}
	}

	public static void delayForMillSec(int millisec) {
		new Thread();
		try {
			Thread.sleep(millisec);
		} catch (InterruptedException e) {
			log.error("ThreadUtil.delayForMillSec() InterruptedException # ", e);
		}
	}
}

