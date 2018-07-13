import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import RoleGuaranteeRoleTable from './RoleGuaranteeRoleTable';

/**
 * Role guarantees - by role only now
 *
 * @author Radek Tomiška
 */
export default class RoleGuarantees extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.guarantees';
  }

  getNavigationKey() {
    return 'role-guarantees';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('role', this.props.params.entityId);
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('header')}
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <RoleGuaranteeRoleTable
            uiKey="role-guarantee-role-table"
            forceSearchParameters={ forceSearchParameters }/>
        </Basic.Panel>
      </div>
    );
  }
}
