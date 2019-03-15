package eu.bcvsolutions.idm.rpt.report.identity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportRenderException;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractXlsxRenderer;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityRoleByRoleDeduplicationDto;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityRoleByRoleDeduplicationDuplicityDto;

/**
 * Renderer for report that is used for show duplicities
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 *
 */
@Component("identity-role-by-identity-deduplication-xlsx-renderer")
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION) // will be show as format for download
public class IdentityRoleByIdentityDeduplicationXlsxRenderer extends AbstractXlsxRenderer implements RendererRegistrar {

	@Override
	public InputStream render(RptReportDto report) {
		try {
			// read json stream
			JsonParser jParser = getMapper().getFactory().createParser(getReportData(report));
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Report");
			sheet.setDefaultColumnWidth(15);

			XSSFFont headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short)15);
			
			XSSFRichTextString headerColumn = new XSSFRichTextString();
			// header
			Row row = sheet.createRow(0);
			Cell cell = row.createCell(0);

			headerColumn = new XSSFRichTextString();
			headerColumn.append(StringUtils.capitalize(IdmIdentity_.username.getName()), headerFont);
			cell.setCellValue(headerColumn);

			headerColumn = new XSSFRichTextString();
			cell = row.createCell(1);
			headerColumn.append("Personal number", headerFont);
			cell.setCellValue(headerColumn);

			headerColumn = new XSSFRichTextString();
			cell = row.createCell(2);
			headerColumn.append(StringUtils.capitalize(IdmIdentity_.lastName.getName()), headerFont);
			cell.setCellValue(headerColumn);

			headerColumn = new XSSFRichTextString();
			cell = row.createCell(3);
			headerColumn.append(StringUtils.capitalize(IdmIdentity_.firstName.getName()), headerFont);
			cell.setCellValue(headerColumn);

			headerColumn = new XSSFRichTextString();
			cell = row.createCell(4);
			headerColumn.append("Contract", headerFont);
			cell.setCellValue(headerColumn);

			headerColumn = new XSSFRichTextString();
			cell = row.createCell(5);
			headerColumn.append("Duplicity", headerFont);
			cell.setCellValue(headerColumn);
		
			int rowNum = 1;

			// json is array of identities
			if (jParser.nextToken() == JsonToken.START_ARRAY) {
				// write single identity
				while (jParser.nextToken() == JsonToken.START_OBJECT) {
					RptIdentityRoleByRoleDeduplicationDto item = getMapper().readValue(jParser, RptIdentityRoleByRoleDeduplicationDto.class);
					
					IdmIdentityContractDto identityContract = item.getIdentityContract();
					IdmIdentityDto identity = item.getIdentity();
					
					StringBuilder duplicityRow = new StringBuilder();
					for (RptIdentityRoleByRoleDeduplicationDuplicityDto duplicity : item.getDuplicity()) {
						IdmRoleDto roleDto = duplicity.getRole();
						duplicityRow.append(roleDto.getName());
						duplicityRow.append(" (");
						duplicityRow.append(roleDto.getCode());
						duplicityRow.append(")");
						duplicityRow.append(" [");
						duplicityRow.append(ObjectUtils.nullSafeToString(duplicity.getValidFrom()));
						duplicityRow.append(" - ");
						duplicityRow.append(ObjectUtils.nullSafeToString(duplicity.getValidTill()));
						duplicityRow.append("]");
						duplicityRow.append(System.lineSeparator());
					}

					StringBuilder contract = new StringBuilder();
					contract.append(ObjectUtils.nullSafeToString(identityContract.getPosition()));
					contract.append(" [");
					contract.append(ObjectUtils.nullSafeToString(identityContract.getValidFrom()));
					contract.append(" - ");
					contract.append(ObjectUtils.nullSafeToString(identityContract.getValidTill()));
					contract.append("] ");
					contract.append(System.lineSeparator());
					contract.append(StringUtils.capitalize(IdmIdentityContract_.workPosition.getName()));
					contract.append(": ");
					
					StringBuilder treeNode = new StringBuilder("null");
					if (item.getWorkPosition() != null) {
						IdmTreeNodeDto treeNodeDto = item.getWorkPosition();
						// Just for sure
						if (treeNodeDto == null) {
							treeNode = new StringBuilder(identityContract.getWorkPosition().toString());
						} else {
							treeNode = new StringBuilder();
							treeNode.append(treeNodeDto.getName());
							treeNode.append(" (");
							treeNode.append(treeNodeDto.getCode());
							treeNode.append(")");
						}
					}
					
					contract.append(treeNode.toString());
					
					row = sheet.createRow(rowNum++);			
					cell = row.createCell(0);
					cell.setCellValue(identity.getUsername());
					cell = row.createCell(1);
					cell.setCellValue(identity.getExternalCode());
					cell = row.createCell(2);
					cell.setCellValue(identity.getLastName());
					cell = row.createCell(3);
					cell.setCellValue(identity.getFirstName());
					cell = row.createCell(4);
					cell.setCellValue(contract.toString());
					cell = row.createCell(5);
					cell.setCellValue(duplicityRow.toString());
				}
			}
			// close json stream
			jParser.close();

			for (int index = 0; index < 6; index++) {
				sheet.autoSizeColumn(index);
			}

			// close and return input stream
			return getInputStream(workbook);
		} catch (IOException ex) {
			throw new ReportRenderException(report.getName(), ex);
		}
	}

	/**
	 * Register renderer to report
	 */
	@Override
	public String[] register(String reportName) {
		if (IdentityRoleByIdentityDeduplicationExecutor.REPORT_NAME.equals(reportName)) {
			return new String[] { getName() };
		}
		return new String[] {};
	}
}
