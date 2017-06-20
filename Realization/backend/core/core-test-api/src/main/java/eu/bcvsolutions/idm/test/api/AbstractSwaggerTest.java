package eu.bcvsolutions.idm.test.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;

/**
 * Static swagger generation for given module - generated output will be used as input for swagger2Markup build.
 * 
 * @author Radek Tomiška
 *
 */
@Ignore
public abstract class AbstractSwaggerTest extends AbstractRestTest {
	
	/**
	 * Swagger output directory will contain swagger input files for next asciidoc processing.
	 * Properties can be overriden in profile configuration.
	 */
	@Value("${springfox.documentation.swagger.v2.path:/api/doc}")
	private String path;
	@Value("${springfox.documentation.swagger.outputDir:target/swagger}")
	private String outputDir;
	@Value("${springfox.documentation.swagger.outputFilename:swagger.json}")
	private String filename;
	
	/**
	 * Converts module's swagger endpoint to json
	 * 
	 * @see ModuleDescriptor#getId()
	 * @param moduleId
	 * @throws Exception
	 */
    public void convertSwagger(String moduleId) throws Exception {    	
        MvcResult mvcResult = getMockMvc().perform(get(String.format("%s?group=%s", path, moduleId))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String swaggerJson = response.getContentAsString();
        Files.createDirectories(Paths.get(outputDir));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputDir, filename), StandardCharsets.UTF_8)){
            writer.write(swaggerJson);
        }
    }

}
