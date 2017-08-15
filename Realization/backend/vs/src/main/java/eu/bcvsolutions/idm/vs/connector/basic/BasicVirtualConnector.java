package eu.bcvsolutions.idm.vs.connector.basic;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormDefinitionService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorCreate;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcConnectorRead;
import eu.bcvsolutions.idm.ic.api.IcConnectorSchema;
import eu.bcvsolutions.idm.ic.api.IcConnectorUpdate;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.api.annotation.IcConnectorClass;
import eu.bcvsolutions.idm.ic.czechidm.domain.CzechIdMIcConvertUtil;
import eu.bcvsolutions.idm.ic.czechidm.domain.IcConnectorConfigurationCzechIdMImpl;
import eu.bcvsolutions.idm.ic.exception.IcException;
import eu.bcvsolutions.idm.ic.impl.IcSchemaImpl;

// @Component
@IcConnectorClass(displayName = "Virtual system for CzechIdM", framework = "czechidm", name = "virtual-system-basic", version = "0.2.0", configurationClass = BasicVirtualConfiguration.class)
public class BasicVirtualConnector
		implements IcConnector, IcConnectorRead, IcConnectorCreate, IcConnectorUpdate, IcConnectorSchema {
	
	
	@Autowired FormService formService;

	@Override
	public void init(IcConnectorConfiguration configuration) {
		Assert.notNull(configuration);
		if (!(configuration instanceof IcConnectorConfigurationCzechIdMImpl)) {
			throw new IcException(
					MessageFormat.format("Connector configuration for virtual system must be instance of [{0}]",
							IcConnectorConfigurationCzechIdMImpl.class.getName()));
		}
		
		UUID systemId = ((IcConnectorConfigurationCzechIdMImpl)configuration).getSystemId(); 
		if(systemId == null){
			throw new IcException("System ID cannot be null (for virtual system)");
		}
		
		IcConnectorClass connectorAnnotation = this.getClass().getAnnotation(IcConnectorClass.class);
		IcConnectorInfo info = CzechIdMIcConvertUtil.convertConnectorClass(connectorAnnotation, this.getClass());
		String key = MessageFormat.format("{0}:systemId={1}",info.getConnectorKey().getFullName(),systemId.toString());
		
		
		List<IdmFormAttribute> formAttributes = new ArrayList<>();
//		for (short seq = 0; seq < conf.getConfigurationProperties().getProperties().size(); seq++) {
//			IcConfigurationProperty property = conf.getConfigurationProperties().getProperties().get(seq);
//			IdmFormAttribute attribute = formPropertyManager.toFormAttribute(property);
//			attribute.setSeq(seq);
//			formAttributes.add(attribute);
//		}
		this.formService.createDefinition(this.getClass().getName(), key, formAttributes);
	}

	@Override
	public IcUidAttribute update(IcUidAttribute uid, IcObjectClass objectClass, List<IcAttribute> attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IcUidAttribute create(IcObjectClass objectClass, List<IcAttribute> attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IcConnectorObject read(IcUidAttribute uid, IcObjectClass objectClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IcSchema schema() {
		IcSchema schema = new IcSchemaImpl();
		return schema;
	}
}
