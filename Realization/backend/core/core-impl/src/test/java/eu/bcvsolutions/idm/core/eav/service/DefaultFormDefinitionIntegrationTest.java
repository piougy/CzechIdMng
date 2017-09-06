package eu.bcvsolutions.idm.core.eav.service;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormAttributeRepository;
import eu.bcvsolutions.idm.core.eav.service.impl.DefaultIdmFormDefinitionService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * EAV definition tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultFormDefinitionIntegrationTest extends AbstractIntegrationTest {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultFormDefinitionIntegrationTest.class);

	@Autowired private ApplicationContext context;
	@Autowired private IdmFormAttributeRepository formAttributeRepository;
	@Autowired private IdmFormAttributeService formAttributeService;
	//
	private IdmFormDefinitionService formDefinitionService;	
	
	private Random r = new Random();
	
	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		formDefinitionService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmFormDefinitionService.class);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	/**
	 * Creates definition
	 * 
	 * @param name
	 * @param randomAttributes with random count of attributes
	 */
	private ResultHolder createDefinition(String code, boolean randomAttributes, boolean log) {
		ResultHolder result = new ResultHolder();
		
		IdmFormDefinitionDto formDefinition = new IdmFormDefinitionDto();
		formDefinition.setType("test_type");
		formDefinition.setCode(code);
		if (log) {
			LOG.info("Before definition save [{}]", code);
		}
		long startTime = System.currentTimeMillis();
		formDefinition = formDefinitionService.save(formDefinition);
		if (log) {
			result.createTime = System.currentTimeMillis() - startTime;
			LOG.info("--- {}ms:  After definition save [{}]", result.createTime, code);
		}
		int attributeCount = r.nextInt(40);
		if (log) {
			LOG.info("Before definition [{}] attributes save, attributes count [{}]", code, attributeCount);
		}
		startTime = System.currentTimeMillis();
		for(int i = 0; i < attributeCount; i++) {
			IdmFormAttributeDto attributeDefinition = new IdmFormAttributeDto();
			attributeDefinition.setFormDefinition(formDefinition.getId());
			attributeDefinition.setCode("name_" + i);
			attributeDefinition.setName(attributeDefinition.getCode());
			attributeDefinition.setPersistentType(PersistentType.TEXT);			
			attributeDefinition = formAttributeService.save(attributeDefinition);
		}
		if (log) {
			result.childrenCreateTime = (double) System.currentTimeMillis() - startTime;
			LOG.info("--- {}ms:  After definition [{}] attributes save, attributes count [{}]", result.childrenCreateTime, code, attributeCount);
			if(attributeCount > 0) {
				result.childrenCreateTime = result.childrenCreateTime / attributeCount;
			}
			startTime = System.currentTimeMillis();
			int realAttributeCount = formAttributeRepository.findByFormDefinition_IdOrderBySeq(formDefinition.getId()).size();
			assertEquals(attributeCount, realAttributeCount);
			result.childrenLoadTime = System.currentTimeMillis() - startTime;
			LOG.info("--- {}ms:  After definition [{}] attributes load, attributes count [{}]", result.childrenLoadTime, code, realAttributeCount);
		}
		result.formDefinition = formDefinition;
		return result;
	}
	
	@Test
	@Transactional
	public void testDeleteDefinitionWithAttributes() {
		IdmFormDefinitionDto formDefinition = createDefinition("one", true, true).formDefinition;
		
		formDefinitionService.delete(formDefinition);
		
		assertEquals(0, formAttributeRepository.findByFormDefinition_IdOrderBySeq(formDefinition.getId()).size());
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
		public IdmFormDefinitionDto formDefinition;
		public double createTime;
		public double childrenCreateTime;
		public double childrenLoadTime;
	}
}
