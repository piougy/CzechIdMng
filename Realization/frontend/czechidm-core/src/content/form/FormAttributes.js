import React from 'react';
//
import * as Basic from '../../components/basic';
import FormAttributeTable from './FormAttributeTable';

/**
* Attributes content for forms (table).
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
    const { entityId } = this.props.match.params;
    return (
      <Basic.Panel className="no-border tab-pane-table-body last">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <FormAttributeTable uiKey="form-attributes-table" definitionId={ entityId } className="no-margin" />

        <Basic.PanelFooter rendered={ this.isDevelopment() }>
          <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>{ this.i18n('button.back') }</Basic.Button>
        </Basic.PanelFooter>
      </Basic.Panel>
    );
  }
}
