import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import SearchParameters from '../../../domain/SearchParameters';
import { IdentityManager } from '../../../redux';
import IdentityTable from '../../identity/IdentityTable';

/**
 * Detail with identities that have assigned this automatic role
 *
 * @author Radek Tomiška
 */
export default class AutomaticRoleAttributeIdentities extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    if (entityId) {
      this.selectNavigationItems(['roles-menu', 'roles', 'role-automatic-roles', 'role-automatic-role-attribute', 'role-automatic-role-attribute-identities']);
    } else {
      this.selectNavigationItems(['roles-menu', 'automatic-roles', 'automatic-role-attribute-identities']);
    }
  }

  getContentKey() {
    return 'content.automaticRoles.attribute';
  }

  getNavigationKey() {
    return 'automatic-role-attribute-identities';
  }

  render() {
    const { automaticRoleId } = this.props.params;
    const forceSearchParameters = new SearchParameters().setFilter('automaticRoleId', automaticRoleId);
    //
    return (
      <div className="tab-pane-table-body">
        <Helmet title={this.i18n('edit.title')} />
        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          { this.i18n('identities.header') }
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <IdentityTable
            uiKey="automatic-role-identities-table"
            identityManager={ this.getManager() }
            filterOpened={ false }
            forceSearchParameters={ forceSearchParameters }
            showAddButton={ false }/>
        </Basic.Panel>
      </div>
    );
  }

}

AutomaticRoleAttributeIdentities.propTypes = {
};
AutomaticRoleAttributeIdentities.defaultProps = {
};
