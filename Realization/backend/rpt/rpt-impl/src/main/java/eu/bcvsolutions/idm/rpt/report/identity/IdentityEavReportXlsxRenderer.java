package eu.bcvsolutions.idm.rpt.report.identity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
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

@Component("identityEavReportRenderer")
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION) // will be show as format for download
public class IdentityEavReportXlsxRenderer extends AbstractXlsxRenderer
		implements RendererRegistrar {

	private String eavName;


	@Override
	public InputStream render(RptReportDto report) {
		try {
			// read json stream
			JsonParser jParser = getMapper().getFactory().createParser(getReportData(report));
			XSSFWorkbook workbook = new XSSFWorkbook();
			CreationHelper createHelper = workbook.getCreationHelper();
			XSSFSheet sheet = workbook.createSheet("Report");
			// header
			Row row = sheet.createRow(0);
			Cell cell = row.createCell(0);
			cell.setCellValue("Username");
			cell = row.createCell(1);
			cell.setCellValue("First name");
			cell = row.createCell(2);
			cell.setCellValue("Last name");
			cell = row.createCell(3);
			cell.setCellValue("Disabled");
			cell = row.createCell(4);
			cell.setCellValue("EAV");
			int rowNum = 1;
			//
			// json is array of identities
			if (jParser.nextToken() == JsonToken.START_ARRAY) {
				// write single identity
				while (jParser.nextToken() == JsonToken.START_OBJECT) {
					HashMap<String, Object> myMap = new HashMap<>();
					myMap = getMapper().readValue(jParser, HashMap.class);
					if (rowNum == 1) {
						eavName = getEavName(myMap);
						cell.setCellValue(eavName);
					}
					//IdmIdentityDto identity = getMapper().readValue(jParser, IdmIdentityDto.class);
					row = sheet.createRow(rowNum++);
					cell = row.createCell(0);
					cell.setCellValue(myMap.get(IdentityEavReportExecutor.ATTRIBUTE_USERNAME).toString());
					cell = row.createCell(1);
					Object firstName = myMap.get(IdentityEavReportExecutor.ATTRIBUTE_FNAME);
					cell.setCellValue(firstName != null ? firstName.toString() : "");
					cell = row.createCell(2);
					Object lastName = myMap.get(IdentityEavReportExecutor.ATTRIBUTE_LNAME);
					cell.setCellValue(lastName != null ? lastName.toString() : "");
					cell = row.createCell(3);
					cell.setCellValue(myMap.get(IdentityEavReportExecutor.ATTRIBUTE_DISABLED).toString());
					cell = row.createCell(4);
					Object eav = myMap.get(eavName);
					cell.setCellValue(eav != null ? eav.toString() : "");
				}
			}
			// close json stream
			jParser.close();
			//
			// footer info about more available reports
			rowNum++;
			rowNum++;
			row = sheet.createRow(rowNum++);
			cell = row.createCell(0);
			cell.setCellValue("<<<<<<<<<<<<REPORT END>>>>>>>>>>>>");
			//
			// close and return input stream
			return getInputStream(workbook);
		} catch (IOException ex) {
			throw new ReportRenderException(report.getName(), ex);
		}
	}

	private String getEavName(HashMap<String, Object> map) {
		Set<String> keys = map.keySet();
		for (String key : keys) {
			if (!key.equals(IdentityEavReportExecutor.ATTRIBUTE_USERNAME) &&
					!key.equals(IdentityEavReportExecutor.ATTRIBUTE_LNAME) &&
					!key.equals(IdentityEavReportExecutor.ATTRIBUTE_FNAME) &&
					!key.equals(IdentityEavReportExecutor.ATTRIBUTE_DISABLED)) {
				return key;
			}
		}
		throw new IllegalArgumentException("There must be some EAV attribute");
	}

	@Override
	public String[] register(String reportName) {
		if (IdentityEavReportExecutor.REPORT_NAME.equals(reportName)) {
			return new String[]{getName()};
		}
		return new String[]{};
	}
}
