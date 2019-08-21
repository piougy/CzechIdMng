package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Controller tests.
 * 
 * FIXME: move all tests to IdmRoleRequestControllerRestTest. Use {@link #find(org.springframework.util.MultiValueMap)} method.
 *
 * @author Kolychev Artem
 */
public class IdmRoleRequestFilterRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleRequestDto> {

	private static final String URL_PARAM_SIZE = "size";
	private static final String URL_PARAM_PAGE = "page";
	private static final String URL_PARAM_APPLICANT = "applicant";
	private static final String URL_PARAM_APPLICANTS = "applicants";

	private static final String URL_PARAM_FACE = "face";
	private static final String URL_PARAM_FACE_BETWEEN = "BETWEEN";
	private static final String URL_PARAM_CREATED_FROM = "createdFrom";
	private static final String URL_PARAM_CREATED_TILL = "createdTill";

	private static final String URL_PARAM_SYSTEM_STATES = "systemStates";
	private static final String URL_PARAM_STATES = "states";

	private static final String URL_PARAM_SORT = "sort";
	private static final String URL_PARAM_SORT_MODIFIED_ASC = "modified,asc";
	private static final String URL_PARAM_SORT_MODIFIED_DESC = "modified,desc";
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmRoleRequestController controller;

	/**
	 * Controller, which will be tested
	 *
	 * @return AbstractReadWriteDtoController
	 */
	@Override
	protected AbstractReadWriteDtoController<IdmRoleRequestDto, ?> getController() {
		return controller;
	}

	@Before
	public void init() {
	}

	protected boolean isReadOnly() {
		return true;
	}

	protected boolean supportsAutocomplete() {
		return false;
	}

	/**
	 * Prepare dto instance (not saved)
	 *
	 * @return IdmRoleRequestDto
	 */
	@Override
	protected IdmRoleRequestDto prepareDto() {
		IdmIdentityDto identityDto = getHelper().createIdentity();
		IdmRoleDto roleDto = getHelper().createRole();
		getHelper().createIdentityRole(identityDto, roleDto);
		return getHelper().createRoleRequest(identityDto);
	}

	private URIBuilder getBaseUrlBuilder() throws URISyntaxException {
		URIBuilder findQuickUrlBuilder = new URIBuilder(getFindQuickUrl());
		findQuickUrlBuilder
				.addParameter(URL_PARAM_SIZE, "10")
				.addParameter(URL_PARAM_PAGE, "0")
				.addParameter(URL_PARAM_SORT, URL_PARAM_SORT_MODIFIED_ASC);
		return findQuickUrlBuilder;
	}

	private List<IdmRoleRequestDto> sendReqest(@NotNull URIBuilder uriBuilder) throws Exception {
		String response = getMockMvc().perform(get(uriBuilder.build())
				.with(authentication(getAdminAuthentication()))
				.contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
				.andReturn()
				.getResponse()
				.getContentAsString();
		return toDtos(response);
	}

	@Test
	public void filterStates() throws Exception {

		List<IdmRoleRequestDto> idmRolesRequestDto = new ArrayList<>();
		URIBuilder baseUrlBuilder = this.getBaseUrlBuilder();

		idmRolesRequestDto.add(this.prepareDto());
		idmRolesRequestDto.get(0).setState(RoleRequestState.EXECUTED);
		baseUrlBuilder.addParameter(URL_PARAM_STATES, idmRolesRequestDto.get(0).getState().name());
		baseUrlBuilder.addParameter(URL_PARAM_APPLICANTS, idmRolesRequestDto.get(0).getApplicant().toString());

		idmRolesRequestDto.add(this.prepareDto());
		idmRolesRequestDto.get(1).setState(RoleRequestState.CANCELED);
		baseUrlBuilder.addParameter(URL_PARAM_STATES, idmRolesRequestDto.get(1).getState().name());
		baseUrlBuilder.addParameter(URL_PARAM_APPLICANTS, idmRolesRequestDto.get(1).getApplicant().toString());

		roleRequestService.saveAll(idmRolesRequestDto);

		List<IdmRoleRequestDto> results = sendReqest(baseUrlBuilder);
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(idmRolesRequestDto.get(0).getId(), results.get(0).getId());
		Assert.assertEquals(idmRolesRequestDto.get(1).getId(), results.get(1).getId());
	}

	@Test
	public void filterApplicant() throws Exception {

		List<IdmRoleRequestDto> idmRolesRequestDto = new ArrayList<>();
		URIBuilder baseUrlBuilder = this.getBaseUrlBuilder();

		IdmIdentityDto applicant1 = getHelper().createIdentity();
		IdmIdentityDto applicant2 = getHelper().createIdentity();

		idmRolesRequestDto.add(this.prepareDto());
		idmRolesRequestDto.get(0).setApplicant(applicant1.getId());
		baseUrlBuilder.addParameter(URL_PARAM_APPLICANTS, idmRolesRequestDto.get(0).getApplicant().toString());

		idmRolesRequestDto.add(this.prepareDto());
		idmRolesRequestDto.get(1).setApplicant(applicant2.getId());
		baseUrlBuilder.addParameter(URL_PARAM_APPLICANTS, idmRolesRequestDto.get(1).getApplicant().toString());

		roleRequestService.saveAll(idmRolesRequestDto);

		List<IdmRoleRequestDto> results = sendReqest(baseUrlBuilder);
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(idmRolesRequestDto.get(0).getId(), results.get(0).getId());
		Assert.assertEquals(idmRolesRequestDto.get(1).getId(), results.get(1).getId());

		baseUrlBuilder.setParameter(URL_PARAM_SORT, URL_PARAM_SORT_MODIFIED_DESC);
		results = sendReqest(baseUrlBuilder);
		Assert.assertEquals(idmRolesRequestDto.get(1).getId(), results.get(0).getId());
		Assert.assertEquals(idmRolesRequestDto.get(0).getId(), results.get(1).getId());

		baseUrlBuilder.addParameter(URL_PARAM_APPLICANT, idmRolesRequestDto.get(1).getApplicant().toString());
		results = sendReqest(baseUrlBuilder);
		Assert.assertEquals(idmRolesRequestDto.get(1).getId(), results.get(0).getId());

	}

	@Test
	public void filterSystemStates() throws Exception {

		List<IdmRoleRequestDto> idmRolesRequestDto = new ArrayList<>();
		URIBuilder baseUrlBuilder = this.getBaseUrlBuilder();

		idmRolesRequestDto.add(this.prepareDto());

		idmRolesRequestDto.get(0).setSystemState(new OperationResultDto(OperationState.RUNNING));
		baseUrlBuilder.addParameter(URL_PARAM_SYSTEM_STATES, idmRolesRequestDto.get(0).getSystemState().getState().name());
		baseUrlBuilder.addParameter(URL_PARAM_APPLICANTS, idmRolesRequestDto.get(0).getApplicant().toString());

		idmRolesRequestDto.add(this.prepareDto());
		idmRolesRequestDto.get(1).setSystemState(new OperationResultDto(OperationState.BLOCKED));
		baseUrlBuilder.addParameter(URL_PARAM_APPLICANTS, idmRolesRequestDto.get(1).getApplicant().toString());
		baseUrlBuilder.addParameter(URL_PARAM_SYSTEM_STATES, idmRolesRequestDto.get(1).getSystemState().getState().name());

		roleRequestService.saveAll(idmRolesRequestDto);

		List<IdmRoleRequestDto> results = sendReqest(baseUrlBuilder);
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(idmRolesRequestDto.get(0).getSystemState().getState(), OperationState.RUNNING);
		Assert.assertEquals(idmRolesRequestDto.get(1).getSystemState().getState(), OperationState.BLOCKED);

		baseUrlBuilder.setParameter(URL_PARAM_SYSTEM_STATES, OperationState.NOT_EXECUTED.name());
		results = sendReqest(baseUrlBuilder);
		Assert.assertEquals(0, results.size());
	}

	@Test
	public void filterModified() throws Exception {

		List<IdmRoleRequestDto> idmRolesRequestDto = new ArrayList<>();
		URIBuilder baseUrlBuilder = this.getBaseUrlBuilder();

		idmRolesRequestDto.add(this.prepareDto());
		idmRolesRequestDto.add(this.prepareDto());

		idmRolesRequestDto.get(0).setModified(new DateTime());
		idmRolesRequestDto.get(1).setModified(new DateTime().minusDays(5));
		roleRequestService.saveAll(idmRolesRequestDto);

		baseUrlBuilder.setParameter(URL_PARAM_FACE, URL_PARAM_FACE_BETWEEN);
		baseUrlBuilder.setParameter(URL_PARAM_CREATED_FROM, new DateTime().minusDays(3).toString(DATE_FORMAT));
		baseUrlBuilder.setParameter(URL_PARAM_CREATED_TILL, new DateTime().toString(DATE_FORMAT));
		baseUrlBuilder.addParameter(URL_PARAM_APPLICANTS, idmRolesRequestDto.get(0).getApplicant().toString());
		baseUrlBuilder.addParameter(URL_PARAM_APPLICANTS, idmRolesRequestDto.get(1).getApplicant().toString());

		List<IdmRoleRequestDto> roleRequestDtos;
		roleRequestDtos = sendReqest(baseUrlBuilder);
		Assert.assertEquals(2, roleRequestDtos.size());
	}
}
