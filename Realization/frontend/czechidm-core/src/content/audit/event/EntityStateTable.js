import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { EntityStateManager } from '../../../redux';
import SearchParameters from '../../../domain/SearchParameters';
import OperationStateEnum from '../../../enums/OperationStateEnum';

const manager = new EntityStateManager();

/**
 * Table of persisted entity states
 *
 * @author Radek Tomiška
 */
export class EntityStateTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.entityStates';
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return this.props.uiKey;
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  reload() {
    this.refs.table.getWrappedInstance().reload();
  }

  render() {
    const {
      columns,
      forceSearchParameters,
      rendered,
      showFilter,
      showToolbar,
      className,
      showRowSelection
    } = this.props;
    const { filterOpened, detail } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    const _forceSearchParameters = forceSearchParameters || new SearchParameters();
    //
    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ this.getManager() }
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className={ _.includes(columns, 'ownerType') ? '' : 'last' }>
                  <Basic.Col lg={ 8 }>
                    <Advanced.Filter.FilterDate
                      mode="datetime"
                      fromProperty="createdFrom"
                      tillProperty="createdTill"
                      ref="createdTill"/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.EnumSelectBox
                      ref="states"
                      placeholder={ this.i18n('filter.states.placeholder') }
                      enum={ OperationStateEnum }
                      multiSelect
                      useSymbol={ false }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="resultCode"
                      placeholder={ this.i18n('filter.resultCode.placeholder') }/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row rendered={ _.includes(columns, 'ownerType') }>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('filter.text.placeholder') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="ownerType"
                      placeholder={ this.i18n('filter.ownerType.placeholder') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="ownerId"
                      placeholder={ this.i18n('filter.ownerId.placeholder') }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={ filterOpened }
          showFilter={ showFilter }
          forceSearchParameters={_forceSearchParameters}
          _searchParameters={ this.getSearchParameters() }
          showToolbar={ showToolbar }
          className={ className }
          showRowSelection={ showRowSelection }>
          <Advanced.Column
            property=""
            header=""
            rendered={ false }
            className="detail-button"
            cell={
              ({rowIndex, data}) => (
                <Advanced.DetailButton
                  title={this.i18n('button.detail')}
                  onClick={() => this.showDetail(data[rowIndex])}/>
              )
            }/>
          <Advanced.Column
            property="result"
            width={75}
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.OperationResult value={ entity.result } />
                );
              }
            }
            rendered={_.includes(columns, 'result')}/>
          <Advanced.Column
            property="resultCode"
            width={75}
            cell={
              ({rowIndex, data}) => (
                data[rowIndex].result.code
              )
            }
            sort
            sortProperty="result.code"
            rendered={_.includes(columns, 'resultCode')}/>
          <Advanced.Column property="created" sort face="datetime" rendered={ _.includes(columns, 'created') } width={ 175 }/>
          <Advanced.Column
            property="ownerType"
            rendered={_.includes(columns, 'ownerType')}
            width={ 200 }
            cell={
              ({rowIndex, data, property}) => (
                <span title={data[rowIndex][property]}>
                  { Utils.Ui.getSimpleJavaType(data[rowIndex][property]) }
                </span>
              )}
          />
          <Advanced.Column
            property="ownerId"
            header={ this.i18n('entity.EntityEvent.owner.label') }
            rendered={_.includes(columns, 'ownerId')}
            cell={
              ({ rowIndex, data, property }) => {
                //
                if (!data[rowIndex]._embedded || !data[rowIndex]._embedded[property]) {
                  return (
                    <Advanced.UuidInfo value={ data[rowIndex][property] } />
                  );
                }
                //
                return (
                  <Advanced.EntityInfo
                    entityType={ Utils.Ui.getSimpleJavaType(data[rowIndex].ownerType) }
                    entityIdentifier={ data[rowIndex][property] }
                    entity={ data[rowIndex]._embedded[property] }
                    face="popover"
                    showEntityType={ false }
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            property="processorName"
            rendered={_.includes(columns, 'processor')}
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (!entity.processorName) {
                  return null;
                }
                return `${entity.processorModule}:${entity.processorName}`;
              }
            }/>
          <Advanced.Column
            property="processedOrder"
            rendered={_.includes(columns, 'processedOrder')}
            width={ 150 }
            sort />
          <Advanced.Column
            property="instanceId"
            rendered={_.includes(columns, 'instanceId')}
            width={ 100 } />
          <Advanced.Column
            width={ 125 }
            property="event"
            rendered={_.includes(columns, 'event')}
            cell={
              ({rowIndex, data, property}) => (
                <Advanced.UuidInfo value={ data[rowIndex][property] }/>
              )
            }/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={ detail.show }
          onHide={ this.closeDetail.bind(this) }
          backdrop="static">
          <Basic.Modal.Header closeButton text={ this.i18n('state.detail.header') }/>
          <Basic.Modal.Body>
            {
              !detail.entity
              ||
              <div>
                <Advanced.OperationResult value={ detail.entity.result } face="full"/>
              </div>
            }
          </Basic.Modal.Body>

          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={ this.closeDetail.bind(this) }>
              { this.i18n('button.close') }
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </div>
    );
  }
}

EntityStateTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Rendered
   */
  rendered: PropTypes.bool
};

EntityStateTable.defaultProps = {
  columns: ['result', 'resultCode', 'created', 'ownerType', 'ownerId', 'processor', 'processedOrder', 'instanceId', 'event'],
  filterOpened: false,
  forceSearchParameters: null,
  rendered: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select, null, null, { withRef: true })(EntityStateTable);
