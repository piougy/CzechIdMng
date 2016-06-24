package eu.bcvsolutions.idm.core.workflow.domain;

import java.util.List;

import org.activiti.bpmn.model.FieldExtension;
import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;

/**
 * 
 * @author svandav
 *
 */
public class CustomActivityBehaviorFactory extends DefaultActivityBehaviorFactory {


	@Override
	protected MailActivityBehavior createMailActivityBehavior(String taskId, List<FieldExtension> fields) {
		List<FieldDeclaration> fieldDeclarations = createFieldDeclarations(fields);
		CustomMailActivityBehavior customMailActivityBehavior = (CustomMailActivityBehavior) ClassDelegate.defaultInstantiateDelegate(CustomMailActivityBehavior.class, fieldDeclarations);
		return customMailActivityBehavior;
	}
}
