package eu.bcvsolutions.idm.icf.api;

import java.util.Date;

public interface IcfEnabledAttribute {

	Boolean getEnabled();

	Date getEnabledDate();

	Date getDisabledDate();

}