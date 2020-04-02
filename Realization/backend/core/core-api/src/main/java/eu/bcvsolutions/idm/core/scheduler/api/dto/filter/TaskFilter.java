package eu.bcvsolutions.idm.core.scheduler.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;

/**
 * Scheduler task filter.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
public class TaskFilter extends DataFilter {

    public TaskFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public TaskFilter(MultiValueMap<String, Object> data) {
        this(data, null);
    }

    public TaskFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(Task.class , data, parameterConverter);
    }
}
