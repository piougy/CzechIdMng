import React from 'react';
import * as Basic from '../../components/basic';
import { RoleCatalogueManager } from '../../redux';
import RoleCatalogueTable from './RoleCatalogueTable';

/**
 * List of roles catalogues
 *
 * @author Ondřej Kopr
 */
export default class RoleCatalogues extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.roleCatalogueManager = new RoleCatalogueManager();
  }

  getContentKey() {
    return 'content.roleCatalogues';
  }

  /**
   * override getNavigationKey with specific for roles catalogues
   */
  getNavigationKey() {
    return 'role-catalogues';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <RoleCatalogueTable uiKey="role_catalogue_table" roleCatalogueManager={ this.roleCatalogueManager }/>
        </Basic.Panel>

      </Basic.Div>
    );
  }
}

RoleCatalogues.propTypes = {
};
RoleCatalogues.defaultProps = {
};
