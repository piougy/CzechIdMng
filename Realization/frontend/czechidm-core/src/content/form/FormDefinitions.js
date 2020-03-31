import React from 'react';
import Helmet from 'react-helmet';
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
    return 'form-definitions';
  }

  render() {
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <FormDefinitionTable uiKey="form-table" definitionManager={ this.manager }/>
      </Basic.Div>
    );
  }
}

FormDefinitions.propTypes = {
};
FormDefinitions.defaultProps = {
};
