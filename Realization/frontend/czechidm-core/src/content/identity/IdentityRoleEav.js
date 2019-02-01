import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import FormInstance from '../../domain/FormInstance';

/**
 * Extended identity role attributes
 *
 * @author Vít Švanda
 */
export default class IdentityRoleEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.identity.roles';
  }

  render() {
    const { entity } = this.props;
    if ( entity
      && entity._eav
      && entity._eav.length === 1
      && entity._eav[0].formDefinition) {
      const formInstance = entity._eav[0];
      const _formInstance = new FormInstance(formInstance.formDefinition, formInstance.values);

      return (
        <Basic.Div className="abstract-form" style={{minWidth: 150, padding: 0}}>
          <Advanced.EavForm
            ref="eavForm"
            formInstance={ _formInstance }
            validationErrors={formInstance.validationErrors}
            readOnly
            useDefaultValue={false}/>
        </Basic.Div>
      );
    }
    return null;
  }
}
