import React from 'react';
import * as Basic from '../../components/basic';
import FormValuesTable from './FormValuesTable';

/**
 * @author Roman Kučera
 */
export default class FormValues extends Basic.AbstractContent {

  getContentKey() {
    return 'content.formDefinitions.values';
  }

  componentDidMount() {
    super.componentDidMount();
  }

  getNavigationKey() {
    return 'forms-values';
  }

  render() {
    const { entityId } = this.props.params;
    return (
      <Basic.Panel className={'no-border last'}>
        <Basic.PanelHeader text={this.i18n('title')} />
        <Basic.PanelBody style={{ padding: 0 }}>
          <FormValuesTable uiKey="form-values-table" definitionId={ entityId } />
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}

FormValues.propTypes = {
};
FormValues.defaultProps = {
};
