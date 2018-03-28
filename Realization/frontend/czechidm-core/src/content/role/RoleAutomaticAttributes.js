import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import { AutomaticRoleAttributeManager } from '../../redux';
import AutomaticRoleAttributeTable from '../automaticrole/attribute/AutomaticRoleAttributeTable';

/**
 * Automatic roles - tab on role detail
 *
 * @author Vít Švanda
 */
export default class RoleAutomaticAttributes extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new AutomaticRoleAttributeManager();
  }

  getContentKey() {
    return 'content.automaticRoles.attribute';
  }

  getNavigationKey() {
    return 'role-automatic-role-attribute';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('roleId', this.props.params.entityId);
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />
          <AutomaticRoleAttributeTable
            uiKey="role-automatic-attributes-table"
            forceSearchParameters={ forceSearchParameters }
            manager={this.manager}
            columns={['name', 'role']}/>
      </div>
    );
  }
}
