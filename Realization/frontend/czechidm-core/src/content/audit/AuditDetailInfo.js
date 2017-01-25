import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';

/**
 * Audit detail with info about class IdmAudit
 *
 */
class AuditDetailInfo extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentWillReceiveProps(nextProps) {
    if (this.refs.revisionDiff) {
      this.refs.revisionDiff.setValue(nextProps.auditDetail);
    }
  }

  _getType(name) {
    const type = name.split('.');
    return type[type.length - 1];
  }


  render() {
    const { auditDetail, cbChangeSecondRev, useAsSelect, noVersion,
      forceSearchParameters, auditManager } = this.props;

    return (
      <Basic.AbstractForm ref="form" className="form-horizontal" data={noVersion ? {revisionDate: null} : auditDetail} showLoading={auditDetail === null && !noVersion}>
        <Basic.TextField hidden={useAsSelect}
          ref="id" readOnly
          label={this.i18n('revision.id')}/>
        {
          !useAsSelect
          ||
          <Basic.SelectBox
            ref="revisionDiff" rendered={useAsSelect}
            value={auditDetail}
            label={this.i18n('revision.id')}
            onChange={cbChangeSecondRev}
            forceSearchParameters={forceSearchParameters}
            manager={auditManager}/>
        }
        <Basic.TextField
          ref="entityId" readOnly
          label={this.i18n('revision.entityId')}/>
        <Basic.TextField
          ref="type" readOnly
          label={this.i18n('revision.type')}/>
        <Basic.TextField
          ref="modification" readOnly
          label={this.i18n('revision.modification')}/>
        <Basic.TextField
          ref="modifier" readOnly
          label={this.i18n('revision.modifier')}/>
        <Basic.DateTimePicker
          componentSpan="col-sm-8"
          ref="revisionDate" readOnly
          label={this.i18n('revision.revisionDate')}
          format="d. M. Y  H:mm:ss"/>
      </Basic.AbstractForm>
    );
  }
}

AuditDetailInfo.propTypes = {
  auditDetail: PropTypes.object,
  showLoading: PropTypes.bool,
  cbChangeSecondRev: PropTypes.object,
  forceSearchParameters: PropTypes.object,
  auditManager: PropTypes.object,
  useAsSelect: PropTypes.bool,
  revID: PropTypes.number,
  noVersion: PropTypes.bool
};

AuditDetailInfo.defaultProps = {
  useAsSelect: false,
  noVersion: false
};

function select() {
  return {
  };
}

export default connect(select)(AuditDetailInfo);
