package eu.bcvsolutions.idm.acc.dto;

import org.joda.time.DateTime;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * DTO for {@link SysProvisioningBatch}
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "provisioningBatchs")
public class SysProvisioningBatchDto extends AbstractDto {

	private static final long serialVersionUID = -6935661873072888426L;
	
	private DateTime nextAttempt;

	public DateTime getNextAttempt() {
		return nextAttempt;
	}

	public void setNextAttempt(DateTime nextAttempt) {
		this.nextAttempt = nextAttempt;
	}

	
}
