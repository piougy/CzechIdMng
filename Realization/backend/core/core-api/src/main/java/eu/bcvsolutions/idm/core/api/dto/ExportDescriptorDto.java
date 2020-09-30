package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Export descriptor
 * 
 * @author Vít Švanda
 *
 */
public class ExportDescriptorDto implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Defines whether authoritative mode is enabled for this object type. If so,
	 * then the import removes other / redundant entities from the target IdM (for
	 * example, if the role contains additional guarantors that do not exist in the
	 * batch, they will be deleted).Parent fields are uses for find ID of super
	 * parents for this batch (system, role, ..).
	 */
	private boolean supportsAuthoritativeMode = false;
	/**
	 * Contains class of exported DTO.
	 */
	private Class<? extends BaseDto> dtoClass;

	/**
	 * Defines name of parent fields. This contains UUID of entity for which was DTO
	 * export. This is important for authoritative import, because we need to delete
	 * others entity from target IdM.
	 * 
	 * Parent fields are uses for find ID of super parents for this batch (system,
	 * role, ..).
	 */
	private LinkedHashSet<String> parentFields = new LinkedHashSet<String>();

	/**
	 * Defines name of filter for find all DTOs existing under same super parent on
	 * target system. Super parent DTO typically system or role. This field is
	 * mandatory for authoritative mode.
	 * 
	 * This property is uses for find specific setter in Filter POJO. It means
	 * setter must exists in filter!
	 */
	private String superParentFilterProperty;

	/**
	 * Defines fields in DTO, where we need to use advanced paring strategy. It
	 * means that we need to check if UUID exists in target system. If not, we will
	 * use DTO from embedded map and try to find DTO by code.
	 */
	private Set<String> advancedParingFields = Sets.newHashSet();
	/**
	 * Defines fields in DTO, which will be excluded during the import. It means this
	 * fields will be not changed on target IdM. If entity will not exists,then that
	 * fields will set to null.
	 *
	 * For example, the token in sync definition is excluded.
	 */
	private Set<String> excludedFields = Sets.newHashSet();

	/**
	 * If is true and will cannot be persisted (some relation not was not found),
	 * then only warning will be logged, but batch can continue.
	 */
	private boolean optional = false;

	public ExportDescriptorDto() {
		super();
	}

	public ExportDescriptorDto(Class<? extends BaseDto> dtoClass) {
		super();
		this.dtoClass = dtoClass;
	}

	public ExportDescriptorDto(Class<? extends BaseDto> dtoClass, String superParentFilterProperty) {
		super();
		this.dtoClass = dtoClass;
		this.superParentFilterProperty = superParentFilterProperty;
	}

	public Class<? extends BaseDto> getDtoClass() {
		return dtoClass;
	}

	public void setDtoClass(Class<? extends BaseDto> dtoClass) {
		this.dtoClass = dtoClass;
	}

	public LinkedHashSet<String> getParentFields() {
		return parentFields;
	}

	public void setParentFields(LinkedHashSet<String> parentFields) {
		this.parentFields = parentFields;
	}

	public String getSuperParentFilterProperty() {
		return superParentFilterProperty;
	}

	public void setSuperParentFilterProperty(String superParentFilterProperty) {
		this.superParentFilterProperty = superParentFilterProperty;
	}

	public Set<String> getAdvancedParingFields() {
		return advancedParingFields;
	}

	public void setAdvancedParingFields(Set<String> advancedParingFields) {
		this.advancedParingFields = advancedParingFields;
	}

	public Set<String> getExcludedFields() {
		return excludedFields;
	}

	public void setExcludedFields(Set<String> excludedFields) {
		this.excludedFields = excludedFields;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optionalDto) {
		this.optional = optionalDto;
	}

	public boolean isSupportsAuthoritativeMode() {
		return supportsAuthoritativeMode;
	}

	public void setSupportsAuthoritativeMode(boolean supportsAuthoritativeMode) {
		this.supportsAuthoritativeMode = supportsAuthoritativeMode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dtoClass);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ExportDescriptorDto)) {
			return false;
		}
		ExportDescriptorDto other = (ExportDescriptorDto) obj;
		return Objects.equals(dtoClass, other.dtoClass);
	}

}