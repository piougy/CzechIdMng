import React from 'react';
import * as Basic from '../../components/basic';
import TokenTable from './TokenTable';

/**
 * Token agenda.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export default class Tokens extends Basic.AbstractContent {

  getContentKey() {
    return 'content.tokens';
  }

  getNavigationKey() {
    return 'tokens';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Alert level="info" text={ this.i18n('content.tokens.help', { escape: false }) }/>

        <Basic.Panel>
          <TokenTable uiKey="token-table" match={ this.props.match } filterOpened/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}
