import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';

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

  componentDidMount() {
    this.selectSidebarItem('audit-entities');
    this._reloadComponent(this.props);
  }

  /**
   * After change props is necessary to reload tables with data
   */
  componentWillReceiveProps(nextProps) {
    const { auditDetail } = this.props;

    if (auditDetail !== nextProps.auditDetail) {
      this._reloadComponent(nextProps);
    }
  }

  _getType(name) {
    const type = name.split('.');
    return type[type.length - 1];
  }

  _reloadComponent(props) {
    if (this.refs.form && props.auditDetail) {
      const revison = _.merge({ }, props.auditDetail);
      revison.modification = this.i18n('content.audit.revision.modificationType.' + revison.modification);
      revison.type = this._getType(revison.type);
      this.refs.form.setData(revison);
    }
  }

  render() {
    const { showLoading } = this.props;

    return (
      <Basic.AbstractForm ref="form" className="form-horizontal" readOnly showLoading={showLoading}>
        <Basic.TextField
          ref="id"
          label={this.i18n('revision.id')}/>
        <Basic.TextField
          ref="type"
          label={this.i18n('revision.type')}/>
        <Basic.TextField
          ref="modification"
          label={this.i18n('revision.modification')}/>
        <Basic.TextField
          ref="modifier"
          label={this.i18n('revision.modifier')}/>
        <Basic.DateTimePicker
        componentSpan="col-sm-8"
          ref="revisionDate"
          label={this.i18n('revision.revisionDate')}
          format="d. M. Y  H:mm:ss"/>
      </Basic.AbstractForm>
    );
  }
}

AuditDetailInfo.propTypes = {
  auditDetail: PropTypes.object,
  showLoading: PropTypes.bool
};

AuditDetailInfo.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(AuditDetailInfo);
