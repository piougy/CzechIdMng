package eu.bcvsolutions.idm.acc.domain;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;

/**
 * IdmAttachmentWithDataDto is equivalent to IdmAttachmenDto only adds array of bytes.
 *
 * @author Vít Švanda
 */
@Relation(collectionRelation = "attachments")
public class IdmAttachmentWithDataDto extends IdmAttachmentDto {

	private static final long serialVersionUID = 1L;
	
	public IdmAttachmentWithDataDto() {
		super();
	}
	
	public IdmAttachmentWithDataDto(Auditable auditable) {
		super(auditable);
	}
	
	private byte[] data;

	public byte[] getData() {
		if (data != null) {
			return data.clone();
		}
		return null;
	}

	public void setData(byte[] data) {
		if (data != null) {
			this.data = data.clone();
		} else {
			this.data = null;
		}
	}
}