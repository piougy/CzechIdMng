package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormAttributeRenderer;

/**
 * Configurable tree node select attribute.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component(TreeNodeSelectFormAttributeRenderer.RENDERER_NAME)
public class TreeNodeSelectFormAttributeRenderer extends AbstractFormAttributeRenderer {
	
	public static final String RENDERER_NAME = BaseFaceType.TREE_NODE_SELECT;
	public static final String PARAMETER_TREE_TYPE = "tree-type";
	
	@Override
	public String getName() {
		return RENDERER_NAME;
	}
	
	@Override
	public PersistentType getPersistentType() {
		return PersistentType.UUID;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_TREE_TYPE);
		//
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		return Lists.newArrayList(
				new IdmFormAttributeDto(PARAMETER_TREE_TYPE, PARAMETER_TREE_TYPE, PersistentType.UUID, BaseFaceType.TREE_TYPE_SELECT)
		);
	}
}
