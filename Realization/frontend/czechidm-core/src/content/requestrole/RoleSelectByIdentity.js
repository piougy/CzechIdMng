import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { RoleManager, IdentityManager, IdentityRoleManager, IdentityContractManager } from '../../redux';
//
import SearchParameters from '../../domain/SearchParameters';

/**
* Component for select roles by identity.
*
* @author Ondrej Kopr
*/

const identityRoleManager = new IdentityRoleManager();

const IDENTITY_ROLE_BY_IDENTITY_UIKEY = 'identity-role-by-identity-';

class RoleSelectByIdentity extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.identityManger = new IdentityManager();
    this.roleManager = new RoleManager();
    this.identityContractManager = new IdentityContractManager();

    this.state = {
      selectedIdentity: null,
      defaultValues: {
        useValidFromIdentity: true,
        copyRoleParameters: true
      },
      useValidFromIdentity: true
    };
  }

  getComponentKey() {
    return 'content.task.IdentityRoleConceptTable.addByIdentity';
  }

  componentDidMount() {

  }

  getUiKey() {
    return this.props.uiKey;
  }

  getIdentityManager() {
    return this.identityManger;
  }

  getAllRoles(unique = true) {
    const selectedRoles = this.refs.identityRolesTree.getWrappedInstance().getValue();
    const { identityRoles } = this.props;

    const roles = [];
    if (identityRoles && identityRoles.length > 0) {
      for (const index in identityRoles) {
        if (identityRoles.hasOwnProperty(index)) {
          const identityRole = identityRoles[index];
          if (identityRole && identityRole._embedded && identityRole._embedded.role) {
            if (selectedRoles.length > 0 && _.indexOf(selectedRoles, identityRole.id) !== -1) {
              const role = identityRole._embedded.role;
              if (unique) {
                const exists = _.indexOf(roles, role) !== -1;
                if (!exists) {
                  roles.push(role);
                }
              } else {
                roles.push(role);
              }
            }
          }
        }
      }
    }
    return roles;
  }

  getValue() {
    return this.getAllRoles(true);
  }

  getAllIdentityRoles() {
    const { identityRoles } = this.props;
    const selectedIdentityRoles = this.refs.identityRolesTree.getWrappedInstance().getValue();
    if (selectedIdentityRoles.length > 0) {
      const finalIdentityRoles = [];
      for (const index in identityRoles) {
        if (identityRoles.hasOwnProperty(index)) {
          const identityRole = identityRoles[index];
          if ( _.indexOf(selectedIdentityRoles, identityRole.id) !== -1) {
            finalIdentityRoles.push(identityRole);
          }
        }
      }
      return finalIdentityRoles;
    }
    return identityRoles;
  }

  createRoleRequestByIdentity() {
    const { request } = this.props;
    const { selectedIdentity } = this.state;
    const identityRoles = this.getAllIdentityRoles();
    const identityRolesId = [];
    const identityContract = this.refs.identityContract.getValue();

    identityRoles.forEach(identityRole => {
      identityRolesId.push(identityRole.id);
    });

    const roleRequestByIdentity = {
      roleRequest: request.id,
      fromIdentity: selectedIdentity.id,
      identityRoles: identityRolesId,
      identityContract: identityContract.id,
      validFrom: this.refs.validFrom.getValue(),
      validTill: this.refs.validTill.getValue(),
      useValidFromIdentity: this.refs.useValidFromIdentity.getValue(),
      copyRoleParameters: this.refs.copyRoleParameters.getValue()
    };

    return roleRequestByIdentity;
  }

  getSelectedIdentity() {
    const { selectedIdentity } = this.state;
    return selectedIdentity;
  }

  _changeIdentity(value) {
    let selectedIdentity = null;
    if (value && value.id) {
      const searchParameters = identityRoleManager.getSearchParameters().setFilter('identityId', value.id);
      this.context.store.dispatch(identityRoleManager.fetchEntities(searchParameters, IDENTITY_ROLE_BY_IDENTITY_UIKEY, null));
      selectedIdentity = value;
    }
    this.setState({
      selectedIdentity
    });
  }

  _onChangeValidFromIdentity(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      useValidFromIdentity: event.currentTarget.checked
    });
  }

  getNiceLabelForIdentityRole(node) {
    if (node) {
      return this.roleManager.getNiceLabel(node._embedded.role);
    }
  }

  render() {
    const { identityRoles, identityRoleShowLoading, identityUsername } = this.props;
    const { selectedIdentity, useValidFromIdentity, defaultValues } = this.state;
    const existIdentityRoles = identityRoles && identityRoles.length > 0;
    return (
      <div>
        <Basic.AbstractForm
            ref="form"
            data={defaultValues}>
          <Basic.SelectBox
            ref="select"
            label={this.i18n('selectUser.label')}
            helpBlock={this.i18n('selectUser.help')}
            onChange={ this._changeIdentity.bind(this) }
            manager={this.getIdentityManager()}/>
          <Basic.SelectBox
            ref="identityContract"
            manager={ this.identityContractManager }
            forceSearchParameters={ new SearchParameters().setFilter('identity', identityUsername).setFilter('validNowOrInFuture', true) }
            label={ this.i18n('entity.IdentityRole.identityContract.label') }
            placeholder={ this.i18n('entity.IdentityRole.identityContract.placeholder') }
            helpBlock={ this.i18n('entity.IdentityRole.identityContract.help') }
            returnProperty={false}
            niceLabel={ (contract) => { return this.identityContractManager.getNiceLabel(contract, false); }}
            required
            useFirst/>
          <Basic.Row>
            <Basic.Col lg={ 6 }>
              <Basic.Checkbox
                ref="useValidFromIdentity"
                onChange={ this._onChangeValidFromIdentity.bind(this) }
                label={this.i18n('useValidFromIdentity.label')}
                helpBlock={this.i18n('useValidFromIdentity.help')}/>
            </Basic.Col>
            <Basic.Col lg={ 6 }>
              <Basic.Checkbox
                ref="copyRoleParameters"
                label={this.i18n('copyRoleParameters.label')}
                helpBlock={this.i18n('copyRoleParameters.help')}/>
            </Basic.Col>
          </Basic.Row>
          <Basic.Row>
            <Basic.Col lg={ 6 }>
              <Basic.DateTimePicker
                mode="date"
                ref="validFrom"
                readOnly={useValidFromIdentity}
                label={this.i18n('label.validFrom')}/>
            </Basic.Col>
            <Basic.Col lg={ 6 }>
              <Basic.DateTimePicker
                mode="date"
                ref="validTill"
                readOnly={useValidFromIdentity}
                label={this.i18n('label.validTill')}/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
        <Basic.Panel>
          <Basic.Alert rendered={existIdentityRoles} level="info" text={this.i18n('dev pocet roli ' + identityRoles.length)}/>
          <Basic.Alert rendered={!(existIdentityRoles && selectedIdentity)} level="info" text="dev: nejsou role"/>
          <Advanced.Tree
            showLoading={identityRoleShowLoading}
            ref="identityRolesTree"
            uiKey="identity-roles-tree"
            rendered={(existIdentityRoles && selectedIdentity)}
            nodeNiceLabel={this.getNiceLabelForIdentityRole.bind(this)}
            manager={identityRoleManager}
            forceSearchParameters={identityRoleManager.getSearchParameters().setFilter('identityId', selectedIdentity ? selectedIdentity.id : null)}
            multiSelect
            traverse
            nodeIconClassName={null}/>
        </Basic.Panel>
      </div>
    );
  }

}

function select(state) {
  return {
    identityRoles: identityRoleManager.getEntities(state, IDENTITY_ROLE_BY_IDENTITY_UIKEY),
    identityRoleShowLoading: identityRoleManager.isShowLoading(state, IDENTITY_ROLE_BY_IDENTITY_UIKEY)
  };
}

export default connect(select, null, null, { withRef: true})(RoleSelectByIdentity);
