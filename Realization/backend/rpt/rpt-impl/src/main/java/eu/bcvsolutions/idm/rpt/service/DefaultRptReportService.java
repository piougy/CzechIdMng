package eu.bcvsolutions.idm.rpt.service;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.service.CommonFormService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.rpt.api.domain.RptGroupPermission;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import eu.bcvsolutions.idm.rpt.entity.RptReport;
import eu.bcvsolutions.idm.rpt.entity.RptReport_;
import eu.bcvsolutions.idm.rpt.repository.RptReportRepository;

/**
 * CRUD for generated reports
 * 
 * @author Radek Tomiška
 *
 */
@Service("rptReportService")
public class DefaultRptReportService 
	extends AbstractReadWriteDtoService<RptReportDto, RptReport, RptReportFilter>
	implements RptReportService {
	
	private final CommonFormService commonFormService;
	private final AttachmentManager attachmentManager;

	@Autowired
	public DefaultRptReportService(
			RptReportRepository repository, 
			CommonFormService commonFormService,
			AttachmentManager attachmentManager) {
		super(repository);
		//
		Assert.notNull(commonFormService);
		Assert.notNull(attachmentManager);
		//
		this.commonFormService = commonFormService;
		this.attachmentManager = attachmentManager;
	}
	
	@Override
	@Transactional
	public RptReportDto saveInternal(RptReportDto report) {
		boolean isNew = isNew(report);
		IdmFormDto filter = report.getFilter();
		report = super.saveInternal(report);
		// save report filter
		if (filter != null) {
			if (isNew) {
				// clone filter - filter template can be given
				// TODO: clone filter properly
				filter.setId(null);
				DtoUtils.clearAuditFields(filter);
				filter.getValues().forEach(formValue -> {
					formValue.setId(null);
					DtoUtils.clearAuditFields(formValue);
				});			
			}
			filter.setName(report.getName());
			filter.setOwnerCode(report.getExecutorName());
			report.setFilter(commonFormService.saveForm(report, filter));
		} else {
			// TODO: remove previous filter - find and destroy			
		}
		//
		return report;
	}
	
	@Override
	@Transactional
	public void deleteInternal(RptReportDto dto) {
		// delete filter
		commonFormService.deleteForms(dto);
		// delete attachments
		attachmentManager.deleteAttachments(dto);
		// delete report
		super.deleteInternal(dto);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(RptGroupPermission.REPORT, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<RptReport> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			RptReportFilter filter) {
		List<Predicate> predicates =  super.toPredicates(root, query, builder, filter);
		//
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.like(builder.lower(root.get(RptReport_.name)), "%" + filter.getText().toLowerCase() + "%"));
		}
		if (filter.getFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(RptReport_.created), filter.getFrom()));
		}
		if (filter.getTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(RptReport_.created), filter.getTill().plusDays(1)));
		}
		//
		return predicates;
	}
	
	@Override
	protected RptReportDto toDto(RptReport entity, RptReportDto dto) {
		dto =  super.toDto(entity, dto);
		if (dto != null) {
			dto.setFilter(getFilter(dto));
		}
		return dto;
	}
	
	/**
	 * Generated report can have only one filter
	 * 
	 * @param reportId
	 * @return
	 */
	private IdmFormDto getFilter(RptReportDto report) {
		List<IdmFormDto> filters = commonFormService.getForms(report);
		if (filters.isEmpty()) {
			return null;
		}
		return filters.get(0);
	}
}
