import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import AuditIdentityLoginTable from '../audit/identity/AuditIdentityLoginTable';
import { IdentityManager } from '../../redux/data';

const identityManager = new IdentityManager();

/**
 * Audit of logines
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 */
class AuditLogins extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    const { entityId } = this.props.match.params;
    this.selectNavigationItems(['identities', 'profile-audit', 'profile-audit-login']);
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
        <AuditIdentityLoginTable
          singleUserMod
          id={identity.id}
          uiKey={`identity-login-audit-table-${identity.id}`}/>
      </div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(AuditLogins);
