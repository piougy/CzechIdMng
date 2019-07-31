import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Domain from '../../domain';
import { SecurityManager, IdentityManager } from '../../redux';
import IdentityRoleTableComponent, { IdentityRoleTable } from '../identity/IdentityRoleTable';

const identityManager = new IdentityManager();

/**
 * Assigned roles to identity
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
class IdentityRoleDashboard extends Basic.AbstractContent {

  render() {
    const { identity, entityId, permissions } = this.props;
    //
    if (!entityId || !SecurityManager.hasAuthority('IDENTITYROLE_READ')) {
      return null;
    }
    //
    return (
      <Basic.Div>
        <Basic.ContentHeader
          icon="component:identity-role"
          text={ this.i18n('content.identity.roles.directRoles.header') }
          buttons={
            identityManager.canRead(identity, permissions)
            ?
              [
                <Link to={ `/identity/${ encodeURIComponent(entityId) }/roles` }>
                  <Basic.Icon value="fa:angle-double-right"/>
                  {' '}
                  { this.i18n('component.advanced.IdentityInfo.link.detail.label') }
                </Link>
              ]
            :
            null
          } />
        <Basic.Panel>
          <IdentityRoleTableComponent
            uiKey={ `dashboard-${ entityId }` }
            forceSearchParameters={
              new Domain.SearchParameters()
                .setFilter('identityId', entityId)
                .setFilter('directRole', true)
                .setFilter('addEavMetadata', true)
            }
            showAddButton={ false }
            params={{ ...this.props.params, entityId }}
            columns={ _.difference(IdentityRoleTable.defaultProps.columns, ['directRole', 'contractPosition']) }
            _permissions={ permissions }/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select)(IdentityRoleDashboard);
