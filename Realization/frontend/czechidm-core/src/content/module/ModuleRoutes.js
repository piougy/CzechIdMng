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
      <Basic.Div>
        { this.renderPageHeader({ icon: 'fa:puzzle-piece' }) }

        <Advanced.TabPanel position="top" parentId="modules" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}
