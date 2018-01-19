import React from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';

/**
 * Default content (routes diff) for audits
 *
 * @author Adamec Petr
 */
export default class AutomaticRoleAttributeRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.automaticRoles';
  }

  componentDidMount() {
  }

  render() {
    return (
      <div>
        <Basic.PageHeader>
          <Basic.Icon value="fa:universal-access"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Advanced.TabPanel position="left" parentId="automatic-role-attribute" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}

AutomaticRoleAttributeRoutes.propTypes = {
};
AutomaticRoleAttributeRoutes.defaultProps = {
};
