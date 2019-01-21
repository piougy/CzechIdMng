import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Default content (routes diff) for audits
 *
 * @author Ondřej Kopr
 */
export default class AutomaticRoleRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.automaticRoles';
  }

  getNavigationKey() {
    return 'automatic-roles';
  }

  selectNavigationItem() {
    // nothing
  }

  render() {
    return (
      <div>
        { this.renderPageHeader() }

        <Advanced.TabPanel position="top" parentId="automatic-roles" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}

AutomaticRoleRoutes.propTypes = {
};
AutomaticRoleRoutes.defaultProps = {
};
