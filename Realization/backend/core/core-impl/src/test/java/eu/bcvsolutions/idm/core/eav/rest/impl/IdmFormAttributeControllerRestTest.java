package eu.bcvsolutions.idm.core.eav.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.FormAttributeRendererDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.TreeNodeSelectFormAttributeRenderer;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Controller tests
 * - CRUD
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFormAttributeControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmFormAttributeDto> {

	@Autowired private IdmFormAttributeController controller;
	@Autowired private FormService formService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmFormAttributeDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmFormAttributeDto prepareDto() {
		IdmFormDefinitionDto formDefinition = formService.createDefinition(IdmIdentityDto.class, getHelper().createName(), null);
		IdmFormAttributeDto dto = new IdmFormAttributeDto();
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		dto.setFormDefinition(formDefinition.getId());
		dto.setPersistentType(PersistentType.SHORTTEXT);
		//
		return dto;
	}
	
	@Test
	public void testSupportedAttributeRenderers() throws Exception {		
		String response = getMockMvc().perform(get(getFindUrl("supported-attribute-renderers"))
				.with(authentication(getAdminAuthentication()))
	            .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		List<FormAttributeRendererDto> supportedAttributeRenderers = toDtos(response, FormAttributeRendererDto.class);
		//
		Assert.assertFalse(supportedAttributeRenderers.isEmpty());
		Assert.assertTrue(supportedAttributeRenderers.stream().anyMatch(r -> r.getId().equals(TreeNodeSelectFormAttributeRenderer.RENDERER_NAME)));
	}
}
