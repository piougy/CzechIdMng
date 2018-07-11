import React from 'react';
import * as Basic from '../../components/basic';
import FormValuesTable from './FormValuesTable';
import SearchParameters from '../../domain/SearchParameters';

/**
 * @author Roman Kučera
 */
export default class FormAttributeValues extends Basic.AbstractContent {

  getContentKey() {
    return 'content.formValues';
  }

  componentDidMount() {
    super.componentDidMount();
  }

  getNavigationKey() {
    return 'forms-attribute-values';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('attributeId', this.props.params.entityId);
    return (
      <Basic.Panel className={'no-border last'}>
        <Basic.PanelHeader text={this.i18n('title')} />
        <Basic.PanelBody style={{ padding: 0 }}>
          <FormValuesTable uiKey="form-values-table" forceSearchParameters={forceSearchParameters} />
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}

FormAttributeValues.propTypes = {
};
FormAttributeValues.defaultProps = {
};
