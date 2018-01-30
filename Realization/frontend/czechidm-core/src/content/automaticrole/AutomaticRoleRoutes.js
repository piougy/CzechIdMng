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
