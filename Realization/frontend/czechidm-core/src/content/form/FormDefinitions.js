import React from 'react';
//
import * as Basic from '../../components/basic';
import { FormDefinitionManager } from '../../redux';
import FormDefinitionTable from './FormDefinitionTable';

/**
 * Form definitions and attributes (same agenda)
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class FormDefinitions extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new FormDefinitionManager();
  }

  getContentKey() {
    return 'content.formDefinitions';
  }

  getNavigationKey() {
    return 'forms';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}
        <Basic.Panel>
          <FormDefinitionTable uiKey="form-table" definitionManager={this.manager}/>
        </Basic.Panel>
      </div>
    );
  }
}

FormDefinitions.propTypes = {
};
FormDefinitions.defaultProps = {
};
