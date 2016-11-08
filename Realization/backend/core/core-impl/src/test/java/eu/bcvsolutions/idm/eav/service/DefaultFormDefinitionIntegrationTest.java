package eu.bcvsolutions.idm.eav.service;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttributeDefinition;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeDefinitionRepository;

public class DefaultFormDefinitionIntegrationTest extends AbstractIntegrationTest {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultFormDefinitionIntegrationTest.class);

	@Autowired
	private IdmFormDefinitionService formDefinitionService;
	
	@Autowired
	private IdmFormAttributeDefinitionRepository formAttributeDefinitionRepository;
	
	private Random r = new Random();
	
	/**
	 * Creates definition
	 * 
	 * @param name
	 * @param randomAttributes with random count of attributes
	 */
	private IdmFormDefinition createDefinition(String name, boolean randomAttributes, boolean log) {
		IdmFormDefinition formDefinition = new IdmFormDefinition();
		formDefinition.setName(name);
		if (log) {
			LOG.info("Before definition save [{}]", name);
		}
		long startTime = System.currentTimeMillis();
		formDefinition = formDefinitionService.save(formDefinition);
		if (log) {
			LOG.info("--- {}ms:  After definition save [{}]", System.currentTimeMillis() - startTime, name);
		}
		int attributeCount = r.nextInt(40);
		if (log) {
			LOG.info("Before definition [{}] attributes save, attributes count [{}]", name, attributeCount);
		}
		startTime = System.currentTimeMillis();
		for(int i = 0; i < attributeCount; i++) {
			IdmFormAttributeDefinition attributeDefinition = new IdmFormAttributeDefinition();
			attributeDefinition.setFormDefinition(formDefinition);
			attributeDefinition.setName("name_" + i);
			attributeDefinition.setDisplayName(attributeDefinition.getName());
			attributeDefinition.setPersistentType(PersistentType.TEXT);			
			formAttributeDefinitionRepository.save(attributeDefinition);
		}
		if (log) {
			LOG.info("--- {}ms:  After definition [{}] attributes save, attributes count [{}]", System.currentTimeMillis() - startTime, name, attributeCount);
			startTime = System.currentTimeMillis();
			int realAttributeCount = formAttributeDefinitionRepository.findByFormDefinitionOrderBySeq(formDefinition).size();
			assertEquals(attributeCount, realAttributeCount);
			LOG.info("--- {}ms:  After definition [{}] attributes load, attributes count [{}]", System.currentTimeMillis() - startTime, name, realAttributeCount);
		}
		return formDefinition;
	}
	
	@Ignore
	@Test
	@Transactional
	public void deleteDefinitionWithAttributes() {
		IdmFormDefinition formDefinition = createDefinition("one", true, true);
		
		formDefinitionService.delete(formDefinition);
		
		assertEquals(0, formAttributeDefinitionRepository.findByFormDefinitionOrderBySeq(formDefinition).size());
	}
	
	@Test
	public void generateFormDefinitionsOneByOne() {		
		for (int i = 649100; i < 650000; i++) {
			createDefinition(i + "_def", true, true);
		}
	}
	
	@Test
	@Ignore
	public void generateFormDefinitions() throws InterruptedException {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(75);
		
		for (int i = 100000; i < 1000000; i++) {
			Runnable creator = new FormDefinitionCreator(i);
            executor.execute(creator);
            int queueSize = executor.getQueue().size();
            if (queueSize > 500) {
            	LOG.warn("Form definition generator has full queue [{}], pausing ...", queueSize);
            	Thread.sleep(5000);
            }
		}
		
		executor.shutdown();
        while (!executor.isTerminated()) {
        }
        LOG.info("Form definition generator - finished all threads");
	}
	
	private class FormDefinitionCreator implements Runnable {

		private final int id;
		
		public FormDefinitionCreator(int id) {
			this.id = id;
		}
		
		@Override
		public void run() {
			createDefinition(id + "_def", true, ((id % 100) == 99));
		}
		
	}
}
