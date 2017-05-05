package eu.bcvsolutions.idm.core.api.dto;

import java.util.Map;

/**
 * Default DTO for diff between two revision.
 * In this DTO will be transferred only ID
 * of revisions and values that is changed
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public class IdmAuditDiffDto {

    private Long idFirstRevision;

    private Long idSecondRevision;

    private Map<String, Object> diffValues;

    public Long getIdFirstRevision() {
        return idFirstRevision;
    }

    public void setIdFirstRevision(Long idFirstRevision) {
        this.idFirstRevision = idFirstRevision;
    }

    public Long getIdSecondRevision() {
        return idSecondRevision;
    }

    public void setIdSecondRevision(Long idSecondRevision) {
        this.idSecondRevision = idSecondRevision;
    }

    public Map<String, Object> getDiffValues() {
        return diffValues;
    }

    public void setDiffValues(Map<String, Object> diffValues) {
        this.diffValues = diffValues;
    }
}
