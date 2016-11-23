import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { AuditManager, IdentityManager } from '../../redux';
import AuditModificationEnum from '../../enums/AuditModificationEnum';

const auditManager = new AuditManager();

const identityManager = new IdentityManager();

/**
* Table of Audit for entities
*/

export class AuditTable extends Basic.AbstractContent {

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

  componentWillUnmount() {
    this.cancelFilter();
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

  _getAdvancedFilter(auditedEntities, showLoading, columns) {
    return (
      <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
        <Basic.AbstractForm ref="filterForm" className="form-horizontal" showLoading={showLoading}>
          <Basic.Row>
            {
              !_.includes(columns, 'revisionDate')
              ||
              <div className="col-lg-4">
                <Advanced.Filter.DateTimePicker
                  mode="date"
                  ref="from"
                  placeholder={this.i18n('filter.dateFrom.placeholder')}
                  label={this.i18n('filter.dateFrom.label')}/>
              </div>
            }
            {
              !_.includes(columns, 'revisionDate')
              ||
              <div className="col-lg-4">
                <Advanced.Filter.DateTimePicker
                  mode="date"
                  ref="to"
                  placeholder={this.i18n('filter.dateTill.placeholder')}
                  label={this.i18n('filter.dateTill.label')}/>
              </div>
            }
            <div className="col-lg-4 text-right">
              <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
            </div>
          </Basic.Row>
          <Basic.Row>
            {
              !_.includes(columns, 'type')
              ||
              <div className="col-lg-4">
                <Advanced.Filter.EnumSelectBox
                  ref="type"
                  placeholder={this.i18n('entity.Audit.type')}
                  label={this.i18n('entity.Audit.type')}
                  options={auditedEntities}/>
              </div>
            }
            {
              !_.includes(columns, 'modification')
              ||
              <div className="col-lg-4">
                <Advanced.Filter.EnumSelectBox
                  className="col-lg-4"
                  ref="modification"
                  placeholder={this.i18n('entity.Audit.modification')}
                  label={this.i18n('entity.Audit.modification')}
                  enum={AuditModificationEnum}/>
              </div>
            }
            {
              !_.includes(columns, 'modifier')
              ||
              <div className="col-lg-4">
                <Advanced.Filter.SelectBox
                  ref="modifier"
                  label={this.i18n('entity.Audit.modifier')}
                  placeholder={this.i18n('entity.Audit.modifier')}
                  multiSelect={false}
                  manager={identityManager}
                  returnProperty="username"/>
              </div>
            }
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }

  _getDefaultSearchParameters() {
    const { entityId, entityClass } = this.props;

    if (entityId !== undefined || entityClass !== undefined) {
      return auditManager.getDefaultSearchParameters().setFilter('type', entityClass).setFilter('entityId', entityId);
    }
    return auditManager.getDefaultSearchParameters();
  }

  render() {
    const { columns, tableUiKey, clickTarget } = this.props;
    const { showLoading, auditedEntities } = this.state;
    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={tableUiKey}
          manager={auditManager}
          defaultSearchParameters={this._getDefaultSearchParameters()}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filter={
            !auditedEntities
            ||
            this._getAdvancedFilter(auditedEntities, showLoading, columns)
          }>
          {
            !clickTarget
            ||
            <Advanced.Column
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={this.i18n('button.detail')}
                      onClick={clickTarget.bind(this, data[rowIndex].id, data[rowIndex].entityId)}/>
                  );
                }
              }
              sort={false}/>
          }
          <Advanced.Column property="id" sort face="text" rendered={_.includes(columns, 'id')}/>
          <Advanced.Column property="entityId" sort face="text" rendered={_.includes(columns, 'entityId')}/>
          <Advanced.Column
            property="type"
            rendered={_.includes(columns, 'type')}
            cell={
              ({ rowIndex, data, property }) => {
                return this._getType(data[rowIndex][property]);
              }}
          />
          <Advanced.Column
            property="modification" sort
            rendered={_.includes(columns, 'modification')}
            cell={
              ({ rowIndex, data, property }) => {
                return <Basic.Label level={AuditModificationEnum.getLevel(data[rowIndex][property])} text={AuditModificationEnum.getNiceLabel(data[rowIndex][property])}/>;
              }}
          />
          <Advanced.Column property="modifier" sort face="text" rendered={_.includes(columns, 'modifier')}/>
          <Advanced.Column property="timestamp" header={this.i18n('entity.Audit.revisionDate')} sort face="datetime" rendered={_.includes(columns, 'revisionDate')}/>
          <Advanced.Column
            property="changedAttributes"
            rendered={_.includes(columns, 'changedAttributes')}
            cell={
              ({ rowIndex, data, property }) => {
                return _.replace(data[rowIndex][property], ',', ', ');
              }
            }
          />
          <Advanced.Column
            property="modifiedEntityNames" sort
            rendered={_.includes(columns, 'modifiedEntityNames')}
            cell={
              ({ rowIndex, data, property }) => {
                return this._getTypeArray(data[rowIndex][property]);
              }
            }
          />
        </Advanced.Table>
      </div>
    );
  }
}

AuditTable.propTypes = {
  // columns for display, check default props.
  columns: PropTypes.arrayOf(PropTypes.string),
  // simple name of entity class, if this paramater isn't defined show all revisions
  entityClass: PropTypes.string,
  // id of entity
  entityId: PropTypes.number,
  // callback for detail
  clickTarget: PropTypes.func,
  // flag for detail
  isDetail: PropTypes.boolean,
  // table uiKe
  tableUiKey: PropTypes.string
};

AuditTable.defaultProps = {
  columns: ['id', 'type', 'modification', 'modifier', 'revisionDate', 'changedAttributes'],
  isDetail: false,
  tableUiKey: 'audit-table'
};

function select() {
  return {
  };
}

export default connect(select)(AuditTable);
