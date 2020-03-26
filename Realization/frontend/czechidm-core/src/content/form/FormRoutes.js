import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Form definitions projections entry point.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class FormRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.formDefinitions';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Advanced.TabPanel position="top" parentId="forms" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}
