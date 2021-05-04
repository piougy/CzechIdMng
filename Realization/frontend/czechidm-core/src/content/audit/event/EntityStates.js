import React from 'react';
import {connect} from 'react-redux';
import * as Basic from '../../../components/basic';
import EntityStateTable from './EntityStateTable';

/**
 * Audit of entity events state.
 *
 * @author artem
 * @author Radek Tomiška
 */
class EntityStates extends Basic.AbstractContent {

  getNavigationKey() {
    return 'entity-states';
  }

  getContentKey() {
    return 'content.entityStates';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <EntityStateTable
            uiKey="entity-states-table"
            filterOpened
            showRowSelection/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

export default connect()(EntityStates);
