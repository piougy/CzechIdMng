import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { AuditManager } from '../../redux';

const auditManager = new AuditManager();

const MOD_ADD = 'ADD';

/**
* Table for detail audits
*
* @author Ondřej Kopr
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

  /**
   * Method set nice label for value from audit version
   * TODO: link to detail of UUID?
   */
  _prepareValue(value) {
    if (typeof value === 'boolean') {
      return value ? 'true' : 'false'; // TODO? localized?
    } else if (value === null) {
      return 'null';
    }

    return value;
  }

  _prepareData(revisionValues) {
    const transformData = [];
    let index = 0;
    for (const key in revisionValues) {
      if (revisionValues.hasOwnProperty(key)) {
        if (revisionValues[key] instanceof Object) {
          for (const keySec in revisionValues[key]) {
            if (revisionValues[key].hasOwnProperty(keySec)) {
              const row = {
                'key': keySec,
                'value': this._prepareValue(revisionValues[key][keySec])
              };
              transformData[index] = row;

              index++;
            }
          }
        } else {
          const row = {
            key,
            'value': this._prepareValue(revisionValues[key])
          };
          transformData[index] = row;

          index++;
        }
      }
    }
    return transformData;
  }

  render() {
    const { detail, weight, diffValues, diffRowClass, showLoading } = this.props;
    if (detail === null || detail.revisionValues === null) {
      return null;
    }

    // transform revision values for table, key=>value
    const transformData = this._prepareData(detail.revisionValues);

    return (
      <div className={weight}>
        <Basic.Table
          showLoading={showLoading}
          data={transformData}
          noData={detail.modification === MOD_ADD ? this.i18n('revision.created') : this.i18n('revision.deleted') }
          rowClass={({ rowIndex, data }) => {
            if (diffValues && diffValues[data[rowIndex].key] !== undefined) {
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
  diffRowClass: PropTypes.string,
  showLoading: PropTypes.bool
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
