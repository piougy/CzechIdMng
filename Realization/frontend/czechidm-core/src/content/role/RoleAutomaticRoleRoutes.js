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

  getNavigationKey() {
    const isRequest = this.isRequest(this.props.params);
    if (isRequest) {
      return this.getRequestNavigationKey('role-automatic-roles', this.props.params);
    }
    return super.getNavigationKey();
  }

  _gotToRequests() {
    // Redirect to requests
    this.context.router.push(`/automatic-role/trees`);
  }

  render() {
    const isRequest = this.isRequest(this.props.params);

    if (isRequest) {
      return (
        <Basic.Row>
          <div className="col-lg-6">
            <Basic.Alert
              level="info"
              title={this.i18n('content.automaticRoles.universalRequestNotSupported.title')}
              text={this.i18n('content.automaticRoles.universalRequestNotSupported.text')}
              buttons={[
                <Basic.Button
                  level="primary"
                  key="gotToRequests"
                  style={{marginLeft: '5px'}}
                  onClick={ this._gotToRequests.bind(this) }
                  titlePlacement="bottom">
                  <Basic.Icon type="fa" icon="magic"/>
                  {' '}
                  { this.i18n('content.automaticRoles.universalRequestNotSupported.gotToRequests.label') }
                </Basic.Button>
              ]}/>
            </div>
        </Basic.Row>
      );
    }
    return (
      <div>
        <Basic.ContentHeader>
          <Basic.Icon value="fa:magic"/>
          {' '}
          {this.i18n('header')}
        </Basic.ContentHeader>

        <Advanced.TabPanel
          position="top"
          parentId={'role-automatic-roles'}
          params={this.props.params}>
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
