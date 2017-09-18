package eu.bcvsolutions.idm.test.api;

import java.util.Observer;

import com.nilhcem.fakesmtp.core.exception.BindPortException;
import com.nilhcem.fakesmtp.core.exception.OutOfRangePortException;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;

/**
 * Abstract class with smtp server. Server isn't started automatically, also
 * stop is executed manually. For catch email use {@link Observer} method
 * {@link AbstractNotificationTest#addObserver(Observer)}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class AbstractNotificationTest extends AbstractIntegrationTest {

	protected static int DEFAULT_SMTP_PORT = 2525;
	protected static final String TOPIC = "idm:smtpTest";
	protected static final String TEST_TEMPLATE = "testTemplate";
	protected static final String FROM = "idm-test@bcvsolutions.eu";
	protected static final String PROTOCOL = "smtp";
	protected static final String HOST = "localhost";

	/**
	 * Initialized smtp server with default port {@link AbstractNotificationTest#DEFAULT_SMTP_PORT}
	 * 
	 * @return
	 * @throws BindPortException
	 * @throws OutOfRangePortException
	 */
	protected SMTPServerHandler initSmtpServer() throws BindPortException, OutOfRangePortException {
		// TODO: set memory mode only ArgsHandler.INSTANCE.
		SMTPServerHandler.INSTANCE.startServer(DEFAULT_SMTP_PORT, null);

		return SMTPServerHandler.INSTANCE;
	}

	/**
	 * Add observer for this server. Observer can't be part of this class.
	 * 
	 * @param observer
	 */
	protected void addObserver(Observer observer) {
		SMTPServerHandler.INSTANCE.getMailSaver().addObserver(observer);
	}

	/**
	 * Stop smpt server, this operation take a while.
	 * 
	 */
	protected void stopSmtpServer() {
		SMTPServerHandler.INSTANCE.stopServer();
	}

	/**
	 * Start smtp server.
	 * 
	 * @return
	 * @throws BindPortException
	 * @throws OutOfRangePortException
	 */
	protected SMTPServerHandler startSmtpServer() throws BindPortException, OutOfRangePortException {
		return this.initSmtpServer();
	}

	/**
	 * Check of server running.
	 * 
	 * @return
	 */
	protected boolean isRunning() {
		return SMTPServerHandler.INSTANCE.getSmtpServer().isRunning();
	}
}
