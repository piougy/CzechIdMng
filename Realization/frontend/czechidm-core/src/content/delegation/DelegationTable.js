import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { DelegationManager } from '../../redux';
import OperationStateEnum from '../../enums/OperationStateEnum';

const manager = new DelegationManager();

/**
* Table for delegations.
*
* @author Vít Švanda
*/
export class DelegationTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      showLoading: true
    };
  }

  componentDidMount() {
    super.componentDidMount();
  }

  getContentKey() {
    return 'content.delegation-definitions.delegations';
  }

  getManager() {
    return manager;
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  /**
  * Method get last string of split string by dot.
  * Used for get niceLabel for type entity.
  */
  _getType(name) {
    return Utils.Ui.getSimpleJavaType(name);
  }

  _renderOwnerColumn({rowIndex, data}) {
    const value = data[rowIndex];
    if (!value) {
      return null;
    }
    return (
      <Advanced.EntityInfo
        entityType={ this._getType(value.ownerType) }
        entityIdentifier={ value.ownerId}
        face="popover"
        entity={ value.ownerDto }
        showEntityType
        showLink
        showIcon/>
    );
  }

  _renderDefinitionColumn({rowIndex, data}) {
    const value = data[rowIndex];
    if (!value) {
      return null;
    }
    return (
      <Advanced.EntityInfo
        entityType="DelegationDefinitionDto"
        entityIdentifier={ value.definition}
        face="popover"
        entity={ value._embedded ? value._embedded.definition : null}
        showEntityType
        showLink
        showIcon/>
    );
  }

  render() {
    const { filterOpened } = this.state;
    const { forceSearchParameters, columns } = this.props;
    //
    return (
      <Basic.Div>
        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ this.getManager() }
          showRowSelection={false}
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 8 }>
                    <Advanced.Filter.EnumSelectBox
                      ref="operationState"
                      placeholder={this.i18n('entity.Delegation.ownerState.label')}
                      enum={OperationStateEnum}/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={ filterOpened }
          forceSearchParameters={forceSearchParameters}
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            property="definition"
            header={ this.i18n('entity.Delegation.definition.label') }
            rendered={ _.includes(columns, 'definition') }
            cell={this._renderDefinitionColumn.bind(this)}/>
          <Advanced.Column
            property="ownerId"
            header={ this.i18n('entity.Delegation.ownerId.label') }
            rendered={ _.includes(columns, 'ownerId') }
            cell={this._renderOwnerColumn.bind(this)}/>
          <Advanced.Column
            property="ownerState"
            header={ this.i18n('entity.Delegation.ownerState.label') }
            sort
            sortProperty="ownerState.state"
            cell={
              ({ data, rowIndex }) => {
                return <Advanced.OperationResult value={ data[rowIndex].ownerState }/>;
              }
            }/>
          <Advanced.Column
            header={ this.i18n('entity.created') }
            property="created"
            rendered={ _.includes(columns, 'created') }
            face="datetime"
            sort/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

DelegationTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool
};

DelegationTable.defaultProps = {
  columns: ['definition', 'ownerId', 'created'],
  filterOpened: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select, null, null, { forwardRef: true })(DelegationTable);
