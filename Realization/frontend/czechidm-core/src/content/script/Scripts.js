import React from 'react';
import * as Basic from '../../components/basic';
import { ScriptManager } from '../../redux';
import ScriptTable from './ScriptTable';

const SCRIPT_TABLE_UIKEY = 'scriptTableUikey';

/**
* Content with all scripts
*
* @author Ondřej Kopr
*/
export default class Scripts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.scriptManager = new ScriptManager();
  }

  getManager() {
    return this.scriptManager;
  }

  getContentKey() {
    return 'content.scripts';
  }

  getNavigationKey() {
    return 'scripts';
  }

  render() {
    return (
      <div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <ScriptTable
            scriptManager={ this.getManager() }
            uiKey={ SCRIPT_TABLE_UIKEY }
            filterOpened/>
        </Basic.Panel>
      </div>
    );
  }
}
