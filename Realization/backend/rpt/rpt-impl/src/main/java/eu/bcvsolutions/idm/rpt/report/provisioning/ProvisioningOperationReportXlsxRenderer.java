package eu.bcvsolutions.idm.rpt.report.provisioning;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.exception.ReportRenderException;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractXlsxRenderer;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;
import eu.bcvsolutions.idm.rpt.dto.RptProvisioningOperationDto;

/**
 * Render report of active provisioning operations into XLSX
 * 
 * @author Radek Tomiška
 *
 */
@Component("provisioningReportRenderer")
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION) // will be show as format for download
public class ProvisioningOperationReportXlsxRenderer extends AbstractXlsxRenderer implements RendererRegistrar {

	@Override
	public InputStream render(RptReportDto report) {
		try {
			// read json stream
			JsonParser jParser = getMapper().getFactory().createParser(getReportData(report));
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Report");
			// header
			Row row = sheet.createRow(0);
			Cell cell = row.createCell(0);
			cell.setCellValue("Created");
			cell = row.createCell(1);
			cell.setCellValue("System");
			cell = row.createCell(2);
			cell.setCellValue("Operation type");
			cell = row.createCell(3);
			cell.setCellValue("Entity type");
			cell = row.createCell(4);
			cell.setCellValue("Entity uid (system)");
			cell = row.createCell(5);
			cell.setCellValue("Entity identifier (IdM)");
			cell = row.createCell(6);
			cell.setCellValue("Attribute");
			cell = row.createCell(7);
			cell.setCellValue("Value");
			int rowNum = 1;
			//
			// json is array of identities
			if (jParser.nextToken() == JsonToken.START_ARRAY) {
				// write single identity
				while (jParser.nextToken() == JsonToken.START_OBJECT) {
					RptProvisioningOperationDto idmProvisioningOperationDto = getMapper().readValue(jParser, RptProvisioningOperationDto.class);
					if (idmProvisioningOperationDto.getProvisioningValues().isEmpty()) {
						row = sheet.createRow(rowNum++);
						createStartCells(row, idmProvisioningOperationDto);
					} else {
						for (Entry<String, String> attribute : idmProvisioningOperationDto.getProvisioningValues().entrySet()) {
							row = sheet.createRow(rowNum++);
							createStartCells(row, idmProvisioningOperationDto);							
							cell = row.createCell(6);
							cell.setCellValue(attribute.getKey());
							cell = row.createCell(7);
							cell.setCellValue(attribute.getValue());
						}
					}
				}
			}
			// close json stream
			jParser.close();
			//
			// close and return input stream
			return getInputStream(workbook);
		} catch (IOException ex) {
			throw new ReportRenderException(report.getName(), ex);
		}
	}
	
	private void createStartCells(Row row, RptProvisioningOperationDto idmProvisioningOperationDto) {
		Cell cell = row.createCell(0);
		cell.setCellValue(idmProvisioningOperationDto.getCreated().toString(getConfigurationService().getDateTimeFormat()));
		cell = row.createCell(1);
		cell.setCellValue(idmProvisioningOperationDto.getSystem());
		cell = row.createCell(2);
		cell.setCellValue(idmProvisioningOperationDto.getOperationType().name());
		cell = row.createCell(3);
		cell.setCellValue(idmProvisioningOperationDto.getSystemEntityType());
		cell = row.createCell(4);
		cell.setCellValue(idmProvisioningOperationDto.getSystemEntityUid());
		cell = row.createCell(5);
		cell.setCellValue(idmProvisioningOperationDto.getEntityIdentifier() == null ? "" : idmProvisioningOperationDto.getEntityIdentifier().toString());
	}
	
	/**
	 * Register renderer to ProvisioningReportExecutor report
	 */
	@Override
	public String[] register(String reportName) {
		if (ProvisioningOperationReportExecutor.REPORT_NAME.equals(reportName)) {
			return new String[] { getName() };
		}
		return new String[] {};
	}

}