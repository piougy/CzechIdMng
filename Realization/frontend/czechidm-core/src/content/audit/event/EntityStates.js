import React from 'react';
import {connect} from 'react-redux';
import * as Basic from '../../../components/basic';
import {EntityStateTable} from './EntityStateTable';
import {SecurityManager} from '../../../redux';

/**
 * Audit of entity events state
 *
 * @author artem
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
      <div>
        <EntityStateTable
          uiKey="entity-states"
          filterOpened
          showFilter
          show
          showRowSelection={SecurityManager.hasAuthority('APP_ADMIN')}/>
      </div>
    );
  }
}

export default connect()(EntityStates);
