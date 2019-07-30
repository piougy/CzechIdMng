package eu.bcvsolutions.idm.rpt.report.identity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
import eu.bcvsolutions.idm.rpt.dto.RptIdentityWithFormValueDto;

/**
 * Report for identity with chosen eav - xlsx renderer
 *
 * @author Marek Klement
 */
@Component("identityEavReportRenderer")
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION) // will be show as format for download
public class IdentityEavReportXlsxRenderer extends AbstractXlsxRenderer
		implements RendererRegistrar {

	@Override
	public InputStream render(RptReportDto report) {
		try {
			// read json stream
			JsonParser jParser = getMapper().getFactory().createParser(getReportData(report));
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Report");
			//
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setWrapText(true);
			// header
			Row row = sheet.createRow(0);
			Cell cell = row.createCell(0);
			cell.setCellValue("External code");
			cell = row.createCell(1);
			cell.setCellValue("Username");
			cell = row.createCell(2);
			cell.setCellValue("Title before");
			cell = row.createCell(3);
			cell.setCellValue("First name");
			cell = row.createCell(4);
			cell.setCellValue("Last name");
			cell = row.createCell(5);
			cell.setCellValue("Title after");
			cell = row.createCell(6);
			cell.setCellValue("Disabled");
			cell = row.createCell(7);
			cell.setCellValue("Form value");
			cell.setCellStyle(cellStyle);
			int rowNum = 1;
			//
			// json is array of identities
			if (jParser.nextToken() == JsonToken.START_ARRAY) {
				// write single identity
				while (jParser.nextToken() == JsonToken.START_OBJECT) {
					RptIdentityWithFormValueDto item = getMapper().readValue(jParser,
							RptIdentityWithFormValueDto.class);
					//
					rowNum = createRow(item, rowNum,sheet);
				}
			}
			// close json stream
			jParser.close();
			// close and return input stream
			return getInputStream(workbook);
		} catch (IOException ex) {
			throw new ReportRenderException(report.getName(), ex);
		}
	}

	private int createRow(RptIdentityWithFormValueDto item, int rowNum, XSSFSheet sheet){
		Row row = sheet.createRow(rowNum++);
		Cell cell = row.createCell(0);
		cell.setCellValue(item.getExternalCode());
		cell = row.createCell(1);
		cell.setCellValue(item.getUsername());
		cell = row.createCell(2);
		cell.setCellValue(item.getTitleBefore());
		cell = row.createCell(3);
		cell.setCellValue(item.getFirstName());
		cell = row.createCell(4);
		cell.setCellValue(item.getLastName());
		cell = row.createCell(5);
		cell.setCellValue(item.getTitleAfter());
		cell = row.createCell(6);
		cell.setCellValue(item.isDisabled());
		cell = row.createCell(7);
		if(!item.getFormValue().isEmpty()){
			String val = item.getFormValue().get(0);
			item.getFormValue().remove(0);
			cell.setCellValue(val);
		}
		if(!item.getFormValue().isEmpty()){
			return createRow(item, rowNum,sheet);
		}
		return rowNum;
	}

	@Override
	public String[] register(String reportName) {
		if (IdentityEavReportExecutor.REPORT_NAME.equals(reportName)) {
			return new String[]{getName()};
		}
		return new String[]{};
	}
}
