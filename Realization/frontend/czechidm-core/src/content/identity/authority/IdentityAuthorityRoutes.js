import React from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { HelpContent } from '../../../domain';

/**
 * Identity authority routes - authorities and authorization policies.
 *
 * @author Radek Tomiška
 * @since 10.5.0
 */
export default class IdentityAuthorityRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.identity.authorities';
  }

  getHelp() {
    let helpContent = new HelpContent();
    helpContent = helpContent.setHeader(this.i18n('help.header'));
    helpContent = helpContent.setBody(
      <Basic.Div>
        <Basic.Div>
          { this.i18n('help.body.title', { escape: false }) }
        </Basic.Div>
        <Basic.Div style={{ marginTop: 15 }}>
          { this.i18n('help.body.checkbox.title', { escape: false }) }
        </Basic.Div>
        <ul>
          <li className="hidden">
            <Basic.Icon value="fa:square-o" style={{ marginRight: 5 }}/>
            { this.i18n('help.body.checkbox.none', { escape: false }) }
          </li>
          <li>
            <Basic.Icon value="fa:minus-square-o" style={{ marginRight: 5 }}/>
            { this.i18n('help.body.checkbox.some', { escape: false }) }
          </li>
          <li>
            <Basic.Icon value="fa:check-square-o" style={{ marginRight: 5 }}/>
            { this.i18n('help.body.checkbox.all', { escape: false }) }
          </li>
        </ul>
      </Basic.Div>
    );
    //
    return helpContent;
  }

  render() {
    return (
      <Basic.Div>
        { this.renderContentHeader({ help: this.getHelp() }) }

        <Advanced.TabPanel position="top" parentId="profile-authority" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}
