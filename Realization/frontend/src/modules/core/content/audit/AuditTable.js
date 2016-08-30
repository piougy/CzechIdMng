import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Utils from 'core/utils';
import * as Basic from 'app/components/basic';
import * as Advanced from 'app/components/advanced';

/**
* Table of Audit for entities
*/

export class AuditTable extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  render() {
    const { auditEntities, clickTarget } = this.props;
    return (
      <div>
        <Basic.Table
          data={auditEntities._embedded.resources}
          noData={this.i18n('audit.empty')}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>
          <Basic.Column
            className="detail-button"
            cell={
              ({rowIndex, data}) => {
                return (
                  <Advanced.DetailButton onClick={clickTarget.bind(this, data[rowIndex].revisionNumber, data[rowIndex].entity.username)}/>
                );
              }
            }/>
          <Basic.Column
            header={this.i18n('id')}
            property="metadata.revisionNumber"
          />
          <Basic.Column
            header={this.i18n('date')}
            property="metadata.delegate.revisionDate"
            cell={<Basic.DateCell format={this.i18n('format.datetime')}/>}
          />
          <Basic.Column
            header={this.i18n('modifier')}
            property="entity.modifier"
          />
        </Basic.Table>
      </div>
    );
  }
}

AuditTable.propTypes = {
  auditEntities: PropTypes.object,
  clickTarget: PropTypes.func
};

AuditTable.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select, null, null, { withRef: true })(AuditTable);
