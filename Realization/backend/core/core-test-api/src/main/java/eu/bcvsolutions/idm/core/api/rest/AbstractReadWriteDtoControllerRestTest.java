package eu.bcvsolutions.idm.core.api.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.hateoas.core.Relation;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Basic dto CRUD tests
 * - TODO: permissions, count
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public abstract class AbstractReadWriteDtoControllerRestTest<DTO extends AbstractDto> extends AbstractRestTest {	
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractReadWriteDtoControllerRestTest.class);
	
	@Before
	public void setup() throws Exception {
		super.setup();
	}

	@After
	public void logout() {
		// just for sure
		super.logout();
	}
	
	/**
	 * Controller, which will be tested
	 * 
	 * @return
	 */
	protected abstract AbstractReadWriteDtoController<DTO, ?> getController();
	
	/**
	 * Prepare dto instance (not saved)
	 * 
	 * @return
	 */
	protected abstract DTO prepareDto();
	
	/**
	 * True - patch method will be tested
	 * 
	 * @return
	 */
	protected boolean supportsPatch() {
		return true;
	}
	
	/**
	 * True - delete method will be tested
	 * @return
	 */
	protected boolean supportsDelete() {
		return true;
	}
	
	/**
	 * True - autocomplete method will be tested
	 * @return
	 */
	protected boolean supportsAutocomplete() {
		return true;
	}
	
	/**
	 * Login as admin
	 * 
	 * @return
	 */
	protected Authentication getAdminAuthentication() {
		return new IdmJwtAuthentication(
				getHelper().getService(IdmIdentityService.class).getByUsername(TestHelper.ADMIN_USERNAME), 
				null, 
				Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()), 
				"test");
	}
	
	/**
	 * Creates dto (saved)
	 * 
	 * @return
	 */
	protected DTO createDto() {
		return createDto(prepareDto());
	}
	
	/**
	 * Creates dto (saved). Auth internally.
	 * 
	 * @param dto
	 * @return
	 */
	protected DTO createDto(DTO dto) {
		try {
			getHelper().loginAdmin();
			return getController().saveDto(dto);
		} finally {
			getHelper().logout();
		}
	}
	
	/**
	 * Get dto by id. Auth internally.
	 * 
	 * @param id
	 * @return
	 */
	protected DTO getDto(UUID id) {
		try {
			getHelper().loginAdmin();
			return getController().getDto(id);
		} finally {
			getHelper().logout();
		}
	}
	
	protected String getBaseUrl() {
		Class<?> clazz = AopUtils.getTargetClass(getController());
	 
    	RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
    	if (mapping.value().length > 0) {
    		return mapping.value()[0];
    	}
    	if (mapping.path().length > 0) {
    		return mapping.path()[0];
    	}
		throw new CoreException("Controller [" + clazz + "] doeasn't have default mapping, cannot be tested by this abstraction.");
	}
	
	protected String getAutocompleteUrl() {
		return String.format("%s%s", getBaseUrl(), "/search/autocomplete"); 
	}
	
	protected String getDetailUrl(Serializable backendId) {
		return String.format("%s/%s", getBaseUrl(), backendId);
	}
	
	protected String getResourcesName() {
		Relation mapping = getController().getDtoClass().getAnnotation(Relation.class);
		if (mapping == null) {
			throw new CoreException("Dto class [" + getController().getDtoClass() + "] not have @Relation annotation! Configure dto annotation properly.");
		}
		return mapping.collectionRelation();
	}
	
	@Test
	public void testGet() throws Exception {
		DTO dto = createDto();
		// by code
		if (dto instanceof Codeable) {
			getMockMvc().perform(get(getDetailUrl(((Codeable)dto).getCode()))
	        		.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andExpect(jsonPath("$.id", equalTo(dto.getId().toString())));
		}
		// by id
		getMockMvc().perform(get(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.id", equalTo(dto.getId().toString())));
	}
	
	@Test
    public void notFound() throws Exception {
		getMockMvc().perform(get(getDetailUrl(UUID.randomUUID()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
    }
	
	@Test
	@SuppressWarnings("unchecked")
	public void testPost() throws Exception {
		DTO dto = prepareDto();
		//
		String response = getMockMvc().perform(post(getBaseUrl())
        		.with(authentication(getAdminAuthentication()))
        		.content(getController().getMapper().writeValueAsString(dto))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isCreated())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		DTO createdDto = (DTO) getController().getMapper().readValue(response, dto.getClass());
		Assert.assertNotNull(createdDto);
		Assert.assertNotNull(createdDto.getId());
		//
		createdDto = getDto(createdDto.getId());
		Assert.assertEquals(createdDto.getId(), createdDto.getId());
		Assert.assertNotNull(createdDto.getCreator());
		Assert.assertNotNull(createdDto.getCreatorId());
		Assert.assertNotNull(createdDto.getCreated());
		//
		// TODO: realm, transaction id
	}
	
	@Test
	public void testPut() throws Exception {
		DTO dto = createDto();
		//
		getMockMvc().perform(put(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
        		.content(getController().getMapper().writeValueAsString(dto))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE));
		dto = getDto(dto.getId());
		//
		Assert.assertNotNull(dto.getModifier());
		Assert.assertNotNull(dto.getModified());
		Assert.assertNotNull(dto.getModifierId());
		//
		// TODO: modify some common prop (code / external id / external code)
	}
	
	@Test
	public void testPatch() throws Exception {
		if (!supportsPatch()) {
			LOG.info("Controller [{}] doesn't support PATCH method. Mentod will not be tested.", getController().getClass());
			return;
		}
		DTO dto = createDto();
		//
		getMockMvc().perform(patch(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
        		.content(getController().getMapper().writeValueAsString(dto))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE));
		dto = getDto(dto.getId());
		//
		Assert.assertNotNull(dto.getModifier());
		Assert.assertNotNull(dto.getModified());
		Assert.assertNotNull(dto.getModifierId());
		//
		// TODO: modify some common prop (code / external id / external code)
	}

	@Test
	public void testDelete() throws Exception {
		if (!supportsDelete()) {
			LOG.info("Controller [{}] doesn't support DELETE method. Mentod will not be tested.", getController().getClass());
			return;
		}
		DTO dto = createDto();
		DTO getDto = getDto(dto.getId());
		//
		Assert.assertNotNull(getDto);
		Assert.assertEquals(dto.getId(), getDto.getId());
		//
		getMockMvc().perform(delete(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNoContent());
		//
		getMockMvc().perform(get(getDetailUrl(dto.getId()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
	}
	
	protected List<DTO> find(MultiValueMap<String, String> parameters) {
		try {
			String response = getMockMvc().perform(get(getBaseUrl())
	        		.with(authentication(getAdminAuthentication()))
	        		.params(parameters)
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	protected List<DTO> autocomplete(MultiValueMap<String, String> parameters) {
		try {
			String response = getMockMvc().perform(get(getAutocompleteUrl())
	        		.with(authentication(getAdminAuthentication()))
	        		.params(parameters)
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	protected List<DTO> toDtos(String listResponse) {
		try {
			JsonNode json = getController().getMapper().readTree(listResponse);
			JsonNode jsonEmbedded = json.get("_embedded"); // by convention
			JsonNode jsonResources = jsonEmbedded.get(getResourcesName());
			//
			// convert embedded object to target DTO classes
			List<DTO> results = new ArrayList<>();
			jsonResources.forEach(jsonResource -> {
				results.add(getController().getMapper().convertValue(jsonResource, getController().getDtoClass()));
			});
			//
			return results;
		} catch (Exception ex) {
			throw new RuntimeException("Failed parse entities from list response", ex);
		}
	}
	
	/**
	 * Test search by external identifier, if DTO implements {@link ExternalIdentifiable}
	 * 
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testFindByExternalId() {		
		DTO dto = prepareDto();
		if (!(dto instanceof ExternalIdentifiable)) {
			// ignore test
			return;
		}
		
		ExternalIdentifiable externalIdentifiableDto = (ExternalIdentifiable) dto;
		externalIdentifiableDto.setExternalId(getHelper().createName());
		//
		DTO createdDto = createDto(dto);
		//
		// create another mock dto
		ExternalIdentifiable dtoTwo = (ExternalIdentifiable) prepareDto();
		dtoTwo.setExternalId(getHelper().createName());
		createDto((DTO) dtoTwo);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(ExternalIdentifiable.PROPERTY_EXTERNAL_ID, externalIdentifiableDto.getExternalId());
		//
		List<DTO> results = find(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		if (supportsAutocomplete()) {
			results = autocomplete(parameters);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		} else {
			LOG.info("Controller [{}] doesn't support autocomplete method. Mentod will not be tested.", getController().getClass());
		}
	}
	
	/**
	 * Test search by external identifier, if DTO implements {@link ExternalIdentifiable}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindByCodeable() {		
		DTO dto = prepareDto();
		if (!(dto instanceof Codeable)) {
			// ignore test
			return;
		}
		Codeable codeable = (Codeable) dto;
		if (StringUtils.isEmpty(codeable.getCode())) {
			throw new CoreException("Code has to be set by #prepareDto method, its required by default");
		}
		//
		DTO createdDto = createDto(dto);
		// mock dto
		createDto(prepareDto());
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(DataFilter.PARAMETER_CODEABLE_IDENTIFIER, codeable.getCode());
		//
		List<DTO> results = find(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		if (supportsAutocomplete()) {
			results = autocomplete(parameters);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		} else {
			LOG.info("Controller [{}] doesn't support autocomplete method. Mentod will not be tested.", getController().getClass());
		}
	}
	
	/**
	 * Test search by id - supported by default, id DataFilter is used (see #toPedicates in services - has to call super implementation)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFindById() {		
		DTO dto = prepareDto();
		//
		DTO createdDto = createDto(dto);
		// mock dto
		createDto(prepareDto());
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set(DataFilter.PARAMETER_ID, createdDto.getId().toString());
		//
		List<DTO> results = find(parameters);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		//
		if (supportsAutocomplete()) {
			results = autocomplete(parameters);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(createdDto.getId(), results.get(0).getId());
		} else {
			LOG.info("Controller [{}] doesn't support autocomplete method. Mentod will not be tested.", getController().getClass());
		}
	}
	
	@Test
	public void testDeleteIdentityNotExists() throws Exception {
		if (!supportsDelete()) {
			LOG.info("Controller [{}] doesn't support DELETE method. Mentod will not be tested.", getController().getClass());
			return;
		}
		getMockMvc().perform(delete(getDetailUrl(UUID.randomUUID()))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());
	}
	
}
