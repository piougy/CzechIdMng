import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { DelegationDefinitionManager } from '../../redux';

const manager = new DelegationDefinitionManager();

/**
* Table for definition of delegations.
*
* @author Vít Švanda
*/
export class DelegationDefinitionTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      showLoading: true
    };
  }

  getContentKey() {
    return 'content.delegation-definitions';
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

  showDetail(entity) {
    const {delegator} = this.props;

    if (entity.id === undefined) {
      if (delegator) {
        this.context.history.push(`delegation-definitions/${ uuid.v1() }/detail?new=1&delegatorId=${delegator.id}`);
      } else {
        this.context.history.push(`delegation-definitions/${ uuid.v1() }/detail?new=1`);
      }
    } else if (delegator) {
      this.context.history.push(`delegation-definitions/${ entity.id }/detail?delegatorId=${delegator.id}`);
    } else {
      this.context.history.push(`delegation-definitions/${ entity.id }/detail`);
    }
  }

  /**
  * Method get last string of split string by dot.
  * Used for get niceLabel for type entity.
  */
  _getType(name) {
    return Utils.Ui.getSimpleJavaType(name);
  }

  _renderEntityColumn({ rowIndex, data, property }) {
    const value = data[rowIndex];
    if (!value) {
      return null;
    }
    return (
      <Advanced.EntityInfo
        entityType="IdmIdentityDto"
        entityIdentifier={ value[property]}
        face="popover"
        entity={ value._embedded[property] }
        showEntityType
        showLink
        showIcon/>
    );
  }

  _renderTypeColumn({ rowIndex, data, property }) {
    const value = data[rowIndex];
    if (!value || !value[property] || !value._embedded || !value._embedded.delegationType) {
      return null;
    }
    const type = value._embedded.delegationType;

    return this.i18n(`${type.module}:content.delegation-definitions.types.${type.id}.label`, type.id);
  }

  render() {
    const { filterOpened } = this.state;
    const { forceSearchParameters, columns, readOnly } = this.props;
    //
    return (
      <Basic.Div>
        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ this.getManager() }
          showRowSelection
          forceSearchParameters={forceSearchParameters}
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.IdentitySelect
                      label={ null }
                      placeholder={ this.i18n('filter.delegator.placeholder') }
                      ref="delegatorId"/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.IdentitySelect
                      label={ null }
                      placeholder={ this.i18n('filter.delegate.placeholder') }
                      ref="delegateId"/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={this.showDetail.bind(this, { })}
                rendered={ !readOnly && this.getManager().canSave() }
                icon="fa:plus">
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }
          filterOpened={ filterOpened }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column
            property="delegator"
            rendered={ _.includes(columns, 'delegator') }
            cell={this._renderEntityColumn.bind(this)}/>
          <Advanced.Column
            property="delegate"
            rendered={ _.includes(columns, 'delegate') }
            cell={this._renderEntityColumn.bind(this)}/>
          <Advanced.Column
            property="validFrom"
            rendered={ _.includes(columns, 'validFrom') }
            face="date"
            sort/>
          <Advanced.Column
            property="validTill"
            rendered={ _.includes(columns, 'validTill') }
            face="date"
            sort/>
          <Advanced.Column
            property="type"
            rendered={ _.includes(columns, 'type') }
            face="text"
            cell={this._renderTypeColumn.bind(this)}
            sort/>
          <Advanced.Column
            header={ this.i18n('entity.created') }
            property="created"
            rendered={ _.includes(columns, 'created') }
            face="datetime"
            sort/>
          <Advanced.Column
            property="description"
            rendered={ _.includes(columns, 'description') }
            face="text"
            sort/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

DelegationDefinitionTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool
};

DelegationDefinitionTable.defaultProps = {
  filterOpened: true,
  columns: ['delegator', 'delegate', 'type', 'validFrom', 'validTill', 'description', 'created'],
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select, null, null, { forwardRef: true })(DelegationDefinitionTable);
