import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Default content (routes diff) for automatic role
 *
 * @author Vít Švanda
 */
export default class RoleAutomaticRoleRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.automaticRoles';
  }

  componentDidMount() {
  }

  render() {
    return (
      <div>
        <Basic.ContentHeader>
          <Basic.Icon value="fa:universal-access"/>
          {' '}
          {this.i18n('header')}
        </Basic.ContentHeader>

        <Advanced.TabPanel position="top" parentId="role-automatic-roles" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}

RoleAutomaticRoleRoutes.propTypes = {
};
RoleAutomaticRoleRoutes.defaultProps = {
};
