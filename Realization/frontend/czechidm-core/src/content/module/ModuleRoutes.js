import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Modules agendas entry point
 *
 * @author Radek Tomiška
 */
export default class ModuleRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.system.modules';
  }

  render() {
    return (
      <div>
        <Basic.PageHeader>
          { this.i18n('header') }
        </Basic.PageHeader>

        <Advanced.TabPanel position="top" parentId="modules" params={ this.props.params }>
          { this.props.children }
        </Advanced.TabPanel>
      </div>
    );
  }
}
