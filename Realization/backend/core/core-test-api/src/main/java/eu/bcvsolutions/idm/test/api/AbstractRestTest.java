package eu.bcvsolutions.idm.test.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.core.Relation;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.GrantedAuthoritiesFactory;

/**
 * Abstract rest test - using mock mvc
 * 
 * @author Radek Tomiška
 *
 */
@Ignore
public abstract class AbstractRestTest extends AbstractIntegrationTest {

	private MockMvc mockMvc;
	private static String CONTENT_TYPE = "application/hal+json";
	//
	@Autowired private WebApplicationContext webApplicationContext;
	@Autowired private GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	@Autowired private ObjectMapper mapper;

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
	}

	/**
	 * Initialized mock mvc 
	 * 
	 * @return
	 */
	public MockMvc getMockMvc() {
		return mockMvc;
	}

	public String getJsonAsString(String endpoint, Authentication auth) throws UnsupportedEncodingException, Exception {
		return this.getJsonAsString(endpoint, null, null, null, null, null, auth);
	}

	public String getJsonAsString(String endpoint, String anotherParameters, Long size, Long page, String sort,
			String order, Authentication auth) throws UnsupportedEncodingException, Exception {

		return getMockMvc()
				.perform(get(BaseController.BASE_PATH + endpoint
						+ getPageableAndParameters(anotherParameters, size, page, sort, order))
								.with(authentication(auth)).contentType(CONTENT_TYPE))
				.andReturn().getResponse().getContentAsString();
	}

	public List<LinkedHashMap<String, Object>> getEmbeddedList(String nameEmbeddedList, String json) throws IOException {
		JSONObject tObject = new JSONObject(json);
		String embeddedString = tObject.get("_embedded").toString();
		tObject = new JSONObject(embeddedString);
		// get embedded list
		String listString = tObject.get(nameEmbeddedList).toString();
		//
		return getMapper().readValue(listString, new TypeReference<List<LinkedHashMap<String, Object>>>() {});
	}
	
	protected ObjectMapper getMapper() {
		return mapper;
	}
	
	/**
	 * Login as admin
	 * 
	 * @return
	 */
	protected Authentication getAdminAuthentication() {
		return getAuthentication(TestHelper.ADMIN_USERNAME);
	}
	
	/**
	 * Login identity
	 * 
	 * TODO: move to test helper
	 * 
	 * @return
	 */
	protected Authentication getAuthentication(String username) {
		return new IdmJwtAuthentication(
				getHelper().getService(IdmIdentityService.class).getByUsername(username), 
				null, 
				grantedAuthoritiesFactory.getGrantedAuthorities(username), 
				"test");
	}

	private String getPageableAndParameters(String anotherParameters, Long size, Long page, String sort, String order) {
		StringBuilder pageable = new StringBuilder();
		if (anotherParameters != null && !anotherParameters.isEmpty()) {
			pageable.append('?');
			pageable.append(anotherParameters);
		}
		if (size != null) {
			if (pageable.length() > 0) {
				pageable.append('&');
			} else {
				pageable.append('?');
			}
			pageable.append("size=");
			pageable.append(size);
		}
		if (page != null) {
			if (pageable.length() > 0) {
				pageable.append('&');
			} else {
				pageable.append('?');
			}
			pageable.append("page=");
			pageable.append(page);
		}
		if (sort != null) {
			if (pageable.length() > 0) {
				pageable.append('&');
			} else {
				pageable.append('?');
			}
			pageable.append("sort=");
			pageable.append(sort);
			if (order != null) {
				pageable.append(',');
				pageable.append(order);
			}
		}
		return pageable.toString();
	}
	
	/**
	 * Returns basic auth in base64 usable in rest header
	 * 
	 * @param user
	 * @param password
	 * @return
	 */
	protected String getBasicAuth(String user, String password) {
		return Base64.encodeBase64String((user + ":" + password).getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Returns dto's resource name defined by {@link Relation} annotation.
	 * 
	 * @param dtoClass
	 * @return
	 */
	protected String getResourcesName(Class<? extends BaseDto> dtoClass) {
		Relation mapping = dtoClass.getAnnotation(Relation.class);
		if (mapping == null) {
			throw new CoreException("Dto class [" + dtoClass + "] not have @Relation annotation! Configure dto annotation properly.");
		}
		return mapping.collectionRelation();
	}
	
	/**
	 * Converts filter parameters to string
	 * 
	 * @param filter
	 * @return
	 */
	protected MultiValueMap<String, String> toQueryParams(DataFilter filter) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		if (filter == null) {
			return queryParams;
		}
		//
		filter.getData().entrySet().forEach(entry -> {
			queryParams.put(
					entry.getKey(), 
					entry
						.getValue()
						.stream()
						.filter(Objects::nonNull)
						.map(Objects::toString)
						.collect(Collectors.toList())
						);
		});
		return queryParams;
	}
	
	/**
	 * Transform response with embedded dto list to dtos
	 * 
	 * @param listResponse
	 * @return
	 */
	protected <T extends BaseDto> List<T> toDtos(String listResponse, Class<T> dtoClass) {
		try {
			JsonNode json = getMapper().readTree(listResponse);
			JsonNode jsonEmbedded = json.get("_embedded"); // by convention
			JsonNode jsonResources = jsonEmbedded.get(getResourcesName(dtoClass));
			//
			// convert embedded object to target DTO classes
			List<T> results = new ArrayList<>();
			jsonResources.forEach(jsonResource -> {
				results.add(getMapper().convertValue(jsonResource, dtoClass));
			});
			//
			return results;
		} catch (Exception ex) {
			throw new RuntimeException("Failed parse entities from list response", ex);
		}
	}
}
