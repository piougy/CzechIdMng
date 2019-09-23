package eu.bcvsolutions.idm.rpt.report.identity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.bulk.action.impl.IdentityRoleByIdentityDeduplicationBulkAction;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportGenerateException;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityRoleByRoleDeduplicationDto;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityRoleByRoleDeduplicationDuplicityDto;

/**
 * Report that is used for show duplicities
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 *
 */
@Component(value = IdentityRoleByIdentityDeduplicationExecutor.REPORT_NAME)
@Enabled(RptModuleDescriptor.MODULE_ID)
@Description("Identity role by identity deduplication.")
public class IdentityRoleByIdentityDeduplicationExecutor extends AbstractReportExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(IdentityRoleByIdentityDeduplicationExecutor.class);

	public static final String REPORT_NAME = "identity-role-by-identity-deduplication-report";

	public static final String PARAMETER_TREE_NODE = "treeNode";
	
	@Autowired
	private IdentityRoleByIdentityDeduplicationBulkAction deduplicationBulkAction;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService identityContractService;

	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		IdmIdentityFilter filter = new IdmIdentityFilter();
		UUID treeNode = getTreeNode(report);
		if (treeNode != null) {
			filter.setTreeNode(treeNode);
			filter.setRecursively(true);
		} else {
			LOG.info("Tree node id isn't filled. Report will be done for all identities.");
		}

		File temp = null;
		FileOutputStream outputStream = null;
		try {
			// prepare temp file for json stream
			temp = getAttachmentManager().createTempFile();
	        outputStream = new FileOutputStream(temp);
	        // write into json stream
			JsonGenerator jGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			try {
				// json will be array of identities
				jGenerator.writeStartArray();		

				counter = 0L;
				List<UUID> identities = identityService.findIds(filter, null, IdmBasePermission.READ).getContent();
				if (count == null) {
					count = Long.valueOf(identities.size());
				}

				boolean canContinue = true;

				for (UUID identityId : identities) {
					List<IdmIdentityContractDto> contracts = identityContractService.findAllValidForDate(identityId,
							LocalDate.now(), null);
					for (IdmIdentityContractDto contract : contracts) {
						RptIdentityRoleByRoleDeduplicationDto newRecord = createRecordForContracts(contract);
						if (newRecord != null) {
							getMapper().writeValue(jGenerator, newRecord);
						}
					}

					++counter;
					canContinue = updateState();

					if (!canContinue) {
						break;
					}
				}
				// close array of identities
				jGenerator.writeEndArray();
			} finally {
				// close json stream
				jGenerator.close();
			}
			// save create temp file with array of identities in json as attachment
			return createAttachment(report, new FileInputStream(temp));
		} catch (IOException ex) {
			throw new ReportGenerateException(report.getName(), ex);
		} finally {
			IOUtils.closeQuietly(outputStream); // just for sure - jGenerator should close stream itself
			FileUtils.deleteQuietly(temp);
		}
	}

	@Override
	public String getName() {
		return REPORT_NAME;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto treeNode = new IdmFormAttributeDto(PARAMETER_TREE_NODE, "Treenode",
				PersistentType.UUID);
		treeNode.setFaceType(BaseFaceType.TREE_NODE_SELECT);
		treeNode.setDescription("Filtering users only on this organization unit and recursively down.");
		//
		return Lists.newArrayList(treeNode);
	}

	/**
	 * Get selected tree node and recursively down
	 *
	 * @param report
	 * @return
	 */
	private UUID getTreeNode(RptReportDto report) {
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(report, getFormDefinition(), report.getFilter());
		Serializable treeNodeIdAsSerializable = formInstance.toSinglePersistentValue(PARAMETER_TREE_NODE);
		if (treeNodeIdAsSerializable == null) {
			return null;
		}

		return UUID.fromString(treeNodeIdAsSerializable.toString());
	}

	/**
	 * Create new record to report for given contract.
	 *
	 * @param contract
	 * @return
	 */
	private RptIdentityRoleByRoleDeduplicationDto createRecordForContracts(IdmIdentityContractDto contract) {
		List<IdmIdentityRoleDto> duplicityIdentityRoles = deduplicationBulkAction
				.getDuplicatesIdentityRoleForContract(contract);
		
		if (duplicityIdentityRoles.isEmpty()) {
			return null;
		}
		
		List<RptIdentityRoleByRoleDeduplicationDuplicityDto> duplicity = new ArrayList<>(duplicityIdentityRoles.size());
		for (IdmIdentityRoleDto duplicityIdentityRole : duplicityIdentityRoles) {
			RptIdentityRoleByRoleDeduplicationDuplicityDto dupl = new RptIdentityRoleByRoleDeduplicationDuplicityDto();
			dupl.setValidFrom(duplicityIdentityRole.getValidFrom());
			dupl.setValidTill(duplicityIdentityRole.getValidTill());
			IdmRoleDto roleDto = DtoUtils.getEmbedded(duplicityIdentityRole, IdmIdentityRole_.role, IdmRoleDto.class, null);
			dupl.setRole(roleDto);
			duplicity.add(dupl);
		}

		RptIdentityRoleByRoleDeduplicationDto item = new RptIdentityRoleByRoleDeduplicationDto();
		
		IdmTreeNodeDto treeNodeDto = DtoUtils.getEmbedded(contract, IdmIdentityContract_.workPosition, IdmTreeNodeDto.class, null);
		IdmIdentityDto identityDto = DtoUtils.getEmbedded(contract, IdmIdentityContract_.identity, IdmIdentityDto.class, null);

		item.setIdentity(identityDto);
		item.setWorkPosition(treeNodeDto);
		item.setIdentityContract(contract);
		item.setDuplicity(duplicity);
		return item;
	}
}
