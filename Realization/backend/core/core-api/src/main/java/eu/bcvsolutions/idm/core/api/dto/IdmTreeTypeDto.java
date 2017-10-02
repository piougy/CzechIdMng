package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Type of tree structure
 *
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "treeTypes")
public class IdmTreeTypeDto extends AbstractDto implements Codeable  {

    private static final long serialVersionUID = 3883227192651419232L;
    @NotEmpty
    @Size(min = 1, max = DefaultFieldLengths.NAME)
    private String code;
    @NotEmpty
    @Size(min = 1, max = DefaultFieldLengths.NAME)
    private String name;
    
    public IdmTreeTypeDto() {
	}
    
    public IdmTreeTypeDto(UUID id) {
    	super(id);
   	}

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
