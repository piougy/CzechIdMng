import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import IncompatibleRoleTable from './IncompatibleRoleTable';

/**
 * Incompatible role - defines Segregation of Duties.
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class IncompatibleRoles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.incompatible-roles';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('incompatible-roles', this.props.params);
  }

  render() {
    const forceSuperiorSearchParameters = new SearchParameters().setFilter('subId', this.props.params.entityId);
    const forceSubSearchParameters = new SearchParameters().setFilter('superiorId', this.props.params.entityId);
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader icon="warning-sign" text={ this.i18n('sub.header') } style={{ marginBottom: 0 }}/>
        <IncompatibleRoleTable
          uiKey="incompatible-role-sub-table"
          forceSearchParameters={ forceSubSearchParameters }
          className="no-margin"
          params={ this.props.params }/>

        <Basic.ContentHeader icon="arrow-up" text={ this.i18n('superior.header') } style={{ marginBottom: 0 }}/>
        <IncompatibleRoleTable
          uiKey="incompatible-role-superior-table"
          forceSearchParameters={ forceSuperiorSearchParameters }
          className="no-margin"
          params={ this.props.params }
          readOnly/>
      </div>
    );
  }
}
