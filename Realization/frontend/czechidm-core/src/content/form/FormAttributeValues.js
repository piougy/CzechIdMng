import React from 'react';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import FormValueTableComponent, { FormValueTable } from './FormValueTable';
import SearchParameters from '../../domain/SearchParameters';

/**
 * Form values - for given attribute
 *
 * @author Roman Kučera
 * @author Radek Tomiška
 */
export default class FormAttributeValues extends Basic.AbstractContent {

  getContentKey() {
    return 'content.form-values';
  }

  getNavigationKey() {
    return 'form-attribute-values';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('attributeId', this.props.match.params.entityId);
    //
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <FormValueTableComponent
          uiKey="form-attribute-values-table"
          forceSearchParameters={ forceSearchParameters }
          showFilter={ false }
          columns={ _.difference(FormValueTable.defaultProps.columns, ['code', 'name']) }
          className="no-margin"/>
      </div>
    );
  }
}
