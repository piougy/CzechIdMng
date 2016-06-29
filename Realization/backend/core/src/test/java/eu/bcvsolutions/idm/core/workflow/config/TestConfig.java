package eu.bcvsolutions.idm.core.workflow.config;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.test.ActivitiRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

	/**
	 * For test Activiti engine
	 * @param processEngine
	 * @return
	 */
    @Bean
    public ActivitiRule activitiRule(ProcessEngine processEngine) {
        return new ActivitiRule(processEngine);
    }
}