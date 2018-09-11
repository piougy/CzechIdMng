import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import RoleCompositionTable from './RoleCompositionTable';

/**
 * Role composition - define superior / sub roles
 *
 * @author Radek Tomiška
 */
export default class RoleCompositions extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.compositions';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-compositions', this.props.params);
  }

  render() {
    const forceSuperiorSearchParameters = new SearchParameters().setFilter('subId', this.props.params.entityId);
    const forceSubSearchParameters = new SearchParameters().setFilter('superiorId', this.props.params.entityId);
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader icon="arrow-up" text={ this.i18n('superior.header') } style={{ marginBottom: 0 }}/>
        <RoleCompositionTable
          uiKey="role-composition-superior-table"
          forceSearchParameters={ forceSuperiorSearchParameters }
          className="no-margin"
          params={ this.props.params }/>

        <Basic.ContentHeader icon="arrow-down" text={ this.i18n('sub.header') } style={{ marginBottom: 0 }}/>
        <RoleCompositionTable
          uiKey="role-composition-sub-table"
          forceSearchParameters={ forceSubSearchParameters }
          className="no-margin"
          params={ this.props.params }/>
      </div>
    );
  }
}
