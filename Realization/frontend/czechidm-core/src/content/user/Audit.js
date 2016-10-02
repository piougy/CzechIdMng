import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components//basic';
import AuditTable from '../audit/AuditTable';
import { IdentityManager, DataManager } from '../../redux/data';

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
    this.context.router.push('/user/' + username + '/revision/' + revId);
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectSidebarItem('profile-audit');
    this.context.store.dispatch(identityManager.fetchRevisions(entityId, uiKey + entityId));
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

        <Basic.Panel className="no-border last">
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
  const { entityId } = component.params;
  return {
    auditEntities: DataManager.getData(state, uiKey + entityId),
    _showLoading: identityManager.isShowLoading(state, null, uiKey + entityId)
  };
}

export default connect(select)(Audit);
