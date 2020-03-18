package eu.bcvsolutions.idm.core.eav.processor.pojo;

import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;

public class EavValue {

	private String name;
	private IdmFormValueDto oldValue;
	private IdmFormValueDto newValue;

	public EavValue(String name , IdmFormValueDto oldValue, IdmFormValueDto newValue){

		this.name = name;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IdmFormValueDto getOldValue() {
		return oldValue;
	}

	public void setOldValue(IdmFormValueDto oldValue) {
		this.oldValue = oldValue;
	}

	public IdmFormValueDto getNewValue() {
		return newValue;
	}

	public void setNewValue(IdmFormValueDto newValue) {
		this.newValue = newValue;
	}
}
