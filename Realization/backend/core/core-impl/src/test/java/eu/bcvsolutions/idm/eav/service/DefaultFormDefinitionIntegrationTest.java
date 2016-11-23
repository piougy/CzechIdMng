package eu.bcvsolutions.idm.eav.service;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeDefinitionRepository;
import eu.bcvsolutions.idm.eav.service.api.IdmFormDefinitionService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * EAV definition tests
 * 
 * @author Radek Tomiška
 *
 */
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
	private ResultHolder createDefinition(String name, boolean randomAttributes, boolean log) {
		ResultHolder result = new ResultHolder();
		
		IdmFormDefinition formDefinition = new IdmFormDefinition();
		formDefinition.setType("test_type");
		formDefinition.setName(name);
		if (log) {
			LOG.info("Before definition save [{}]", name);
		}
		long startTime = System.currentTimeMillis();
		formDefinition = formDefinitionService.save(formDefinition);
		if (log) {
			result.createTime = System.currentTimeMillis() - startTime;
			LOG.info("--- {}ms:  After definition save [{}]", result.createTime, name);
		}
		int attributeCount = r.nextInt(40);
		if (log) {
			LOG.info("Before definition [{}] attributes save, attributes count [{}]", name, attributeCount);
		}
		startTime = System.currentTimeMillis();
		for(int i = 0; i < attributeCount; i++) {
			IdmFormAttribute attributeDefinition = new IdmFormAttribute();
			attributeDefinition.setFormDefinition(formDefinition);
			attributeDefinition.setName("name_" + i);
			attributeDefinition.setDisplayName(attributeDefinition.getName());
			attributeDefinition.setPersistentType(PersistentType.TEXT);			
			formAttributeDefinitionRepository.save(attributeDefinition);
		}
		if (log) {
			result.childrenCreateTime = (double) System.currentTimeMillis() - startTime;
			LOG.info("--- {}ms:  After definition [{}] attributes save, attributes count [{}]", result.childrenCreateTime, name, attributeCount);
			if(attributeCount > 0) {
				result.childrenCreateTime = result.childrenCreateTime / attributeCount;
			}
			startTime = System.currentTimeMillis();
			int realAttributeCount = formAttributeDefinitionRepository.findByFormDefinitionOrderBySeq(formDefinition).size();
			assertEquals(attributeCount, realAttributeCount);
			result.childrenLoadTime = System.currentTimeMillis() - startTime;
			LOG.info("--- {}ms:  After definition [{}] attributes load, attributes count [{}]", result.childrenLoadTime, name, realAttributeCount);
		}
		result.formDefinition = formDefinition;
		return result;
	}
	
	@Test
	@Transactional
	public void deleteDefinitionWithAttributes() {
		IdmFormDefinition formDefinition = createDefinition("one", true, true).formDefinition;
		
		formDefinitionService.delete(formDefinition);
		
		assertEquals(0, formAttributeDefinitionRepository.findByFormDefinitionOrderBySeq(formDefinition).size());
	}
	
	@Test
	@Ignore
	public void generateFormDefinitionsOneByOne() {
		int start = 5000000;
		int end = 5000100;
		//
		ResultHolder averageResult = new ResultHolder();
		for (int i = start; i < end; i++) {
			ResultHolder result = createDefinition(i + "_def", true, true);
			averageResult.createTime += result.createTime;
			averageResult.childrenCreateTime += result.childrenCreateTime;
			averageResult.childrenLoadTime += result.childrenLoadTime;
		}
		LOG.info("----");
		LOG.info("---- Average definition save: {}ms", averageResult.createTime / (end - start));
		LOG.info("---- Average children save: {}ms", averageResult.childrenCreateTime / (end - start));
		LOG.info("---- Average children load: {}ms", averageResult.childrenLoadTime / (end - start));
	}
	
	@Test
	@Ignore
	public void generateFormDefinitions() throws InterruptedException {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(75);
		
		for (int i = 4555000; i < 5000000; i++) {
			Runnable creator = new FormDefinitionCreator(i);
            executor.execute(creator);
            int queueSize = executor.getQueue().size();
            if (queueSize > 5000) {
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
	
	private class ResultHolder {
		public IdmFormDefinition formDefinition;
		public double createTime;
		public double childrenCreateTime;
		public double childrenLoadTime;
	}
}
