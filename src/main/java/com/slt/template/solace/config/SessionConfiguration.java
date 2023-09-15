package com.slt.template.solace.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPProperties;

//import async.controller.config.SessionConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class SessionConfiguration {
	private static SessionConfiguration instance;
	Environment env;

//	public static SessionConfiguration createSessionConfiguration(Environment env) {
//		if (instance == null) {
//			instance = new SessionConfiguration(env);
//		}
//		return instance;
//	}

	public enum AuthenticationScheme {
        BASIC,
        CLIENT_CERTIFICATE,
        KERBEROS
    };
    
	// Router properties
    @Value("${solace.java.host}")
    private String host;
	
	@Value("${solace.java.msg-vpn}")
    private String msgVpn;
	
	@Value("${solace.java.client-username}")
    private String clientUserName;
	
	@Value("${solace.java.client-password}")
    private String clientPassWord;
	
	@Value("${solace.java.client-name}")
    private String clientName;
	
	@Value("${solace.java.reconnnect-retries}")
    private int reconnnectRetries;
	
	@Value("${solace.java.retries-per-host}")
    private int retriesPerHost;
	
	@Value("${application.latch-count}")
    private int latchCount;
	
	@Value("${application.module-name}")
    private String moduleName;

	public SessionConfiguration(Environment env) {
		this.env = env;
		instance = this;
//		host = env.getProperty("solace.java.host");
//		msgVpn = env.getProperty("solace.java.msg-vpn");
//		clientUserName = env.getProperty("solace.java.client-username");
//		clientPassWord = env.getProperty("solace.java.client-password");
//		reconnnectRetries = Integer.parseInt(Objects.requireNonNull(env.getProperty("solace.java.reconnect-retries")));
//		retriesPerHost = Integer.parseInt(Objects.requireNonNull(env.getProperty("solace.java.connect-retries-per-host")));
//		clientName = env.getProperty("solace.java.client-name");
//		moduleName = env.getProperty("application.module-name");
//		latchCount = Integer.parseInt(Objects.requireNonNull(env.getProperty("application.latch-count")));
	}
	public static SessionConfiguration getSessionConfiguration() {
		return instance;
	}
	
	private DeliveryMode delMode = DeliveryMode.DIRECT;

	private Map<String, String> argBag = new HashMap<String, String>();
	

	public JCSMPProperties getProperty(String postfixClientName){
		JCSMPProperties properties = new JCSMPProperties();

		properties.setProperty(JCSMPProperties.HOST, host);
		//solace msgVpn명
		properties.setProperty(JCSMPProperties.VPN_NAME, this.msgVpn);
		//solace msgVpn에 접속할 클라이언트사용자명
		properties.setProperty(JCSMPProperties.USERNAME, this.clientUserName);
		//solace msgVpn에 접속할 클라이언트사용자 패스워드(생략 가능)
		if(this.clientPassWord != null && !this.clientPassWord.isEmpty())
			properties.setProperty(JCSMPProperties.PASSWORD, this.clientPassWord);
		//Allication client name 설정 - 동일 msgVpn 내에서 uniq 해야 함
		properties.setProperty(JCSMPProperties.CLIENT_NAME, this.clientName + postfixClientName);
		//endpoint에 등록되어 있는 subscription으로 인해 발생하는 에러 무시
		properties.setProperty(JCSMPProperties.IGNORE_DUPLICATE_SUBSCRIPTION_ERROR, true);

		JCSMPChannelProperties chProp = new JCSMPChannelProperties();

		chProp.setReconnectRetries(getReconnnectRetries()); // 세션 다운 시 재 연결 트라이 횟수
		chProp.setConnectRetriesPerHost(getRetriesPerHost()); // 세션 리트라이 간격
		properties.setProperty(JCSMPChannelProperties.RECONNECT_RETRIES, chProp);

		return properties;
	}
	
	public EndpointProperties getEndpoint(){
		/*
		 * EndPoint 설정
		 * - SolAdmin에서 설정이 되어 있는 경우 Applicaiton에서는 사용하지 않아도 됨(사용할 경우 SolAdmin 화면과 동일하게 구성)
		 * - SolAdmin에 설정이 없는 경우 Application에서 설정한 값으로 설정됨
		 */
		EndpointProperties endpointProps = new EndpointProperties();
		/* Endpoint(queue, topic) 설정 - solAdmin 화면에서 설정한 값과 동일 */
		//Endpoint(Queue) 권한 설정
		endpointProps.setPermission(EndpointProperties.PERMISSION_DELETE);
		//Endpoint(Queue) accesstype 설정
		endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
		//Endpoint(Queue) 용량 설정
		endpointProps.setQuota(100);
		//Endpoint provisioning - solAdmin 에 생성된 Endpoint 가 있으므로 "FLAG_IGNORE_ALREADY_EXISTS" 사용)
		return endpointProps;
	}
}
