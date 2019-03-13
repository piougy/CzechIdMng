import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import AuditIdentityPasswordChangeTable from '../audit/identity/AuditIdentityPasswordChangeTable';
import { IdentityManager } from '../../redux/data';

const identityManager = new IdentityManager();

/**
 * Audit of password changes
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 */
class AuditPasswordChanges extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['identities', 'profile-audit', 'profile-audit-password-change']);
    this.context.store.dispatch(identityManager.fetchEntity(entityId));
  }

  render() {
    const { identity } = this.props;
    if (!identity) {
      return (
        <Basic.Div showLoading>
          <Helmet title={this.i18n('title')} />
        </Basic.Div>
      );
    }
    return (
      <div>
        <Helmet title={this.i18n('title')} />
          <AuditIdentityPasswordChangeTable
            singleUserMod
            id={identity.id}
            uiKey={`identity-password-change-audit-table-${identity.id}`}/>
      </div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(AuditPasswordChanges);
