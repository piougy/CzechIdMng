package eu.bcvsolutions.idm.core.exception;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

/**
 * Model wrapper for error response
 * - simply adds errors element to response
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
public class RestErrors {
	
	private final List<ErrorModel> errors = Lists.newArrayList();

	public RestErrors() {
	}

	public RestErrors(ErrorModel error) {
		this.errors.add(error);
	}
	
	public RestErrors(List<ErrorModel> errors) {
		this.errors.addAll(errors);
	}

	// TODO: can be removed after FE error handler will be refactored
	// @JsonIgnore
	public ErrorModel getError() {
		if(this.errors.isEmpty()) {
			return null;
		}
		return errors.get(0);
	}

	public void addError(ErrorModel error) {
		this.errors.add(error);
	}
	
	public List<ErrorModel> getErrors() {
		return Collections.unmodifiableList(errors);
	}
}