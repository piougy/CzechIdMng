import React from 'react';
import Helmet from 'react-helmet';
import Immutable from 'immutable';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { DataManager, FilterBuilderManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import * as Utils from '../../utils';

const UIKEY = 'filter-builders';
const manager = new FilterBuilderManager();

/**
 * Registered filter builders.
 *
 * @author Kolychev Artem
 * @author Radek Tomiška
 * @since 9.7.7
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
    this.fetchFilterBuilders(
      SearchParameters.getSearchParameters(
        SearchParameters.getFilterData(this.refs.filterForm),
        this.props._searchParameters
      )
    );
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

  onEnable(entity, enable, event) {
    if (event) {
      event.preventDefault();
    }
    const name = `${ entity.name } - ${ Utils.Ui.getSimpleJavaType(entity.filterBuilderClass) }`;
    this.refs[`confirm-${enable ? '' : 'de'}activate`].show(
      this.i18n(
        `action.${enable ? '' : 'de'}activate.message`,
        { count: 1, record: name }
      ),
      this.i18n(`action.${enable ? '' : 'de'}activate.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(this.getManager().setEnabled(entity.id, (patchedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n(`action.${enable ? '' : 'de'}activate.success`, { count: 1, record: name }) });
          // refresh table with processors
          this.reload();
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // rejected
    });
  }

  render() {
    const { filterBuilders, showLoading, registeredFilterBuilders, _searchParameters } = this.props;
    const { filterOpened } = this.state;
    let _filterBuilders = new Immutable.OrderedMap();
    let _entityClasses = new Immutable.OrderedSet();
    let _registeredFilterClasses = new Immutable.Map();
    let _registeredEntityClasses = new Immutable.OrderedSet();
    if (filterBuilders) {
      filterBuilders.forEach(filterBuilder => {
        if (!_filterBuilders.has(filterBuilder.entityClass)) {
          _filterBuilders = _filterBuilders.set(filterBuilder.entityClass, []);
        }
        const builder = _filterBuilders.get(filterBuilder.entityClass);
        builder.push(filterBuilder);
        _filterBuilders = _filterBuilders.set(filterBuilder.entityClass, builder);
        _entityClasses = _entityClasses.add(filterBuilder.entityClass);
      });
    }
    if (registeredFilterBuilders) {
      registeredFilterBuilders.forEach(filterBuilder => {
        _registeredEntityClasses = _registeredEntityClasses.add(filterBuilder.entityClass);
        _registeredFilterClasses = _registeredFilterClasses.set(filterBuilder.entityClass, filterBuilder.filterClass);
      });
      // sort _entityTypes
      _registeredEntityClasses = _registeredEntityClasses.sort((one, two) => one > two);
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
                searchParameters={ _searchParameters }/>
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
                    <Basic.Col lg={ 8 }>
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={ this.i18n('filter.text.placeholder') }/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 } className="text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                    </Basic.Col>
                  </Basic.Row>
                  <Basic.Row className="last">
                    <Basic.Col lg={ 4 }>
                      <Advanced.Filter.TextField
                        ref="module"
                        placeholder={this.i18n('filter.module.placeholder')}/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 }>
                      <Advanced.Filter.EnumSelectBox
                        ref="entityClass"
                        placeholder={ this.i18n('filter.entityClass.placeholder') }
                        options={ _registeredEntityClasses.toArray().map(value => ({ value, niceLabel: Utils.Ui.getSimpleJavaType(value) })) }
                        searchable/>
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            </div>
          </Basic.Collapse>
        </Basic.Toolbar>
        <Basic.Loading isStatic show={showLoading}/>
        <Basic.Alert
          level="info"
          text={ this.i18n('component.basic.Table.noData') }
          style={{ margin: 15 }}
          rendered={ !showLoading && _filterBuilders.size === 0 } />
        {
          showLoading || _filterBuilders.size === 0
          ||
          <div>
            {
              _entityClasses.map((entityClass) => (
                <div className="tab-pane-table-body" style={{ marginBottom: 15 }}>
                  <Basic.ContentHeader>
                    { Utils.Ui.getSimpleJavaType(entityClass) }
                    <small
                      style={{ marginLeft: 7, fontSize: '0.8em' }}
                      title={ this.i18n('entity.FilterBuilder.filterClass.title') }>
                      { `(${ Utils.Ui.getSimpleJavaType(_registeredFilterClasses.get(entityClass)) })` }
                    </small>
                  </Basic.ContentHeader>
                  <Basic.Table
                    data={_filterBuilders.get(entityClass)}
                    showLoading={showLoading}
                    noData={ this.i18n('component.basic.Table.noData') }
                    rowClass={ ({rowIndex, data}) => Utils.Ui.getRowClass(data[rowIndex]) }>
                    <Basic.Column
                      property="module"
                      header={ this.i18n('entity.FilterBuilder.module.label') }
                      width={ 100 }/>
                    <Basic.Column
                      property="name"
                      width="20%"
                      header={
                        <span title={ this.i18n('entity.FilterBuilder.name.title') }>
                          { this.i18n('entity.FilterBuilder.name.label') }
                        </span>
                      }/>
                    <Basic.Column
                      property="description"
                      header={ this.i18n('entity.FilterBuilder.description.label') }/>
                    <Basic.Column
                      property="filterBuilderClass"
                      width="20%"
                      header={ this.i18n('entity.FilterBuilder.filterBuilderClass.label') }
                      cell={
                        ({rowIndex, data, property}) => {
                          const filterBuilderClass = data[rowIndex][property];
                          //
                          return (
                            <span title={ filterBuilderClass }>{ Utils.Ui.getSimpleJavaType(filterBuilderClass) }</span>
                          );
                        }
                      }/>
                    <Basic.Column
                      header={ this.i18n('entity.id.label') }
                      property="id"
                      rendered={ this.isDevelopment() }
                      className="text-center"
                      width={ 100 }
                      cell={
                        ({rowIndex, data, property}) => (
                          <Advanced.UuidInfo value={ data[rowIndex][property] }/>
                        )
                      }/>
                    <Basic.Column
                      header={this.i18n('label.action')}
                      className="action"
                      cell={
                        ({rowIndex, data}) => {
                          if (!data[rowIndex].disabled) {
                            // filter can be activated only
                            return null;
                          }
                          return (
                            <Basic.Button
                              level="success"
                              onClick={ this.onEnable.bind(this, data[rowIndex], true) }
                              className="btn-xs"
                              title={this.i18n('button.activate')}
                              titlePlacement="bottom">
                              {this.i18n('button.activate')}
                            </Basic.Button>
                          );
                        }
                      }/>
                  </Basic.Table>
                </div>
              ))
            }
            <Basic.Pagination total={ filterBuilders.length } />
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
    _searchParameters: Utils.Ui.getSearchParameters(state, UIKEY)
  };
}

export default connect(select)(FilterBuilders);
