package eu.bcvsolutions.idm.rpt.report.general;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.renderer.AbstractXlsxRenderer;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;

@Component("formableEntityXlsxRenderer")
@Description(AbstractXlsxRenderer.RENDERER_EXTENSION) // will be show as format for download
public class FormableEntityXlsxRenderer extends AbstractXlsxRenderer implements RendererRegistrar {

	public static final String NAME = "formableEntityXlsxRenderer";

	@Override
	public InputStream render(RptReportDto report) {
		try (JsonParser jParser = getMapper().getFactory().createParser(getReportData(report));
			 XSSFWorkbook workbook = new XSSFWorkbook()) {
			XSSFSheet sheet = workbook.createSheet("Report");
			//
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setWrapText(true);

			int rowNum = 0;
			//
			if (jParser.nextToken() == JsonToken.START_ARRAY && jParser.nextToken() == JsonToken.START_OBJECT) {
				Map<String, Object> firstObject = getMapper().readValue(jParser, Map.class);
				final List<String> header = Collections.unmodifiableList(Lists.newArrayList(firstObject.keySet()));
				rowNum = writeHeader(header, rowNum, sheet);
				rowNum = createRow(firstObject, rowNum, sheet, header);

				// write single entity
				while (jParser.nextToken() == JsonToken.START_OBJECT) {
					Map<String, Object> item = getMapper().readValue(jParser, Map.class);
					//
					rowNum = createRow(item, rowNum, sheet, header);
				}
			}
			// close and return input stream
			return getInputStream(workbook);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private int writeHeader(List<String> header, int rowNum, XSSFSheet sheet) {
		Row row = sheet.createRow(rowNum++);

		for (int i = 0; i < header.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(header.get(i));
		}

		return rowNum;
	}

	private int createRow(Map<String, Object> item, int rowNum, XSSFSheet sheet, List<String> header) {
		Row row = sheet.createRow(rowNum++);

		item.forEach((key, val) -> {
			Cell cell = row.createCell(header.indexOf(key));
			cell.setCellValue(val == null ? "" : val.toString());
		});

		return rowNum;
	}

	@Override
	public String[] register(String reportName) {
		if (FromableEntityReportExecutor.NAME.equals(reportName)) {
			return new String[]{getName()};
		}
		return new String[0];
	}

	@Override
	public String getName() {
		return NAME;
	}
}
