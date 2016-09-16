package eu.bcvsolutions.idm.core.model.domain;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.rest.webmvc.IncomingRequest;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Idea - spring data rest
 * 
 * @author Radek Tomiška
 *
 */
public class PersistentEntityResolver {

	private static final String ERROR_MESSAGE = "Could not read an object of type %s from the request!";
	private static final String NO_CONVERTER_FOUND = "No suitable HttpMessageConverter found to read request body into object of type %s from request with content type of %s!";

	private final DomainObjectReader reader;
	private final List<HttpMessageConverter<?>> messageConverters;

	public PersistentEntityResolver(List<HttpMessageConverter<?>> messageConverters, DomainObjectReader reader) {

		Assert.notEmpty(messageConverters, "MessageConverters must not be null or empty!");
		Assert.notNull(reader, "DomainObjectReader must not be null!");

		this.messageConverters = messageConverters;
		this.reader = reader;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object resolveEntity(HttpServletRequest nativeRequest, Class<?> domainType, Object objectToUpdate)
			throws Exception {
		ServletServerHttpRequest request = new ServletServerHttpRequest(nativeRequest);
		IncomingRequest incoming = new IncomingRequest(request);
		MediaType contentType = request.getHeaders().getContentType();
		for (HttpMessageConverter converter : messageConverters) {
			if (!converter.canRead(PersistentEntityResource.class, contentType)) {
				continue;
			}
			Object obj = read(domainType, incoming, converter, objectToUpdate);

			if (obj == null) {
				throw new HttpMessageNotReadableException(String.format(ERROR_MESSAGE, domainType));
			}
			return obj;
		}

		throw new HttpMessageNotReadableException(String.format(NO_CONVERTER_FOUND, domainType, contentType));
	}

	/**
	 * Reads the given {@link ServerHttpRequest} into an object of the type of
	 * the given {@link RootResourceInformation}, potentially applying the
	 * content to an object of the given id.
	 * 
	 * @param information
	 *            must not be {@literal null}.
	 * @param request
	 *            must not be {@literal null}.
	 * @param converter
	 *            must not be {@literal null}.
	 * @param id
	 *            must not be {@literal null}.
	 * @return
	 */
	private Object read(Class<?> domainType, IncomingRequest request,
			HttpMessageConverter<Object> converter, Object objectToUpdate) {

		// JSON + PATCH request
		if (request.isPatchRequest() && converter instanceof MappingJackson2HttpMessageConverter) {

			if (objectToUpdate == null) {
				throw new ResourceNotFoundException();
			}

			ObjectMapper mapper = ((MappingJackson2HttpMessageConverter) converter).getObjectMapper();
			Object result = readPatch(request, mapper, objectToUpdate);

			return result;

			// JSON + PUT request
		} else if (converter instanceof MappingJackson2HttpMessageConverter) {

			ObjectMapper mapper = ((MappingJackson2HttpMessageConverter) converter).getObjectMapper();

			return objectToUpdate == null ? read(request, converter, domainType)
					: readPutForUpdate(request, mapper, objectToUpdate);
		}

		// Catch all
		return read(request, converter, domainType);
	}

	private Object readPatch(IncomingRequest request, ObjectMapper mapper, Object existingObject) {

		try {

			JsonPatchHandler handler = new JsonPatchHandler(mapper, reader);
			return handler.apply(request, existingObject);

		} catch (Exception o_O) {

			if (o_O instanceof HttpMessageNotReadableException) {
				throw (HttpMessageNotReadableException) o_O;
			}

			throw new HttpMessageNotReadableException(String.format(ERROR_MESSAGE, existingObject.getClass()), o_O);
		}
	}

	private Object readPutForUpdate(IncomingRequest request, ObjectMapper mapper, Object existingObject) {

		try {

			JsonPatchHandler handler = new JsonPatchHandler(mapper, reader);
			JsonNode jsonNode = mapper.readTree(request.getBody());

			return handler.applyPut((ObjectNode) jsonNode, existingObject);

		} catch (Exception o_O) {
			throw new HttpMessageNotReadableException(String.format(ERROR_MESSAGE, existingObject.getClass()), o_O);
		}
	}

	private Object read(IncomingRequest request, HttpMessageConverter<Object> converter, Class<?> domainType) {

		try {
			return converter.read(domainType, request.getServerHttpRequest());
		} catch (IOException o_O) {
			throw new HttpMessageNotReadableException(String.format(ERROR_MESSAGE, domainType), o_O);
		}
	}
}
