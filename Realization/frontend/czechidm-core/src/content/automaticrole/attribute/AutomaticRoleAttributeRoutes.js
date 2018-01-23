import React from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import AutomaticRoleAttributeDetail from './AutomaticRoleAttributeDetail';

/**
 * Default content (routes diff) for automatic roles attribue
 *
 * @author Adamec Petr
 */
export default class AutomaticRoleAttributeRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.automaticRoles';
  }

  componentDidMount() {
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    if (query) {
      return query.new ? true : false;
    }
    return false;
  }

  render() {
    return (
      <div>
        {
          this._getIsNew()
          ?
          <div>
            <Basic.Panel>
              <AutomaticRoleAttributeDetail entity={{}}/>
            </Basic.Panel>
          </div>
          :
          <div>

              <Basic.ContentHeader text={
                  <div>
                    <Basic.Icon value="fa:universal-access"/> {this.i18n('content.automaticRoles.attribute.header')}
                  </div>
                }/>
          <Advanced.TabPanel position="left" parentId="automatic-role-attribute" params={this.props.params}>
            {this.props.children}
          </Advanced.TabPanel>
        </div>
        }
      </div>
    );
  }
}

AutomaticRoleAttributeRoutes.propTypes = {
};
AutomaticRoleAttributeRoutes.defaultProps = {
};
