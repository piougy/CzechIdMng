package eu.bcvsolutions.idm.core;

import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.bcvsolutions.idm.IdmApplication;

/**
 * 
 * Super class for activiti workflow tests
 * 
 * @author svandav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(IdmApplication.class)
@ActiveProfiles("test")
public abstract class AbstractWorkflowTest {

    @Autowired @Rule
    public ActivitiRule activitiRule;
}
