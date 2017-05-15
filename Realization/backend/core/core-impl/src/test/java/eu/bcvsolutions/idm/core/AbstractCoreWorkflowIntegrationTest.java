package eu.bcvsolutions.idm.core;

import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;

import eu.bcvsolutions.idm.core.workflow.domain.CustomActivityBehaviorFactory;
import eu.bcvsolutions.idm.test.api.AbstractWorkflowIntegrationTest;

/**
 * 
 * Base class for Activiti workflow tests in core module.
 * This class would be unnecessary if {@link CustomActivityBehaviorFactory}
 * class was moved to API module.
 * 
 * @author Jan Helbich
 *
 */
public abstract class AbstractCoreWorkflowIntegrationTest extends AbstractWorkflowIntegrationTest {

	@Override
	public DefaultActivityBehaviorFactory getBehaviourFactory() {
		return new CustomActivityBehaviorFactory();
	}

}
