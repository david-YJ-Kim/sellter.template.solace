package com.slt.template.solace.utill;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.core.env.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.slt.template.solace.config.SessionConfiguration;
import com.slt.template.solace.utill.SempInfo;
import com.slt.template.solace.utill.SempInfo.PrintingMessageHandler;
import com.slt.template.solace.utill.SempInfo.PrintingPubCallback;
import com.slt.template.solace.utill.SempInfo.PrintingSessionEventHandler;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.CapabilityType;
import com.solacesystems.jcsmp.Consumer;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPTransportException;
import com.solacesystems.jcsmp.Requestor;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImportQueueList extends SempInfo {
	
	Environment env;
	private Consumer cons;
	private XMLMessageProducer prod;
	private ArrayList<String> queueList = new ArrayList<String>();
	private SessionConfiguration sesscionConf;

	public ImportQueueList() {
//		sesscionConf = SessionConfiguration.createSessionConfiguration(env);
		run();
	}

	void run() {
		sesscionConf = SessionConfiguration.getSessionConfiguration();

		session = SolaceUtils.newSession(sesscionConf, new PrintingSessionEventHandler(), null);

		try {
	        session.connect();
			prod = session.getMessageProducer(new PrintingPubCallback());
			cons = session.getMessageConsumer(new PrintingMessageHandler());
			printRouterInfo();
			cons.start();

			String routerName = (String) session.getCapability(CapabilityType.PEER_ROUTER_NAME);
			
			final String SEMP_TOPIC_STRING = String.format("#SEMP/%s/SHOW", routerName);
			final Topic SEMP_TOPIC = JCSMPFactory.onlyInstance().createTopic(SEMP_TOPIC_STRING);
			Map<String,String> extraArguments = SessionConfiguration.getSessionConfiguration().getArgBag();
			String sempVersion = extraArguments.containsKey("-sv") ? sempVersion = extraArguments.get("-sv") : SEMP_VERSION_TR;
			final String SEMP_SHOW_QUEUES = "<rpc semp-version=\""
											+ sempVersion
											+ "\"><show><queue><name>"
											+SessionConfiguration.getSessionConfiguration().getModuleName()
											+"*</name><count/><num-elements>100</num-elements></queue></show></rpc>";
			final String MORECOOKIE_START = "<more-cookie>";
			final String MORECOOKIE_END = "</more-cookie>";

			// Set up the requestor on an open session to perform request operations.
			Requestor requestor = session.createRequestor();

			/*
			 * We perform requests in a loop. Each new request uses the
			 * more-cookie from the previous response.
			 */
			String next_request = SEMP_SHOW_QUEUES;
			while(next_request != null) {
				BytesXMLMessage requestMsg = JCSMPFactory.onlyInstance().createMessage(BytesXMLMessage.class);
				requestMsg.writeAttachment(next_request.getBytes());

				BytesXMLMessage replyMsg = requestor.request(requestMsg, 5000, SEMP_TOPIC);

				String replyStr = "";
				if (replyMsg.getAttachmentContentLength() > 0) {
					byte[] bytes = new byte[replyMsg.getAttachmentContentLength()];
					replyMsg.readAttachmentBytes(bytes);
					replyStr = new String(bytes, "UTF-8");
				}

				Document doc = loadXmlDoc(replyStr);
				XPathFactory xpfactory = XPathFactory.newInstance();
				XPath xpath = xpfactory.newXPath();

				// Check execute-result using an XPath query.
				String resultCode = (String) xpath.evaluate("string(//execute-result/@code)", doc, XPathConstants.STRING);

				if (!"ok".equals(resultCode)) {
					throw new Exception(String.format("SEMP response '%s' not OK.", resultCode));
				}

				/*
				 * List queues. We select text nodes under
				 * <queues><queue><name>NAME</name></queue><queues> in the
				 * response.
				 */
				NodeList nl = (NodeList) xpath.evaluate("//show/queue/queues/queue/name", doc, XPathConstants.NODESET);
				
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					queueList.add(node.getTextContent());
					String str = node.getTextContent();
					queueList.add(str);
					/* property의 module-name이 큐 이름에 포함되어 있으면 리스트에 담음 */
//					if(str.toUpperCase().contains(modulename.toUpperCase())) {}
				}
				
				// Check for more data to request with more-cookie.
				int start_idx = replyStr.indexOf(MORECOOKIE_START);
				if (start_idx >= 0) {
					// more data available
					int end_idx = replyStr.indexOf(MORECOOKIE_END);
					next_request = replyStr.substring(start_idx + MORECOOKIE_START.length(), end_idx);
					System.out.println("Found more-cookie...");
				} else {
					// Abort the loop, no more data.
					next_request = null;
				}
			} // End requestor loop.
			
			finish(0);
		} catch (JCSMPTransportException ex) {
			log.error("Encountered a JCSMPTransportException, closing consumer channel... ", ex.getMessage());
			
			if (cons != null) { cons.close(); }
			finish(1);
		} catch (JCSMPException ex) {
			log.error("Encountered a JCSMPException, closing consumer channel... ", ex.getMessage());

			if (cons != null) { cons.close(); }
			finish(1);
		} catch (Exception ex) {
			log.error("Encountered an Exception... ", ex.getMessage());
			finish(1);
		}
	}
	
	public ArrayList<String> getQueueList() {
		return queueList;
	}

	private static Document loadXmlDoc(String doc) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(doc)));
	}
}
