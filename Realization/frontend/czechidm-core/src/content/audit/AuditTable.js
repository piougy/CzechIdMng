import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { AuditManager, ConfigurationManager } from '../../redux';
import AuditModificationEnum from '../../enums/AuditModificationEnum';

const auditManager = new AuditManager();

/**
* Table of Audit for entities
*
* @author Ondřej Kopr
* @author Radek Tomiška
*/
export class AuditTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      transactionId: this._getTransactionId(props._searchParameters),
      entityId: this._getEntityId(props._searchParameters),
      entityType: props._searchParameters && props._searchParameters.getFilters().has('type')
        ? props._searchParameters.getFilters().get('type')
        : null
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(auditManager.fetchAuditedEntitiesNames());
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    //  filters from redux
    if (nextProps._searchParameters) {
      const newTransactionId = this._getTransactionId(nextProps._searchParameters);
      const newEntityId = this._getEntityId(nextProps._searchParameters);
      if ((newTransactionId && this.state.transactionId !== newTransactionId)
          || (newEntityId && this.state.entityId !== newEntityId)) {
        this.setState({
          transactionId: newTransactionId,
          entityId: newEntityId
        }, () => {
          //
          const filterData = {};
          nextProps._searchParameters.getFilters().forEach((v, k) => {
            filterData[k] = v;
          });
          this.refs.filterForm.setData(filterData);
          this.refs.table.useFilterData(filterData);
        });
      }
    }
  }

  getContentKey() {
    return 'content.audit';
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
    if (this.refs.table !== undefined) {
      this.refs.table.cancelFilter(this.refs.filterForm);
    }
  }

  onEntityTypeChange(entityType) {
    this.setState({
      entityType: entityType ? entityType.value : null
    });
  }

  /**
  * Method get last string of split string by dot.
  * Used for get niceLabel for type entity.
  */
  _getType(name) {
    return Utils.Ui.getSimpleJavaType(name);
  }

  _getTransactionId(searchParameters) {
    if (!searchParameters || !searchParameters.getFilters().has('transactionId')) {
      return null;
    }
    return searchParameters.getFilters().get('transactionId');
  }

  _getEntityId(searchParameters) {
    if (!searchParameters || !searchParameters.getFilters().has('entityId')) {
      return null;
    }
    return searchParameters.getFilters().get('entityId');
  }

  /**
  * Method get last string of arrays split string by dot.
  * Used method _getType
  */
  _getTypeArray(arrayOfName) {
    for (const index in arrayOfName) {
      if (arrayOfName.hasOwnProperty(index)) {
        arrayOfName[index] = this._getType(arrayOfName[index]);
      }
    }
    return _.join(arrayOfName, ', ');
  }

  _getAdvancedFilter(auditedEntities, columns) {
    const { showTransactionId, forceSearchParameters } = this.props;
    const { entityType } = this.state;
    let _showTransactionId = showTransactionId;
    if (forceSearchParameters && forceSearchParameters.getFilters().has('transactionId')) {
      _showTransactionId = false;
    }
    //
    return (
      <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
        <Basic.AbstractForm ref="filterForm">
          <Basic.Row>
            <Basic.Col lg={ 8 } rendered={ _.includes(columns, 'revisionDate') }>
              <Advanced.Filter.FilterDate ref="fromTill"/>
            </Basic.Col>
            <Basic.Col lg={ 4 } className="text-right">
              <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
            </Basic.Col>
          </Basic.Row>
          <Basic.Row>
            <Basic.Col lg={ 4 } rendered={ _.includes(columns, 'type') }>
              <Advanced.Filter.EnumSelectBox
                ref="type"
                searchable
                placeholder={this.i18n('entity.Audit.type')}
                options={ auditedEntities }
                onChange={ this.onEntityTypeChange.bind(this) }/>
            </Basic.Col>
            <Basic.Col lg={ 4 } rendered={ _.includes(columns, 'modification') }>
              <Advanced.Filter.EnumSelectBox
                ref="modification"
                placeholder={ this.i18n('entity.Audit.modification') }
                enum={ AuditModificationEnum }/>
            </Basic.Col>
            <Basic.Col lg={ 4 } rendered={ _.includes(columns, 'modifier') }>
              <Advanced.Filter.TextField
                className="pull-right"
                ref="modifier"
                placeholder={ this.i18n('entity.Audit.modifier') }
                returnProperty="username"/>
            </Basic.Col>
          </Basic.Row>
          <Basic.Row className="last">
            <Basic.Col lg={ 4 } rendered={ _.includes(columns, 'entityId') }>
              <Advanced.Filter.TextField
                ref="entityId"
                placeholder={ entityType ? this.i18n('filter.entityId.codeable') : this.i18n('filter.entityId.placeholder') }
                help={ this.i18n('filter.entityId.help') }/>
            </Basic.Col>
            <Basic.Col lg={ !_showTransactionId ? 8 : 4 } rendered={ _.includes(columns, 'changedAttributes') }>
              <Advanced.Filter.CreatableSelectBox
                ref="changedAttributesList"
                placeholder={this.i18n('entity.Audit.changedAttributes.placeholder')}
                tooltip={this.i18n('entity.Audit.changedAttributes.tooltip')}/>
            </Basic.Col>
            <Basic.Col lg={ 4 } rendered={ _showTransactionId }>
              <Advanced.Filter.TextField
                ref="transactionId"
                placeholder={ this.i18n('filter.transactionId.placeholder') }/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }

  /**
   * Method for show detail of revision, redirect to detail
   *
   * @param entityId id of revision
   */
  showDetail(entityId) {
    this.context.history.push(`/audit/entities/${ entityId }/diff/`);
  }

  _getForceSearchParameters() {
    const { entityId, entityClass, forceSearchParameters } = this.props;

    if (entityId !== undefined || entityClass !== undefined) {
      if (forceSearchParameters) {
        return forceSearchParameters.setFilter('type', entityClass).setFilter('entityId', entityId);
      }
      return auditManager.getDefaultSearchParameters().setFilter('type', entityClass).setFilter('entityId', entityId);
    }
    return forceSearchParameters;
  }

  render() {
    const { columns, uiKey, auditedEntities, className } = this.props;
    return (
      <Basic.Div>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          filterOpened
          manager={ auditManager }
          forceSearchParameters={ this._getForceSearchParameters() }
          rowClass={ ({ rowIndex, data }) => { return Utils.Ui.getRowClass(data[rowIndex]); } }
          className={ className }
          showId
          filter={ this._getAdvancedFilter(auditedEntities, columns) }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex].id)}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column
            property="type"
            rendered={ _.includes(columns, 'type') }
            width={ 200 }
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <span title={data[rowIndex][property]}>
                    { this._getType(data[rowIndex][property]) }
                  </span>
                );
              }
            }/>
          <Advanced.Column
            property="entityId"
            header={ this.i18n('entity.Audit.entity') }
            rendered={ _.includes(columns, 'entityId') }
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                const value = data[rowIndex][property];
                //
                if (data[rowIndex]._embedded && data[rowIndex]._embedded[property]) {
                  return (
                    <Advanced.EntityInfo
                      entityType={ this._getType(data[rowIndex].type) }
                      entityIdentifier={ value }
                      entity={ data[rowIndex]._embedded[property] }
                      face="popover"
                      showEntityType={ false }
                      showIcon/>
                  );
                }
                if (data[rowIndex].revisionValues) {
                  return (
                    <Advanced.EntityInfo
                      entityType={ this._getType(data[rowIndex].type) }
                      entityIdentifier={ value }
                      entity={ data[rowIndex].revisionValues }
                      face="popover"
                      showLink={ false }
                      showEntityType={ false }
                      showIcon
                      deleted/>
                  );
                }
                //
                return (
                  <Advanced.UuidInfo value={ value } />
                );
              }
            }/>
          <Advanced.Column
            property="modification"
            width={ 100 }
            sort
            rendered={_.includes(columns, 'modification')}
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <Basic.Label
                    level={ AuditModificationEnum.getLevel(data[rowIndex][property]) }
                    text={ AuditModificationEnum.getNiceLabel(data[rowIndex][property]) }/>
                );
              }
            }/>
          <Advanced.Column property="modifier" sort face="text" rendered={_.includes(columns, 'modifier')}/>
          <Advanced.Column
            property="timestamp"
            header={ this.i18n('entity.Audit.revisionDate') }
            sort
            face="datetime"
            rendered={ _.includes(columns, 'revisionDate') }/>
          <Advanced.Column
            hidden
            property="changedAttributes"
            rendered={_.includes(columns, 'changedAttributes')}
            cell={
              ({ rowIndex, data, property }) => {
                return _.replace(data[rowIndex][property], ',', ', ');
              }
            }
          />
          <Advanced.Column
            hidden
            property="modifiedEntityNames"
            sort
            rendered={_.includes(columns, 'modifiedEntityNames')}
            cell={
              ({ rowIndex, data, property }) => {
                return this._getTypeArray(data[rowIndex][property]);
              }
            }
          />
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

AuditTable.propTypes = {
  // table uiKey
  uiKey: PropTypes.string.isRequired,
  // columns for display, check default props.
  columns: PropTypes.arrayOf(PropTypes.string),
  // simple name of entity class, if this paramater isn't defined show all revisions
  entityClass: PropTypes.string,
  // id of entity
  entityId: PropTypes.number,
  // flag for detail
  isDetail: PropTypes.bool
};

AuditTable.defaultProps = {
  columns: ['id', 'type', 'modification', 'modifier', 'revisionDate', 'entityId', 'changedAttributes'],
  isDetail: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    auditedEntities: auditManager.prepareOptionsFromAuditedEntitiesNames(auditManager.getAuditedEntitiesNames(state)),
    showTransactionId: ConfigurationManager.showTransactionId(state)
  };
}

export default connect(select)(AuditTable);
