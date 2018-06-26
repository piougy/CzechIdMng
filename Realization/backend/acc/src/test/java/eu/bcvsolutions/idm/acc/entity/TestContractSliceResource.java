package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Entity for test contract slice table resource
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = TestContractSliceResource.TABLE_NAME)
public class TestContractSliceResource {

	public static final String TABLE_NAME = "test_contract_slice_resource";

	@Id
	@Column(name = "id", length = DefaultFieldLengths.NAME)
	private String id;
	@Column(name = "name", length = DefaultFieldLengths.NAME)
	private String name;
	@Column(name = "state", length = DefaultFieldLengths.NAME)
	private String state;
	@Column(name = "disabled", length = DefaultFieldLengths.NAME)
	private String disabled;
	@Column(name = "description", length = DefaultFieldLengths.NAME)
	private String description;
	@Column(name = "validfrom")
	private LocalDate validFrom;
	@Column(name = "validtill")
	private LocalDate validTill;
	@Column(name = "leader", length = DefaultFieldLengths.NAME)
	private String leader;
	@Column(name = "main", length = DefaultFieldLengths.NAME)
	private String main;
	@Column(name = "owner", length = DefaultFieldLengths.NAME)
	private String owner;
	@Column(name = "workposition", length = DefaultFieldLengths.NAME)
	private String workposition;
	@Column(name = "modified")
	private LocalDateTime modified;
	@Column(name = "validfrom_slice")
	private LocalDate validFromSlice;
	@Column(name = "contract_code", length = DefaultFieldLengths.NAME)
	private String contractCode;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDisabled() {
		return disabled;
	}

	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}

	public String getMain() {
		return main;
	}

	public void setMain(String main) {
		this.main = main;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public LocalDateTime getModified() {
		return modified;
	}

	public void setModified(LocalDateTime modified) {
		this.modified = modified;
	}

	public String getWorkposition() {
		return workposition;
	}

	public void setWorkposition(String workposition) {
		this.workposition = workposition;
	}

	public LocalDate getValidFromSlice() {
		return validFromSlice;
	}

	public void setValidFromSlice(LocalDate validFromSlice) {
		this.validFromSlice = validFromSlice;
	}

	public String getContractCode() {
		return contractCode;
	}

	public void setContractCode(String contractCode) {
		this.contractCode = contractCode;
	}
}
