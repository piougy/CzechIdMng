import React from 'react';
import Helmet from 'react-helmet';
import Immutable from 'immutable';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { AvailableServiceManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import * as Utils from '../../utils';

const UIKEY = 'available-services';
const manager = new AvailableServiceManager();

/**
 * Available services
 *
 * @author Ondrej Husnik
 */
class AvailableServices extends Advanced.AbstractTableContent {

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
    this.context.store.dispatch(manager.fetchAvailableServices());
  }

  reload() {
    const {_searchParameters} = this.props;
    //
    this.fetchAvailableServices(_searchParameters);
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.fetchAvailableServices(
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
    this.fetchAvailableServices(null);
    this.refs.filterForm.setData({});
  }

  fetchAvailableServices(searchParameters) {
    this.context.store.dispatch(this.getManager().fetchEntities(searchParameters, UIKEY));
  }


  render() {
    const { availableServices, showLoading, _searchParameters } = this.props;
    const { filterOpened } = this.state;

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
                onClick={this.fetchAvailableServices.bind(this, _searchParameters)}
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
          rendered={ !showLoading && availableServices.length === 0 } />
          {
            showLoading || availableServices.length === 0
            ||
            <div>
              {
                [...availableServices.map((service) => (
                  <div className="tab-pane-table-body" style={{ marginBottom: 15 }}>
                    <Basic.ContentHeader title={service.packageName}>
                      {service.id}
                      <small style={{ marginLeft: 7, fontSize: '0.8em' }}>
                        {' (' + service.module + ', ' +  service.serviceName + ')'}
                      </small>
                    </Basic.ContentHeader>
                    <Basic.Table
                      data={service.methods}
                      showLoading={showLoading}
                      noData={ this.i18n('component.basic.Table.noData') }
                      rowClass={ ({rowIndex, data}) => Utils.Ui.getRowClass(data[rowIndex]) }>
                      <Basic.Column
                        property="methodName"
                        header={ this.i18n('entity.AvailableServices.methodName.label') }
                        width="25%"/>
                      <Basic.Column
                        property="returnType"
                        width="33%"
                        header={ this.i18n('entity.AvailableServices.returnType.label') }
                        cell={
                          ({rowIndex, data, property}) => {
                            return (
                              <div title={data[rowIndex][property]}>
                                {Utils.Ui.getSimpleJavaType(data[rowIndex][property])}
                              </div>
                            );
                          }
                        }
                      />
                      <Basic.Column
                        property="arguments"
                        header={ this.i18n('entity.AvailableServices.arguments.label') }
                        cell={
                          ({rowIndex, data, property}) => {
                            if (data[rowIndex][property].length === 1) {
                              return (
                                <div title={data[rowIndex][property][0]}>
                                  {Utils.Ui.getSimpleJavaType(data[rowIndex][property][0])}
                                </div>
                              );
                            }
                            const arr = [];
                            data[rowIndex][property].forEach((arg) => { arr.push(<li title={arg}>{Utils.Ui.getSimpleJavaType(arg)}</li>); });
                            return (
                              <div style={{marginLeft: 15}}>
                                <ol>
                                  {arr}
                                </ol>
                              </div>
                            );
                          }
                        }
                      />
                    </Basic.Table>
                  </div>
                )).values()]
              }
              <Basic.Pagination total={ availableServices.length } />
            </div>
          }
      </div>
    );
  }
}

AvailableServices.propTypes = {
  userContext: PropTypes.object,
  availableServices: PropTypes.arrayOf(PropTypes.object),
  showLoading: PropTypes.bool
};

AvailableServices.defaultProps = {
  userContext: null,
  showLoading: true
};

function select(state) {
  return {
    availableServices: manager.getEntities(state, UIKEY),
    showLoading: Utils.Ui.isShowLoading(state, UIKEY)
            || Utils.Ui.isShowLoading(state, AvailableServiceManager.UI_KEY_AVAILABLE_SERVICES),
    _searchParameters: Utils.Ui.getSearchParameters(state, UIKEY)
  };
}

export default connect(select)(AvailableServices);
