import React from 'react';
import Immutable from 'immutable';
import _ from 'lodash';
//
import * as Basic from '../../basic';
import BusinessRoleIcon from './BusinessRoleIcon';
import MainContractIcon from './MainContractIcon';
import IdentityIcon from './IdentityIcon';
import ComponentService from '../../../services/ComponentService';

const componentService = new ComponentService();

/**
 * Advanced icons
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class Icons extends Basic.AbstractComponent {

  render() {
    const { rendered, showLoading } = this.props;
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Icon value="refresh" showLoading />
      );
    }
    //
    const components = componentService.getComponentDefinitions(ComponentService.ICON_COMPONENT_TYPE);
    let entityTypes = new Immutable.OrderedSet();
    components.forEach(component => {
      if (!component.entityType) {
        return true;
      }
      // multiple types
      if (_.isArray(component.entityType)) {
        for (const entityTypeItem of component.entityType) {
          entityTypes = entityTypes.add(entityTypeItem.toLowerCase());
        }
      } else {
        // single value
        entityTypes = entityTypes.add(component.entityType.toLowerCase());
      }
    });
    //
    // render available icons as example
    return (
      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'flex-end' }}>
        {
          [...entityTypes.sort((one, two) => one > two).map(entityType => {
            return (
              <div style={{ textAlign: 'center', padding: 15 }}>
                <Basic.Icon type="component" icon={ entityType } className="fa-2x" />
                <div style={{ marginTop: 5 }}>
                  { entityType }
                </div>
              </div>
            );
          }).values()]
        }
      </div>
    );
  }
}

Icons.BusinessRoleIcon = BusinessRoleIcon;
Icons.MainContractIcon = MainContractIcon;
Icons.IdentityIcon = IdentityIcon;
