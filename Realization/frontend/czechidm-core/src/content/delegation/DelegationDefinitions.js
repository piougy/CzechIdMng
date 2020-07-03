import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import DelegationDefinitionTable from './DelegationDefinitionTable';

/**
 * Definition of a delegation.
 *
 * @author Vít Švanda
 * @since 10.4.0
 */
export default class DelegationDefinitions extends Basic.AbstractContent {

  getContentKey() {
    return 'content.delegation-definitions';
  }

  getNavigationKey() {
    return 'delegation-definitions';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }
        <Basic.Panel>
          <Helmet title={ this.i18n('title') } />
          <DelegationDefinitionTable uiKey="delegation-definition-table"/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}
