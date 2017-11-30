package eu.bcvsolutions.idm.core.ecm.entity;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Attachment metadata
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 */
@Entity
@Table(name = "idm_attachment", indexes = { 
		@Index(name = "idx_idm_attachment_o_id", columnList = "owner_id"),
		@Index(name = "idx_idm_attachment_o_type", columnList = "owner_type"),
		@Index(name = "idx_idm_attachment_name", columnList = "name"),
		@Index(name = "idx_idm_attachment_desc", columnList = "description")
})
public class IdmAttachment extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "owner_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String ownerType;
	
	@Column(name = "owner_id", length = 16)
	private UUID ownerId;
	
	@NotNull
	@Basic(optional = false)
	@Column(name = "content_id", nullable = false)
	private UUID contentId; // identifier on FS
	
	@Size(max = 512)
	@Column(name = "content_path", length = 512)
	private String contentPath; // path on FS in attachment storage
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Basic(optional = false)
	@Column(name = "name", nullable = false, length = DefaultFieldLengths.NAME)
	private String name;
	
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	@NotNull
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Basic(optional = false)
	@Column(name = "mimetype", nullable = false, length = DefaultFieldLengths.NAME)
	private String mimetype;
	
	@NotNull
	@Size(min = 1, max = 100)
	@Basic(optional = false)
	@Column(name = "encoding", nullable = false, length = 100)
	private String encoding;
	
	@NotNull
	@Basic(optional = false)
	@Column(name = "filesize", nullable = false, precision = 18, scale = 0)
	@Max(999999999999999999l)
	private Long filesize;
	
	@Size(max = 50)
	@Column(name = "attachment_type", length = 50)
	private String attachmentType;
	
	@Size(max = 50)
	@Column(name = "owner_state", length = 50)
	private String ownerState;
	
	@NotNull
	@Basic(optional = false)
	@Column(name = "version_number", nullable = false, precision = 4, scale = 0)
	@Max(9999)
	@Min(1)
	private Integer versionNumber;
	
	@NotNull
	@Size(min = 1, max = 10)
	@Basic(optional = false)
	@Column(name = "version_label", nullable = false, length = 10)
	private String versionLabel;
	
	@ManyToOne
	@JoinColumn(name = "parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmAttachment parent;
	
	@ManyToOne
	@JoinColumn(name = "next_version_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmAttachment nextVersion;

	public IdmAttachment() {
	}

	public IdmAttachment(UUID id) {
		super(id);
	}
	
	public UUID getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}
	
	public String getOwnerType() {
		return ownerType;
	}
	
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	
	public UUID getContentId() {
		return contentId;
	}
	
	public void setContentId(UUID contentId) {
		this.contentId = contentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Long getFilesize() {
		return filesize;
	}

	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}

	public String getContentPath() {
		return contentPath;
	}

	public void setContentPath(String contentPath) {
		this.contentPath = contentPath;
	}

	/**
	 * Owner key
	 *
	 * @return
	 */
	public String getOwnerKey() {
		return getOwnerType() + ":" + getOwnerId();
	}

	public String getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}

	public String getOwnerState() {
		return ownerState;
	}
	
	public void setOwnerState(String ownerState) {
		this.ownerState = ownerState;
	}

	public Integer getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(Integer versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getVersionLabel() {
		return versionLabel;
	}

	public void setVersionLabel(String versionLabel) {
		this.versionLabel = versionLabel;
	}

	public IdmAttachment getParent() {
		return parent;
	}

	public void setParent(IdmAttachment parent) {
		this.parent = parent;
	}

	public IdmAttachment getNextVersion() {
		return nextVersion;
	}

	public void setNextVersion(IdmAttachment nextVersion) {
		this.nextVersion = nextVersion;
	}
}