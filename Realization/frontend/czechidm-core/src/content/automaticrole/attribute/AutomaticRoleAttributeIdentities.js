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

  getContentKey() {
    return 'content.automaticRoles.attribute';
  }

  isEmbeddedDetail() {
    const { entityId } = this.props.match.params;
    //
    return !!entityId;
  }

  getNavigationKey() {
    if (this.isEmbeddedDetail()) {
      return 'role-automatic-role-attribute-identities';
    }
    return 'automatic-role-attribute-identities';
  }

  render() {
    const { automaticRoleId } = this.props.match.params;
    const forceSearchParameters = new SearchParameters().setFilter('automaticRoleId', automaticRoleId);
    //
    return (
      <Basic.Div className="tab-pane-table-body">
        <Helmet title={ this.i18n('edit.title') } />
        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          { this.i18n('identities.header') }
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <IdentityTable
            uiKey="automatic-role-identities-table"
            identityManager={ this.getManager() }
            filterOpened={ false }
            forceSearchParameters={ forceSearchParameters }
            showAddButton={ false }
            className={ !this.isEmbeddedDetail() ? 'no-margin' : '' }/>
        </Basic.Panel>
      </Basic.Div>
    );
  }

}

AutomaticRoleAttributeIdentities.propTypes = {
};
AutomaticRoleAttributeIdentities.defaultProps = {
};
