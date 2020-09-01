import React from 'react';
//
import * as Basic from '../../components/basic';
import CodeListTable from './CodeListTable';

/**
 * Code lists and items.
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class CodeLists extends Basic.AbstractContent {

  getContentKey() {
    return 'content.code-lists';
  }

  getNavigationKey() {
    return 'code-lists';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }
        <Basic.Panel>
          <CodeListTable uiKey="code-list-table"/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}
