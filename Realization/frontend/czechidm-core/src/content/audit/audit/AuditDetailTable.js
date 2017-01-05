import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import { AuditManager } from '../../../redux';

const auditManager = new AuditManager();

/**
* Table for detail audits
*/

export class AuditDetailTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
  }

  componentWillUnmount() {
  }

  getContentKey() {
    return 'content.audit';
  }

  /**
   * Method for show detail of revision, redirect to detail
   *
   * @param entityId id of revision
   */
  showDetail(entityId) {
    this.context.router.push(`/audit/entities/${entityId}`);
  }

  _getForceSearchParameters() {
    const { entityId, entityClass } = this.props;

    if (entityId !== undefined || entityClass !== undefined) {
      return auditManager.getDefaultSearchParameters().setFilter('type', entityClass).setFilter('entityId', entityId);
    }
    return null;
  }

  render() {
    const { detail, weight, diffValues, diffRowClass } = this.props;
    if (detail === null || detail.revisionValues === null) {
      return null;
    }

    // transform revision values for table, key=>value
    const transformData = [];
    let index = 0;
    for (const key in detail.revisionValues) {
      if (detail.revisionValues.hasOwnProperty(key)) {
        const row = {
          key,
          'value': detail.revisionValues[key]};
        transformData[index] = row;

        index++;
      }
    }

    return (
      <div className={weight}>
        <Basic.Table
          data={transformData}
          noData={this.i18n('revision.deleted')}
          rowClass={({ rowIndex, data }) => {
            if (diffValues && diffValues[data[rowIndex].key]) {
              return diffRowClass;
            }
          }}>
          <Basic.Column
            property="key"
            header={this.i18n('entity.Audit.key')}/>
          <Basic.Column
            property="value"
            header={this.i18n('entity.Audit.value')}/>
        </Basic.Table>
      </div>
    );
  }
}

AuditDetailTable.propTypes = {
  // columns for display, check default props.
  columns: PropTypes.arrayOf(PropTypes.string),
  // detail for audit
  detail: PropTypes.object,
  // diff value for check
  diffValues: PropTypes.object,
  // weight of table
  weight: PropTypes.string,
  // Class for row when diffValues contains key
  diffRowClass: PropTypes.string

};

AuditDetailTable.defaultProps = {
  columns: ['id', 'type', 'modification', 'modifier', 'revisionDate', 'changedAttributes'],
  weight: 'col-md-6',
  diffRowClass: 'warning'
};

function select() {
  return {
  };
}

export default connect(select)(AuditDetailTable);
