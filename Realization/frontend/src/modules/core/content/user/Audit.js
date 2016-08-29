import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from 'app/components//basic';
import AuditTable from 'core/content/audit/AuditTable';
import { IdentityManager, DataManager } from 'core/redux/data';

const identityManager = new IdentityManager();

const uiKey = 'audit-';

class Audit extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  showDetail(revId, username) {
    this.context.router.push('/user/' + username + '/audit/' + revId);
  }

  componentDidMount() {
    const { userID } = this.props.params;
    this.selectSidebarItem('profile-audit');
    this.context.store.dispatch(identityManager.fetchRevisions(userID, uiKey + userID));
  }

  render() {
    const { _showLoading, auditEntities } = this.props;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('header')}
        </Basic.ContentHeader>

        <Basic.Panel>
          <Basic.Loading isStatic showLoading={_showLoading} />
          {
            !auditEntities
            ||
            <AuditTable auditEntities={auditEntities} clickTarget={this.showDetail} />
          }
        </Basic.Panel>
      </div>
    );
  }
}

Audit.propTypes = {
  auditEntities: PropTypes.arrayOf(React.PropTypes.object),
  _showLoading: PropTypes.bool,
};
Audit.defaultProps = {
  auditEntities: []
};

function select(state, component) {
  const { userID } = component.params;
  return {
    auditEntities: DataManager.getData(state, uiKey + userID),
    _showLoading: identityManager.isShowLoading(state, null, uiKey + userID)
  };
}

export default connect(select)(Audit);
