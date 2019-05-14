import React from 'react';
//
import * as Basic from '../../components/basic';
import FormAttributeTable from './FormAttributeTable';

/**
* Attributes content for forms (table)
*
* @author Roman Kučera
* @author Radek Tomiška
*/
export default class FormAttributes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.formAttributes';
  }

  getNavigationKey() {
    return 'forms-attributes';
  }

  render() {
    const { entityId } = this.props.params;
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <FormAttributeTable uiKey="form-attributes-table" definitionId={ entityId } className="no-margin" />
      </div>
    );
  }
}
