import React from 'react';
import * as Basic from '../../components/basic';
import { FormDefinitionManager } from '../../redux';
import FormTable from './FormTable';

/**
 * Form definitions and attributes (same agenda)
 */
export default class Forms extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new FormDefinitionManager();
  }

  getContentKey() {
    return 'content.formDefinitions';
  }

  /**
   * override getNavigationKey with specific for forms agenda (form definition and form attributes same agenda
   */
  getNavigationKey() {
    return 'forms';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}
        <Basic.Panel>
          <FormTable uiKey="form-table" definitionManager={this.manager}/>
        </Basic.Panel>
      </div>
    );
  }
}

Forms.propTypes = {
};
Forms.defaultProps = {
};
