package eu.bcvsolutions.idm.core.model.dto;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.exception.ErrorModel;

/**
 * Model wrapper for errors and infos response
 * - simply adds errors or info element to response
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY)
@org.codehaus.jackson.map.annotate.JsonSerialize(include=org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_EMPTY)
public class ResultModels {
	
	private final List<ErrorModel> errors = Lists.newArrayList();
	private final List<ResultModel> infos = Lists.newArrayList();

	public ResultModels() {
	}

	public ResultModels(ErrorModel error) {
		this(error, null);
	}
	
	public ResultModels(ErrorModel error, ResultModel info) {
		if (error != null) {
			this.errors.add(error);
		}
		if (info != null) {
			this.infos.add(info);
		}
	}
	
	public ResultModels(List<ErrorModel> errors) {
		this.errors.addAll(errors);
	}
	
	public ResultModels(List<ErrorModel> errors, List<ResultModel> infos) {
		if (errors != null) {
			this.errors.addAll(errors);
		}
		if (infos != null) {
			this.infos.addAll(infos);
		}
	}

	public void addError(ErrorModel error) {
		this.errors.add(error);
	}
	
	public void addInfo(ResultModel info) {
		this.infos.add(info);
	}
	
	@com.fasterxml.jackson.annotation.JsonProperty("_errors")
	@org.codehaus.jackson.annotate.JsonProperty("_errors")
	public List<ResultModel> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	
	@com.fasterxml.jackson.annotation.JsonProperty("_infos")
	@org.codehaus.jackson.annotate.JsonProperty("_infos")
	public List<ResultModel> getInfos() {
		return Collections.unmodifiableList(infos);
	}
	
	@com.fasterxml.jackson.annotation.JsonIgnore
	@org.codehaus.jackson.annotate.JsonIgnore
	public ErrorModel getError() {
		if(this.errors.isEmpty()) {
			return null;
		}
		return errors.get(0);
	}
}