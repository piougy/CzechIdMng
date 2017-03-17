import React from 'react';
//
import * as Basic from '../../components/basic';
import FormAttributeTable from './FormAttributeTable';

/**
* Attributes content for forms (table)
*/

export default class FormAttributes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.formAttributes';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'forms', 'forms-attributes']);
  }

  render() {
    const { entityId } = this.props.params;
    return (
      <Basic.Panel className={'no-border last'}>
        <Basic.PanelHeader text={this.i18n('content.formDefinitions.attributes.title')} />
        <Basic.PanelBody style={{ padding: 0 }}>
        <FormAttributeTable formDefinitionId={entityId} />
        </Basic.PanelBody>
        <Basic.PanelFooter>
          <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
        </Basic.PanelFooter>
      </Basic.Panel>
    );
  }
}

FormAttributes.propTypes = {
};
FormAttributes.defaultProps = {
};
