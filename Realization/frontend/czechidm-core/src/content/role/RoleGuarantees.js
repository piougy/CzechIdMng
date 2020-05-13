import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import RoleGuaranteeRoleTable from './RoleGuaranteeRoleTable';
import RoleGuaranteeTable from './RoleGuaranteeTable';
import { SecurityManager, CodeListManager } from '../../redux';

const codeListManager = new CodeListManager();

/**
 * Role guarantees - by role only now
 *
 * @author Radek Tomiška
 */
export default class RoleGuarantees extends Basic.AbstractContent {

  componentDidMount() {
    super.componentDidMount();
    // load guarante type needed in sub tables and detail
    this.context.store.dispatch(codeListManager.fetchCodeListIfNeeded('guarantee-type'));
  }

  getContentKey() {
    return 'content.role.guarantees';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-guarantees', this.props.match.params);
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('role', this.props.match.params.entityId);
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        {
          !SecurityManager.hasAuthority('ROLEGUARANTEEROLE_READ')
          ||
          <Basic.Div>
            <Basic.ContentHeader icon="component:roles" text={ this.i18n('role.header') } style={{ marginBottom: 0 }}/>
            <RoleGuaranteeRoleTable
              uiKey="role-guarantee-role-table"
              forceSearchParameters={ forceSearchParameters }
              className="no-margin"
              match={ this.props.match }/>
          </Basic.Div>
        }
        {
          !SecurityManager.hasAuthority('ROLEGUARANTEE_READ')
          ||
          <Basic.Div>
            <Basic.ContentHeader icon="fa:group" text={ this.i18n('identity.header') } style={{ marginBottom: 0 }}/>
            <RoleGuaranteeTable
              uiKey="role-guarantee-table"
              forceSearchParameters={ forceSearchParameters }
              className="no-margin"
              match={ this.props.match }/>
          </Basic.Div>
        }
      </Basic.Div>
    );
  }
}
