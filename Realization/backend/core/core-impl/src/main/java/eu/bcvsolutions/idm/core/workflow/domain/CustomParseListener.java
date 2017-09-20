package eu.bcvsolutions.idm.core.workflow.domain;

import java.util.Collection;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.parse.BpmnParseHandler;

public class CustomParseListener implements BpmnParseHandler {

	@Override
	public Collection<Class<? extends BaseElement>> getHandledTypes() {
		return null;
	}

	@Override
	public void parse(BpmnParse bpmnParse, BaseElement element) {
	}
	

}
