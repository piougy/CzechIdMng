import React from 'react';
import Helmet from 'react-helmet';
import Immutable from 'immutable';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import {DataManager, FilterBuilderManager} from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import * as Utils from '../../utils';
import UiUtils from "../../utils/UiUtils";

const UIKEY = 'filter-builders';
const manager = new FilterBuilderManager();

/**
 * Filter builders filter
 *
 * @author Kolychev Artem
 */
class FilterBuilders extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true
    };
  }

  getContentKey() {
    return `content.system.${UIKEY}`;
  }

  getNavigationKey() {
    return UIKEY;
  }

  getManager() {
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.reload();
    this.context.store.dispatch(manager.fetchRegisteredFilterBuilders());
  }

  reload() {
    const {_searchParameters} = this.props;
    //
    this.fetchFilterBuilders(_searchParameters);
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.fetchFilterBuilders(SearchParameters.getSearchParameters(SearchParameters.getFilterData(this.refs.filterForm), this.props._searchParameters));
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.fetchFilterBuilders(null);
    this.refs.filterForm.setData({});
  }

  fetchFilterBuilders(searchParameters) {
    this.context.store.dispatch(this.getManager().fetchEntities(searchParameters, UIKEY));
  }

  render() {
    const {filterBuilders, showLoading, registeredFilterBuilders, _searchParameters} = this.props;
    const {filterOpened} = this.state;
    let _filterBuilders = new Immutable.OrderedMap();
    let _entityClasses = new Immutable.OrderedSet();
    let allEntityClasses = new Immutable.OrderedSet();
    if (filterBuilders) {
      filterBuilders.forEach(filterBuilder => {
        if (!_filterBuilders.has(filterBuilder.entityType)) {
          _filterBuilders = _filterBuilders.set(filterBuilder.entityType, []);
        }
        const builder = _filterBuilders.get(filterBuilder.entityType);
        builder.push(filterBuilder);
        _filterBuilders = _filterBuilders.set(filterBuilder.entityType, builder);
        _entityClasses = _entityClasses.add(filterBuilder.entityType);
      });
    }
    if (registeredFilterBuilders) {
      registeredFilterBuilders.forEach(filterBuilder => {
        const value = UiUtils.getSimpleJavaType(filterBuilder.filterBuilderClass);
        allEntityClasses = allEntityClasses.add({value, niceLabel: value});
      });
    }
    return (
      <div>
        <Helmet title={this.i18n('title')}/>
        <Basic.Confirm ref="confirm-deactivate" level="warning"/>
        <Basic.Confirm ref="confirm-activate" level="success"/>

        <Basic.Toolbar>
          <div>
            <div className="pull-right">
              <Advanced.Filter.ToogleButton
                filterOpen={(open) => this.setState({filterOpened: open})}
                filterOpened={filterOpened}
                style={{marginLeft: 3}}
                searchParameters={_searchParameters}/>
              <Advanced.RefreshButton
                onClick={this.fetchFilterBuilders.bind(this, _searchParameters)}
                title={this.i18n('button.refresh')}
                showLoading={showLoading}/>
            </div>
            <div className="clearfix"/>
          </div>
          <Basic.Collapse in={filterOpened}>
            <div>
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row>
                    <Basic.Col lg={4}>
                      <Advanced.Filter.TextField
                        ref="name"
                        placeholder={this.i18n('filter.name.placeholder')}/>
                    </Basic.Col>
                    <Basic.Col lg={4}>
                      <Advanced.Filter.TextField
                        ref="module"
                        placeholder={this.i18n('filter.module.placeholder')}/>
                    </Basic.Col>
                    <Basic.Col lg={4} className="text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </Basic.Col>
                  </Basic.Row>
                  <Basic.Row className="last">
                    <Basic.Col lg={4}>
                      <Advanced.Filter.TextField
                        ref="description"
                        placeholder={this.i18n('filter.description.placeholder')}/>
                    </Basic.Col>

                    <Basic.Col lg={4}>
                      <Advanced.Filter.EnumSelectBox
                        ref="filterBuilderClass"
                        placeholder={this.i18n('filter.eventTypes.placeholder')}
                        options={[...new Set(allEntityClasses.map(x => x.value))]}
                        searchable/>
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            </div>
          </Basic.Collapse>
        </Basic.Toolbar>
        <Basic.Loading isStatic show={showLoading}/>
        {
          showLoading || _filterBuilders.size === 0
                    ||
                    <div>
                      {
                        _entityClasses.map((entityType) => (
                          <div className="tab-pane-table-body" style={{marginBottom: 15}} key={entityType}>
                            <Basic.ContentHeader text={entityType}/>
                            <Basic.Table
                              data={_filterBuilders.get(entityType)}
                              showLoading={showLoading}
                              noData={this.i18n('component.basic.Table.noData')}
                              rowClass={({rowIndex, data}) => Utils.Ui.getRowClass(data[rowIndex])}>
                              <Basic.Column
                                property="id"
                                header={this.i18n('entity.FilterBuilder.id')}/>
                              <Basic.Column
                                property="module"
                                header={this.i18n('entity.FilterBuilder.module')}/>
                              <Basic.Column
                                property="name"
                                header={this.i18n('entity.FilterBuilder.name')}/>
                              <Basic.Column
                                property="description"
                                header={this.i18n('entity.FilterBuilder.description')}/>
                              <Basic.Column
                                property="filterBuilderClass"
                                header={this.i18n('entity.FilterBuilder.entityType')}/>
                            </Basic.Table>
                          </div>
                        ))
                      }
                    </div>
        }
      </div>
    );
  }
}


FilterBuilders.propTypes = {
  userContext: PropTypes.object,
  registeredFilterBuilders: PropTypes.object, // immutable
  filterBuilders: PropTypes.arrayOf(PropTypes.object),
  showLoading: PropTypes.bool
};

FilterBuilders.defaultProps = {
  userContext: null,
  showLoading: true
};

function select(state) {
  return {
    registeredFilterBuilders: DataManager.getData(state, FilterBuilderManager.UI_KEY_FILTER_BUILDERS),
    filterBuilders: manager.getEntities(state, UIKEY),
    showLoading: Utils.Ui.isShowLoading(state, UIKEY)
            || Utils.Ui.isShowLoading(state, FilterBuilderManager.UI_KEY_FILTER_BUILDERS),
  };
}

export default connect(select)(FilterBuilders);
