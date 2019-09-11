package eu.bcvsolutions.idm.core.filterBuilders;

import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultFilterManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


/**
 * Test filtering filter builders
 *
 * @author Kolychev Artem
 */
public class FilterBuildersFilterUnitTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext context;
    private FilterManager filterManager;
    private MultiValueMap<String, Object> parameters;
    private List<FilterBuilderDto> filterBuilderDtos;//All filter builders.
    private FilterBuilderDto filterBuilderDto;


    @Before
    public void init() {
        filterManager = context.getAutowireCapableBeanFactory().createBean(DefaultFilterManager.class);
        FilterBuilderFilter filterBuilderFilter = new FilterBuilderFilter(new LinkedMultiValueMap<>());
        filterBuilderDtos = filterManager.find(filterBuilderFilter);
        Assert.assertNotEquals(0, filterBuilderDtos.size());
        filterBuilderDto = filterBuilderDtos.get(0);
        parameters = new LinkedMultiValueMap<>();
    }

    @Test
    public void testEmptyFilter() {
        parameters.add(FilterBuilderFilter.PARAMETER_NAME, "noName");
        FilterBuilderFilter filterBuilderFilter = new FilterBuilderFilter(parameters);
        List<FilterBuilderDto> filterBuilderDtos = filterManager.find(filterBuilderFilter);
        Assert.assertEquals(0, filterBuilderDtos.size());
    }

    @Test
    public void testFilterName() {
        parameters.add(FilterBuilderFilter.PARAMETER_NAME, filterBuilderDto.getName());
        FilterBuilderFilter filterBuilderFilter = new FilterBuilderFilter(parameters);
        List<FilterBuilderDto> filterBuilderDtos = filterManager.find(filterBuilderFilter);
        Assert.assertEquals(1, filterBuilderDtos.size());
    }

    @Test
    public void testFilterModule() {
        parameters.add(FilterBuilderFilter.PARAMETER_MODULE, filterBuilderDto.getModule());
        FilterBuilderFilter filterBuilderFilter = new FilterBuilderFilter(parameters);
        List<FilterBuilderDto> filterBuilderDtos = filterManager.find(filterBuilderFilter);
        Assert.assertTrue(1 <= filterBuilderDtos.size());
    }

    @Test
    public void testFilterEntityType() {
        parameters.add(FilterBuilderFilter.PARAMETER_ENTITY_TYPE, filterBuilderDto.getEntityType());
        FilterBuilderFilter filterBuilderFilter = new FilterBuilderFilter(parameters);
        List<FilterBuilderDto> filterBuilderDtos = filterManager.find(filterBuilderFilter);
        Assert.assertTrue(1 <= filterBuilderDtos.size());
    }

    @Test
    public void testFilterDescription() {
        FilterBuilderDto filterBuilderDto = null;
        for (FilterBuilderDto _filterBuilderDto : filterBuilderDtos) {
            if (_filterBuilderDto.getDescription() != null) {
                filterBuilderDto = _filterBuilderDto;
                break;
            }
        }
        if (filterBuilderDto == null) {
            return;
        }
        parameters.add(FilterBuilderFilter.PARAMETER_DESCRIPTION,
                filterBuilderDto.getDescription().substring(1, filterBuilderDto.getDescription().length() - 1)
        );
        FilterBuilderFilter filterBuilderFilter = new FilterBuilderFilter(parameters);
        List<FilterBuilderDto> filterBuilderDtos = filterManager.find(filterBuilderFilter);
        Assert.assertTrue(1 <= filterBuilderDtos.size());
    }
}
