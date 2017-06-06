import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Utils from '../../../utils';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { AuditManager } from '../../../redux';
import AuditModificationEnum from '../../../enums/AuditModificationEnum';

const auditManager = new AuditManager();

/**
* Table of Audit for identities
*/
export class AuditIdentityTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: true
    };
  }

  componentDidMount() {
    this.context.store.dispatch(auditManager.fetchEntities(auditManager.getAuditedEntitiesNames(), null, (entities) => {
      if (entities !== null) {
        const auditedEntities = entities._embedded.resources.map(item => { return {value: item, niceLabel: item }; });
        this.setState({
          auditedEntities,
          showLoading: false
        });
      } else {
        this.setState({
          showLoading: false
        });
      }
    }));
  }

  getContentKey() {
    return 'content.audit';
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
    if (this.refs.table !== undefined) {
      this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
    }
  }

  /**
  * Method get last string of split string by dot.
  * Used for get niceLabel for type entity.
  */
  _getType(name) {
    const type = name.split('.');
    return type[type.length - 1];
  }

  _getAdvancedFilter(auditedEntities, showLoading) {
    return (
      <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
        <Basic.AbstractForm ref="filterForm" showLoading={showLoading}>
          <Basic.Row>
            <div className="col-lg-4">
              <Advanced.Filter.DateTimePicker
                mode="date"
                ref="from"
                placeholder={this.i18n('filter.dateFrom.placeholder')}/>
            </div>
            <div className="col-lg-4">
              <Advanced.Filter.DateTimePicker
                mode="date"
                ref="till"
                placeholder={this.i18n('filter.dateTill.placeholder')}/>
            </div>
            <div className="col-lg-4 text-right">
              <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
            </div>
          </Basic.Row>
          <Basic.Row>
            <div className="col-lg-4">
              <Advanced.Filter.TextField
                className="pull-right"
                ref="username"
                placeholder={this.i18n('content.audit.identities.username')}
                returnProperty="username"/>
            </div>
            <div className="col-lg-4">
              <Advanced.Filter.TextField
                className="pull-right"
                ref="modifier"
                placeholder={this.i18n('content.audit.identities.modifier')}
                returnProperty="modifier"/>
            </div>
            <div className="col-lg-4">
              <Advanced.Filter.TextField
                ref="id"
                placeholder={this.i18n('content.audit.identities.identityId')}/>
            </div>
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
    this.context.router.push(`/audit/entities/${entityId}/diff/`);
  }

  _getForceSearchParameters() {
    return auditManager.getDefaultSearchParameters().setName('entity').setFilter('entity', 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity');
  }

  render() {
    const { tableUiKey } = this.props;
    const { showLoading, auditedEntities } = this.state;
    return (
      <div>
        <Advanced.Table
          ref="table"
          filterOpened
          uiKey={tableUiKey}
          manager={auditManager}
          forceSearchParameters={this._getForceSearchParameters()}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          showId
          filter={
            !auditedEntities
            ||
            this._getAdvancedFilter(auditedEntities, showLoading)
          }>
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
            width={ 200 }
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <span title={data[rowIndex][property]}>
                    { this._getType(data[rowIndex][property]) }
                  </span>
                );
              }}
              />
          <Advanced.Column
            property="entityId"
            header={ this.i18n('entity.Audit.entity') }
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                return (
                  <Advanced.EntityInfo
                    entityType={ this._getType(data[rowIndex].type) }
                    entityIdentifier={ data[rowIndex][property] }
                    face="popover"
                    showEntityType={ false }/>
                );
              }
            }/>
          <Advanced.Column
            property="modification"
            width={ 100 }
            sort
            cell={
              ({ rowIndex, data, property }) => {
                return <Basic.Label level={AuditModificationEnum.getLevel(data[rowIndex][property])} text={AuditModificationEnum.getNiceLabel(data[rowIndex][property])}/>;
              }}
              />
          <Advanced.Column property="modifier" sort face="text"/>
          <Advanced.Column property="timestamp" header={this.i18n('entity.Audit.revisionDate')} sort face="datetime"/>
        </Advanced.Table>
      </div>
    );
  }
}

AuditIdentityTable.propTypes = {
  tableUiKey: PropTypes.string
};

AuditIdentityTable.defaultProps = {
  tableUiKey: 'audit-table-identities'
};

function select() {
  return {
  };
}

export default connect(select)(AuditIdentityTable);
