import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import Immutable from 'immutable';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { EntityEventProcessorManager, DataManager } from '../../redux';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';

const UIKEY = 'entity-event-processors';
const manager = new EntityEventProcessorManager();

/**
 * BE event precessors
 *
 * @author Radek Tomiška
 */
class EntityEventProcessors extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true
    };
  }

  getContentKey() {
    return 'content.system.entity-event-processors';
  }

  getNavigationKey() {
    return 'entity-event-processors';
  }

  getManager() {
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.reload();
    this.refs.text.focus();
  }

  reload() {
    const { _searchParameters } = this.props;
    //
    this.fetchEntities(_searchParameters);
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.fetchEntities(SearchParameters.getSearchParameters(SearchParameters.getFilterData(this.refs.filterForm), this.props._searchParameters));
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.fetchEntities(null);
    this.refs.filterForm.setData({});
  }

  fetchEntities(searchParameters) {
    this.context.store.dispatch(this.getManager().fetchEntities(searchParameters, UIKEY));
  }

  render() {
    const { registeredProcessors, showLoading, _searchParameters } = this.props;
    const { filterOpened } = this.state;
    //
    let _registeredProcessors = new Immutable.OrderedMap();
    let _entityTypes = [];
    if (registeredProcessors) {
      registeredProcessors.forEach(processor => {
        if (!_registeredProcessors.has(processor.entityType)) {
          _registeredProcessors = _registeredProcessors.set(processor.entityType, []);
        }
        const entityProcessors = _registeredProcessors.get(processor.entityType);
        entityProcessors.push(processor);
        _registeredProcessors = _registeredProcessors.set(processor.entityType, entityProcessors);
      });
      //
      _entityTypes = _registeredProcessors.keySeq().toArray();
      _entityTypes.sort((one, two) => {
        return one > two;
      });
    }

    console.log('types: ' + _entityTypes);

    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-deactivate" level="warning"/>
        <Basic.Confirm ref="confirm-activate" level="success"/>

        <Basic.Toolbar>
          <div>
            <div className="pull-right">
              <Advanced.Filter.ToogleButton
                filterOpen={ (open)=> this.setState({ filterOpened: open }) }
                filterOpened={ filterOpened }
                style={{ marginLeft: 3 }}
                searchParameters={ _searchParameters }/>
              <Advanced.RefreshButton
                onClick={ this.fetchEntities.bind(this, _searchParameters) }
                title={ this.i18n('button.refresh') }
                showLoading={ showLoading }/>
            </div>
            <div className="clearfix"></div>
          </div>
          <Basic.Collapse in={filterOpened}>
            <div>
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row>
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={this.i18n('filter.text.placeholder')}/>
                    </div>
                    <div className="col-lg-6 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                  <Basic.Row>
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="name"
                        placeholder={this.i18n('filter.name.placeholder')}/>
                    </div>
                  </Basic.Row>
                  <Basic.Row className="last">
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="description"
                        placeholder={this.i18n('filter.description.placeholder')}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            </div>
          </Basic.Collapse>
        </Basic.Toolbar>

        <Basic.Loading isStatic show={ showLoading }/>
        <Basic.Alert level="info" text={ this.i18n('component.basic.Table.noData') } style={{ margin: 15 }} rendered={ !showLoading && _registeredProcessors.size === 0 } />

        {
          showLoading || _registeredProcessors.size === 0
          ||
          <div>
            {
              _entityTypes.map((entityType) => {
                const processors = _registeredProcessors.get(entityType);
                processors.sort((one, two) => {
                  return one.order > two.order;
                });
                return (
                  <div className="tab-pane-table-body" style={{ marginBottom: 15 }}>
                    <Basic.ContentHeader text={entityType}/>

                    <Basic.Table
                      data={processors}
                      showLoading={showLoading}
                      noData={this.i18n('component.basic.Table.noData')}
                      rowClass={({ rowIndex, data }) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>
                      <Basic.Column property="module" header={this.i18n('entity.EntityEventProcessor.module')} width={75} />
                      <Basic.Column property="name" header={this.i18n('entity.EntityEventProcessor.name')} width="30%"/>
                      <Basic.Column
                        property="description"
                        header={this.i18n('entity.EntityEventProcessor.description')}
                        cell={
                          ({ rowIndex, data, property }) => {
                            const values = [];
                            _.keys(data[rowIndex].configurationProperties).map(configurationProperty => {
                              const value = data[rowIndex].configurationProperties[configurationProperty];
                              if (value) {
                                values.push({ configurationProperty, value });
                              }
                            });
                            return (
                              <div>
                                <div>{data[rowIndex][property]}</div>
                                {
                                  values.length === 0
                                  ||
                                  <div>
                                    <div>Configuration:</div>
                                    {
                                      values.map(value => {
                                        return (<div>{ `- ${value.configurationProperty}: ${value.value}` }</div>);
                                      })
                                    }
                                  </div>
                                }
                              </div>
                            );
                          }
                        }/>
                      <Basic.Column
                        property="eventTypes"
                        header={this.i18n('entity.EntityEventProcessor.eventTypes')}
                        width={125}
                        cell={
                          ({ rowIndex, data, property }) => {
                            if (!data[rowIndex][property]) {
                              return null;
                            }
                            return data[rowIndex][property].join(', ');
                          }
                        }/>
                      <Basic.Column property="order" header={this.i18n('entity.EntityEventProcessor.order')} width={100}/>
                      <Basic.Column
                        property="disabled"
                        header={<Basic.Cell className="column-face-bool">{this.i18n('entity.EntityEventProcessor.disabled')}</Basic.Cell>}
                        cell={<Basic.BooleanCell className="column-face-bool"/>}
                        width="100px"/>
                    </Basic.Table>
                  </div>
                );
              })
            }
            <Basic.Pagination total={ registeredProcessors.length } />
          </div>
        }
      </div>
    );
  }
}

EntityEventProcessors.propTypes = {
  userContext: PropTypes.object,
  registeredProcessors: PropTypes.object,
  showLoading: PropTypes.bool
};
EntityEventProcessors.defaultProps = {
  userContext: null,
  showLoading: true,
  registeredProcessors: null
};

function select(state) {
  return {
    userContext: state.security.userContext,
    // registeredProcessors: DataManager.getData(state, EntityEventProcessorManager.UI_KEY_PROCESSORS),
    registeredProcessors: manager.getEntities(state, UIKEY),
    showLoading: Utils.Ui.isShowLoading(state, UIKEY),
    _searchParameters: Utils.Ui.getSearchParameters(state, UIKEY)
  };
}

export default connect(select)(EntityEventProcessors);
