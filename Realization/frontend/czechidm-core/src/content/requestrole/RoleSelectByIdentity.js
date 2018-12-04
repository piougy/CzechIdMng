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

const TREE_COMPONENT_HEIGHT = 400;

const IDENTITY_ROLE_BY_IDENTITY_UIKEY = 'identity-role-by-identity';

class RoleSelectByIdentity extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.identityManger = new IdentityManager();
    this.roleManager = new RoleManager();
    this.identityContractManager = new IdentityContractManager();

    this.state = {
      selectedIdentity: null,
      roleRoots: [],
      selectedRoles: []
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

  getValue() {
    const { selectedRoles } = this.state;
    return selectedRoles;
  }

  /**
   * Create role request that conrespond with roleRequestByIdentity on backend.
   * Whole request can be send to backend into rest.
   */
  createRoleRequestByIdentity() {
    const { request } = this.props;
    const { selectedRoles } = this.state;
    const identityContract = this.refs.identityContract.getValue();

    const roleRequestByIdentity = {
      roleRequest: request.id,
      roles: selectedRoles,
      identityContract: identityContract.id,
      validFrom: this.refs.validFrom.getValue(),
      validTill: this.refs.validTill.getValue(),
    };

    return roleRequestByIdentity;
  }

  /**
   * Return selected identity
   */
  getSelectedIdentity() {
    const { selectedIdentity } = this.state;
    return selectedIdentity;
  }

  /**
   * Catch onchange on identity selectbox and reload identity roles.
   */
  _changeIdentity(value) {
    if (value && value.id) {
      const roleRoots = [];
      const searchParameters = identityRoleManager.getSearchParameters().setFilter('identityId', value.id).setFilter('directRole', 'true').setFilter('automaticRole', 'false').setSize(100000);
      this.context.store.dispatch(identityRoleManager.fetchEntities(searchParameters, IDENTITY_ROLE_BY_IDENTITY_UIKEY, json => {
        // Returned json and inner embbeded with identity roles must exists
        if (json && json._embedded && json._embedded.identityRoles) {
          const identityRoles = json._embedded.identityRoles;
          // Iterate over all identity roles
          for (const index in identityRoles) {
            if (identityRoles.hasOwnProperty(index)) {
              const identityRole = identityRoles[index];
              // Check if identityRole has role as embedded
              if (identityRole && identityRole._embedded && identityRole._embedded.role) {
                const role = identityRole._embedded.role;
                // We want only unique
                if (_.findIndex(roleRoots, rootRole => {
                  return rootRole.id === role.id;
                }) === -1) {
                  roleRoots.push(role);
                }
              }
            }
          }
        }
        this.setState({
          selectedIdentity: value,
          roleRoots
        });
      }));
    }
  }

  /**
   * Add all roles into selection
   */
  _addAllRoles(event) {
    if (event) {
      event.preventDefault();
    }
    const { selectedRoles, roleRoots } = this.state;

    // In add all case is mandatory check id, because roleRoots is object.
    roleRoots.forEach(newRole => {
      if (_.findIndex(selectedRoles, (selectRole) => {
        return selectRole === newRole.id;
      }) === -1) {
        selectedRoles.push(newRole.id);
      }
    });
    this.setState({
      selectedRoles
    });
    this._reloadTrees();
  }

  /**
   * Add only selected roles to selection
   */
  _addSelectedRoles(event) {
    if (event) {
      event.preventDefault();
    }
    const { selectedRoles } = this.state;
    const newRoles = this.refs.roleSelect.getWrappedInstance().getValue();
    newRoles.forEach(newRole => {
      if (_.findIndex(selectedRoles, (selectRole) => {
        return selectRole === newRole;
      }) === -1) {
        selectedRoles.push(newRole);
      }
    });
    this.setState({
      selectedRoles
    });
    this._reloadTrees();
  }

  /**
   * Remove selected roles from selection
   */
  _removeSelectedRoles(event) {
    if (event) {
      event.preventDefault();
    }
    const { selectedRoles } = this.state;
    this.setState({
      selectedRoles: _.pullAll(selectedRoles, this.refs.selectedRoles.getWrappedInstance().getValue())
    });
    this._reloadTrees();
  }

  /**
   * Remove all roles from selection
   */
  _removeAllRoles(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      selectedRoles: []
    });
    this._reloadTrees();
  }

  /**
   * Reload both trees. Tree are not reloaded itselft after change props
   */
  _reloadTrees() {
    this.refs.roleSelect.getWrappedInstance().reload();
    this.refs.selectedRoles.getWrappedInstance().reload();
  }

  render() {
    const { identityRoles, identityRoleShowLoading, identityUsername } = this.props;
    const { selectedIdentity, roleRoots, selectedRoles } = this.state;
    const existIdentityRoles = identityRoles && identityRoles.length > 0;
    const buttonsStyle = { width: '34px', height: '34px', fontSize: '8px', marginTop: '5px' };
    return (
      <div>
        <Basic.AbstractForm
            ref="form">
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
              <Basic.DateTimePicker
                mode="date"
                ref="validFrom"
                label={this.i18n('label.validFrom')}/>
            </Basic.Col>
            <Basic.Col lg={ 6 }>
              <Basic.DateTimePicker
                mode="date"
                ref="validTill"
                label={this.i18n('label.validTill')}/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
        <Basic.Alert
          rendered={!(existIdentityRoles && selectedIdentity)}
          level="info"
          text={this.i18n('noRoles')}/>
        <Basic.Row
          rendered={existIdentityRoles && roleRoots.length > 0}>
          <Basic.Col lg={ 5 } style={{ borderRight: '1px solid #ddd', paddingRight: 0, overflowY: 'auto', maxHeight: `${TREE_COMPONENT_HEIGHT}px`, minHeight: `${TREE_COMPONENT_HEIGHT}px` }}>
            <Advanced.Tree
              showLoading={identityRoleShowLoading}
              ref="roleSelect"
              uiKey="roles-tree"
              manager={this.roleManager}
              roots={roleRoots}
              multiSelect
              traverse={false}
              nodeIconClassName={null}
              showRefreshButton={false}
              header={this.i18n('roleSelect', {'username': this.identityManger.getNiceLabel(selectedIdentity)})}/>
          </Basic.Col>

          <Basic.Col lg={ 2 }
            className="text-center"
            style={{ marginTop: '100px' }}>
            <Basic.Col lg={ 12 }>
              <Basic.Button
                onClick={this._addAllRoles.bind(this)}
                title={this.i18n('buttons.addAllRoles')}
                titleDelayShow="0"
                ref="addAll"
                style={buttonsStyle}>
                <Basic.Icon icon="fa:chevron-right"/>
                <Basic.Icon icon="fa:chevron-right"/>
              </Basic.Button>
            </Basic.Col>
            <Basic.Col lg={ 12 }>
              <Basic.Button
                style={buttonsStyle}
                titleDelayShow="0"
                title={this.i18n('buttons.addSelectedRoles')}
                onClick={this._addSelectedRoles.bind(this)}
                ref="addSelected">
                <Basic.Icon icon="fa:chevron-right"/>
              </Basic.Button>
            </Basic.Col>
            <Basic.Col lg={ 12 }>
              <Basic.Button
                style={buttonsStyle}
                titleDelayShow="0"
                title={this.i18n('buttons.removeSelectedRoles')}
                onClick={this._removeSelectedRoles.bind(this)}
                ref="removeSelected">
                <Basic.Icon icon="fa:chevron-left"/>
              </Basic.Button>
            </Basic.Col>
            <Basic.Col lg={ 12 }>
              <Basic.Button
                style={buttonsStyle}
                titleDelayShow="0"
                title={this.i18n('buttons.removeAllRoles')}
                onClick={this._removeAllRoles.bind(this)}
                ref="removeAll">
                <Basic.Icon icon="fa:chevron-left"/>
                <Basic.Icon icon="fa:chevron-left"/>
              </Basic.Button>
            </Basic.Col>
          </Basic.Col>

          <Basic.Col lg={ 5 } style={{ borderLeft: '1px solid #ddd', paddingLeft: 0, overflowY: 'auto', minHeight: `${TREE_COMPONENT_HEIGHT}px`, maxHeight: `${TREE_COMPONENT_HEIGHT}px` }}>
            <Advanced.Tree
              showLoading={identityRoleShowLoading}
              ref="selectedRoles"
              uiKey="selected-roles-tree"
              manager={this.roleManager}
              roots={ selectedRoles }
              multiSelect
              traverse={false}
              nodeIconClassName={null}
              showRefreshButton={false}
              noData={this.i18n('noSelectedRoles')}
              header={this.i18n('selectedRoles')}/>
          </Basic.Col>
        </Basic.Row>
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
