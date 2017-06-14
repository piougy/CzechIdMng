package eu.bcvsolutions.idm.core.config.web;

import java.util.Arrays;

import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Core module swagger configuration
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Bean
	public Docket coreApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.groupName("core-v1")
				.select()
				.apis(RequestHandlerSelectors.basePackage("eu.bcvsolutions.idm.core.rest"))
				.paths(PathSelectors.ant(BaseController.BASE_PATH + "/**"))
				.build()
				.pathMapping("/")
				.apiInfo(metaData("core"))
				/*.globalResponseMessage(
						RequestMethod.GET,
						Lists.newArrayList(new ResponseMessageBuilder()
				                .code(401)
				                .message("Unauthorized")
				                .responseModel(new ModelRef("Error"))
				                .build()))*/
				.securitySchemes(Arrays.asList((new BasicAuth("basic"))))
				.ignoredParameterTypes(Pageable.class, MultiValueMap.class);
	}
	
	@Bean
	public Docket notificationApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.groupName("notification-v1")
				.select()
				.apis(RequestHandlerSelectors.basePackage("eu.bcvsolutions.idm.core.notification.rest.impl"))
				.paths(PathSelectors.ant(BaseController.BASE_PATH + "/**"))
				.build()
				.apiInfo(metaData("notification"))
				.securitySchemes(Arrays.asList((new BasicAuth("basic"))))
				.ignoredParameterTypes(Pageable.class, MultiValueMap.class);
	}
	
	private ApiInfo metaData(String module) {
        ApiInfo apiInfo = new ApiInfo(
                "CzechIdM REST API",
                "CzechIdM REST API for "  + module + " module",
                "7.3",
                "Terms of service",
                new Contact("BCV solutions s.r.o.", "http://www.bcvsolutions.eu", null),
               "MIT",
                "https://github.com/bcvsolutions/CzechIdMng/blob/develop/LICENSE",
                Lists.newArrayList());
        return apiInfo;
    }
	
	// eav, security, workflow
}