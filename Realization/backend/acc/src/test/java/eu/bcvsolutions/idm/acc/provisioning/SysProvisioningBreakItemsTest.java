package eu.bcvsolutions.idm.acc.provisioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakItems;

/**
 * Tests for check synchronized block and multithread behavior with {@link SysProvisioningBreakItems}
 *
 * @author Ondrej Kopr
 *
 */
public class SysProvisioningBreakItemsTest {
	
	@Test
	public void testAdd() throws InterruptedException {
		CountDownLatch readyCounter = new CountDownLatch(1000);
	    CountDownLatch lock = new CountDownLatch(1);
	    CountDownLatch completeCounter = new CountDownLatch(1000);
	    
	    SysProvisioningBreakItems items = new SysProvisioningBreakItems();
	    
	    for (int index = 0; index < 1000; index++) {
			Thread thread = new Thread(new ItemsWorker(readyCounter, lock, completeCounter, new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					items.addItem(ProvisioningEventType.UPDATE, System.currentTimeMillis());
					return null;
				}
			}));
			thread.start();
		}
	    
	    List<Long> executedItems = items.getExecutedItems(ProvisioningEventType.UPDATE);
	    assertEquals(0, executedItems.size());

	    // Wait on all thread
	    readyCounter.await();
	    // Release all thread
	    lock.countDown(); 
	    // Wait on all thread
	    completeCounter.await(); 

	    executedItems = items.getExecutedItems(ProvisioningEventType.UPDATE);
	    assertEquals(1000, executedItems.size());
	    
	}

	@Test
	public void testRemove() throws InterruptedException {
		CountDownLatch readyCounter = new CountDownLatch(5000);
	    CountDownLatch lock = new CountDownLatch(1);
	    CountDownLatch completeCounter = new CountDownLatch(5000);
	    
	    SysProvisioningBreakItems items = new SysProvisioningBreakItems();
	    
	    for (int index = 0; index < 5000; index++) {
	    	items.addItem(ProvisioningEventType.UPDATE, Long.valueOf(1000 + index));
		}
	    
	    for (int index = 0; index < 5000; index++) {
			Thread thread = new Thread(new ItemsWorker(readyCounter, lock, completeCounter, new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					items.removeOlderRecordsThan(ProvisioningEventType.UPDATE, Long.valueOf(100000));
					return null;
				}
			}));
			thread.start();
		}
	    
	    List<Long> executedItems = items.getExecutedItems(ProvisioningEventType.UPDATE);
	    assertEquals(5000, executedItems.size());

	    // Wait on all thread
	    readyCounter.await();
	    // Release all thread
	    lock.countDown(); 
	    // Wait on all thread
	    completeCounter.await(); 

	    executedItems = items.getExecutedItems(ProvisioningEventType.UPDATE);
	    assertEquals(0, executedItems.size());
	}

	@Test
	public void testClear() throws InterruptedException {
		CountDownLatch readyCounter = new CountDownLatch(1000);
	    CountDownLatch lock = new CountDownLatch(1);
	    CountDownLatch completeCounter = new CountDownLatch(1000);
	    
	    SysProvisioningBreakItems items = new SysProvisioningBreakItems();
	    
	    for (int index = 0; index < 1000; index++) {
	    	items.addItem(ProvisioningEventType.UPDATE, Long.valueOf(1000 + index));
		}
	    
	    for (int index = 0; index < 1000; index++) {
			Thread thread = new Thread(new ItemsWorker(readyCounter, lock, completeCounter, new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					items.clearRecords(ProvisioningEventType.UPDATE);
					return null;
				}
			}));
			thread.start();
		}
	    
	    List<Long> executedItems = items.getExecutedItems(ProvisioningEventType.UPDATE);
	    assertEquals(1000, executedItems.size());

	    // Wait on all thread
	    readyCounter.await();
	    // Release all thread
	    lock.countDown(); 
	    // Wait on all thread
	    completeCounter.await(); 

	    executedItems = items.getExecutedItems(ProvisioningEventType.UPDATE);
	    assertEquals(0, executedItems.size());
	}

	/**
	 * Class that initialize that workers will be processed from barrier together.
	 *
	 * @author Ondrej Kopr
	 *
	 */
	public class ItemsWorker implements Runnable {

		private CountDownLatch readyCounter;
		private CountDownLatch lock;
		private CountDownLatch completeCounter;
		private Callable<Void> func;

		public ItemsWorker(CountDownLatch readyCounter, CountDownLatch lock, CountDownLatch completeCounter,
				Callable<Void> func) {
			this.readyCounter = readyCounter;
			this.lock = lock;
			this.completeCounter = completeCounter;
			this.func = func;
		}

		@Override
		public void run() {
			// Count ready workers
			readyCounter.countDown();
			try {
				// Wait for initializer
				lock.await();
				// Call method
				func.call();
			} catch (Exception e) {
				fail(e.getMessage());
			} finally {
				// Complete counter, on this wait initializer
				completeCounter.countDown();
			}
		}
	}
}
