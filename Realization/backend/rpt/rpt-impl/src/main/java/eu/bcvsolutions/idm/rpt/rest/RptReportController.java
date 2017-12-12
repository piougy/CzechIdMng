package eu.bcvsolutions.idm.rpt.rest;

import java.io.InputStream;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.domain.RptGroupPermission;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportExecutorDto;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Report controller
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(RptModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/" + RptModuleDescriptor.MODULE_ID + "/reports")
@Api(
		value = RptReportController.TAG,  
		tags = { RptReportController.TAG }, 
		description = "Reports",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class RptReportController extends AbstractReadWriteDtoController<RptReportDto, RptReportFilter>  {

	protected static final String TAG = "Reports";
	//
	private final ReportManager reportManager;
	
	@Autowired
	public RptReportController(
			RptReportService service,
			ReportManager reportManager) {
		super(service);
		//
		this.reportManager = reportManager;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')")
	@ApiOperation(
			value = "Search reports (/search/quick alias)", 
			nickname = "searchReports", 
			tags = { RptReportController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')")
	@ApiOperation(
			value = "Search reports", 
			nickname = "searchQuickReports", 
			tags = { RptReportController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete reports (selectbox usage)", 
			nickname = "autocompleteReports", 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')")
	@ApiOperation(
			value = "Report detail", 
			nickname = "getReport", 
			response = RptReportDto.class, 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Report's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_CREATE + "')")
	@ApiOperation(
			value = "Create report", 
			nickname = "createReport", 
			response = RptReportDto.class, 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_CREATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_CREATE, description = "")})
				})
	public ResponseEntity<?> createReport(@Valid @RequestBody RptReportDto report) {
		checkAccess(report, IdmBasePermission.CREATE);
		//
		return new ResponseEntity<>(toResource(reportManager.generate(report)), HttpStatus.ACCEPTED);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_DELETE + "')")
	@ApiOperation(
			value = "Delete report", 
			nickname = "deleteReport", 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Report's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')"
			+ " or hasAuthority('" + RptGroupPermission.REPORT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnReport", 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = ""),
						@AuthorizationScope(scope = RptGroupPermission.REPORT_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = ""),
						@AuthorizationScope(scope = RptGroupPermission.REPORT_AUTOCOMPLETE, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Report's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/render", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> renderReport(
			@ApiParam(value = "Report's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Renderer's identifier.", required = true)
			@RequestParam(required = true, name = "renderer") @NotNull String rendererName) {
		//
		RptReportDto report = getDto(backendId);
		if (report == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		try {
			RptRenderedReportDto result = reportManager.render(report, rendererName);
			InputStream is = result.getRenderedReport();
			//
			
			String reportName = report.getExecutorName() + "-" + report.getCreated().toString("yyyyMMddHHmmss");
			return ResponseEntity.ok()
					.contentLength(is.available())
					.contentType(result.getRenderer().getFormat())
					.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s.%s\"", reportName, result.getRenderer().getExtension()))
					.body(new InputStreamResource(is));
		} catch (Exception ex) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, ex);
		}
	}
	
	@ResponseBody
	@RequestMapping(path = "/search/supported", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_CREATE + "')")
	@ApiOperation(
			value = "Get supported reports", 
			nickname = "getSupportedReports", 
			tags={ RptReportController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_CREATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_CREATE, description = "") })
				})
	public Resources<RptReportExecutorDto> find() {
		return new Resources<>(reportManager.getExecutors());
	}
	
	@Override
	protected RptReportFilter toFilter(MultiValueMap<String, Object> parameters) {
		RptReportFilter filter = new RptReportFilter(parameters);
		filter.setFrom(getParameterConverter().toDateTime(parameters, "from"));
		filter.setTill(getParameterConverter().toDateTime(parameters, "till"));
		return filter;
	}

}
