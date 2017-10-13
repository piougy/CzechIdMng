package eu.bcvsolutions.idm.core;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.nilhcem.fakesmtp.model.EmailModel;
import com.nilhcem.fakesmtp.server.MailSaver;

import eu.bcvsolutions.idm.test.api.AbstractNotificationTest;

/**
 * Observer for catch email from smpt server. This class cant be part of
 * {@link AbstractNotificationTest}, concurrent thread.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class NotificationObserver implements Observer {

	// for synchronization thread
	private CountDownLatch lock = null;

	// store all email for this observer
	private Set<EmailModel> emails = new HashSet<>();

	public NotificationObserver(int count) {
		this.lock = new CountDownLatch(count);
	}

	/**
	 * Wait for emails
	 * 
	 * @throws InterruptedException
	 */
	public void waitForMails() throws InterruptedException {
		this.lock.await();
	}

	/**
	 * Wait for emails set by constructor or for given time in parameters.
	 * 
	 * @param maxWait
	 * @param timeUnit
	 * @throws InterruptedException
	 */
	public void setWaitForMails(long maxWait, TimeUnit timeUnit) throws InterruptedException {
		this.lock.await(maxWait, timeUnit);
	}

	/**
	 * Get actual emails
	 * 
	 * @return
	 */
	public Set<EmailModel> getEmails() {
		return this.emails;
	}

	/**
	 * This method will be called from smtp server, store all emails.
	 * 
	 * @param o
	 * @param arg
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof MailSaver) {
			emails.add((EmailModel) arg);
			if (lock != null) {
				lock.countDown();
			}
		}
	}
}
