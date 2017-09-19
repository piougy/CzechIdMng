
import React from 'react';
//
import { Basic, Managers} from 'czechidm-core';
import { SystemTable} from 'czechidm-acc';
import VsSystemService from '../../services/VsSystemService';

const identityManager = new Managers.IdentityManager();
const roleManager = new Managers.RoleManager();
const vsSystemService = new VsSystemService();

/**
 * Virtual system table
 *
 * @author Vít Švanda
 */
export default class VsSystemTable extends SystemTable {


  _createNewSystem() {
    this.refs[`confirm-new`].show(
      this.i18n(`vs:content.vs-system.action.create.message`),
      this.i18n(`vs:content.vs-system.action.create.header`),
      this._validateCreateDialog.bind(this)
    ).then(() => {
      const detail = this.refs['create-form'].getData();
      vsSystemService.createVirtualSystem(detail)
      .then(json => {
        console.log("rrrrrrrrrrr", json);
      })
      .catch(error => {
        this.addError(error);
      });
    }, () => {
      // Rejected
    });
  }

  _validateCreateDialog(result) {
    if (result === 'reject') {
      return true;
    }
    if (result === 'confirm' && this.refs['create-form'].isFormValid()) {
      return true;
    }
    return false;
  }

  getTableButtons(showAddButton) {
    return (
      [
        <Basic.Button
          level="success"
          key="add_button"
          className="btn-xs"
          onClick={this._createNewSystem.bind(this)}
          rendered={Managers.SecurityManager.hasAuthority('SYSTEM_CREATE') && showAddButton}>
          <Basic.Icon type="fa" icon="plus"/>
          {' '}
          {this.i18n('button.add')}
        </Basic.Button>
      ]
    );
  }

  render() {
    const parentRender = super.render();

    return (
      <div>
        {parentRender}
        <Basic.Confirm ref="confirm-new" level="success">
          <div style={{marginTop: '20px'}}>
            <Basic.AbstractForm ref="create-form">
              <Basic.TextField
                ref="name"
                label={this.i18n('vs:content.vs-system.name.label')}
                placeholder={this.i18n('vs:content.vs-system.name.placeholder')}
                required/>
              <Basic.SelectBox
                ref="implementers"
                label={this.i18n('vs:content.vs-system.implementers.label')}
                placeholder={this.i18n('vs:content.vs-system.implementers.placeholder')}
                manager={identityManager }
                value={null}
                multiSelect/>
              <Basic.SelectBox
                ref="implementerRoles"
                label={this.i18n('vs:content.vs-system.implementerRoles.label')}
                placeholder={this.i18n('vs:content.vs-system.implementerRoles.placeholder')}
                manager={roleManager }
                value={null}
                multiSelect/>
            </Basic.AbstractForm>
          </div>
        </Basic.Confirm>
      </div>
    );
  }
}
