import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import FormProjectionTable from './FormProjectionTable';

/**
 * Form projections.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class FormProjections extends Basic.AbstractContent {

  getContentKey() {
    return 'content.form-projections';
  }

  getNavigationKey() {
    return 'form-projections';
  }

  render() {
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <FormProjectionTable uiKey="form-projection-table"/>
      </Basic.Div>
    );
  }
}
