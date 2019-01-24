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
      identityRoleRoots: [],
      selectedIdentityRoles: []
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
    const { selectedIdentityRoles } = this.state;
    return selectedIdentityRoles;
  }

  /**
   * Create role request that conrespond with roleRequestByIdentity on backend.
   * Whole request can be send to backend into rest.
   */
  createRoleRequestByIdentity() {
    const { request } = this.props;
    const { selectedIdentityRoles } = this.state;
    const identityContract = this.refs.identityContract.getValue();

    const roleRequestByIdentity = {
      roleRequest: request.id,
      identityRoles: selectedIdentityRoles,
      identityContract: identityContract.id,
      copyRoleParameters: this.refs.copyRoleParameters.getValue(),
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
   * Set change of identity or identity contract
   */
  _changeIdentityOrContract(identity, identityContract = null) {
    if (identity && identity.id) {
      const identityRoleRoots = [];
      const searchParameters = identityRoleManager.getSearchParameters().setFilter('identityId', identity.id).setFilter('identityContractId', identityContract ? identityContract.id : null).setSize(100000);
      this.context.store.dispatch(identityRoleManager.fetchEntities(searchParameters, IDENTITY_ROLE_BY_IDENTITY_UIKEY, json => {
        // Returned json and inner embbeded with identity roles must exists
        if (json && json._embedded && json._embedded.identityRoles) {
          const identityRoles = json._embedded.identityRoles;
          // Iterate over all identity roles
          for (const index in identityRoles) {
            if (identityRoles.hasOwnProperty(index)) {
              const identityRole = identityRoles[index];
              if (identityRole) {
                identityRoleRoots.push(identityRole);
              }
            }
          }
        }
        this.setState({
          selectedIdentity: identity,
          identityRoleRoots
        });
      }));
    } else {
      this.setState({
        selectedIdentity: null,
        identityRoleRoots: []
      });
    }
  }

  /**
   * Add all identity roles into selection
   */
  _addAllIdentityRoles(event) {
    if (event) {
      event.preventDefault();
    }
    const { selectedIdentityRoles, identityRoleRoots } = this.state;

    // In add all case is mandatory check id, because roleRoots is object.
    identityRoleRoots.forEach(newIdentityRole => {
      if (_.findIndex(selectedIdentityRoles, (selectIdentityRole) => {
        return selectIdentityRole === newIdentityRole.id;
      }) === -1) {
        selectedIdentityRoles.push(newIdentityRole.id);
      }
    });
    this.setState({
      selectedIdentityRoles
    });
    this._reloadTrees();
  }

  /**
   * Add only selected identity roles to selection
   */
  _addSelectedIdentityRoles(event) {
    if (event) {
      event.preventDefault();
    }

    const { selectedIdentityRoles } = this.state;
    const newIdentityRoles = this.refs.identityRoleSelect.getWrappedInstance().getValue();
    newIdentityRoles.forEach(newIdentityRole => {
      if (_.findIndex(selectedIdentityRoles, (selectIdentityRole) => {
        return selectIdentityRole === newIdentityRole;
      }) === -1) {
        selectedIdentityRoles.push(newIdentityRole);
      }
    });
    this.setState({
      selectedIdentityRoles
    });
    this._reloadTrees();
  }

  /**
   * Remove selected identity roles from selection
   */
  _removeSelectedIdentityRoles(event) {
    if (event) {
      event.preventDefault();
    }
    const { selectedIdentityRoles } = this.state;
    this.setState({
      selectedIdentityRoles: _.pullAll(selectedIdentityRoles, this.refs.selectedIdentityRoles.getWrappedInstance().getValue())
    });
    this._reloadTrees();
  }

  /**
   * Remove all identity roles from selection
   */
  _removeAllIdentityRoles(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      selectedIdentityRoles: []
    });
    this._reloadTrees();
  }

  _identityRoleNiceLabel(identityRole) {
    if (identityRole && identityRole._embedded) {
      const role = identityRole._embedded.role;
      const identityContract = identityRole._embedded.identityContract;
      return `${this.roleManager.getNiceLabel(role)} - (${this.identityContractManager.getNiceLabel(identityContract, false)})`;
    }
  }

  _identityRoleIcon(value) {
    const identityRole = value.node;
    if (identityRole) {
      if (identityRole.automaticRole) {
        return 'component:automatic-role';
      } else if (identityRole.directRole) {
        return 'component:sub-role';
      } else if (identityRole._embedded.role.childrenCount > 0) {
        return 'component:business-role';
      }
      return 'component:role';
    }
  }

  _selectedIdentityContract(identityContract) {
    const { selectedIdentity } = this.state;
    this._changeIdentityOrContract(selectedIdentity, identityContract);
  }

  _selectIdentity(identity) {
    this._changeIdentityOrContract(identity, null);
  }

  /**
   * Reload both trees. Tree are not reloaded itselft after change props
   */
  _reloadTrees() {
    this.refs.identityRoleSelect.getWrappedInstance().reload();
    this.refs.selectedIdentityRoles.getWrappedInstance().reload();
  }

  render() {
    const { identityRoles, identityRoleShowLoading, identityUsername } = this.props;
    const { selectedIdentity, identityRoleRoots, selectedIdentityRoles } = this.state;
    const existIdentityRoles = identityRoles && identityRoles.length > 0;
    const buttonsStyle = { width: '34px', height: '34px', fontSize: '8px', marginTop: '5px' };
    return (
      <div>
        <Basic.AbstractForm
            ref="form">
          <Basic.SelectBox
            ref="select"
            label={ this.i18n('selectUser.label') }
            helpBlock={ this.i18n('selectUser.help') }
            onChange={ this._selectIdentity.bind(this) }
            manager={ this.getIdentityManager() }/>
          <Basic.SelectBox
            ref="selectedIdentityContract"
            readOnly={!selectedIdentity}
            manager={ this.identityContractManager }
            forceSearchParameters={ new SearchParameters().setFilter('identity', selectedIdentity ? selectedIdentity.username : null) }
            label={ this.i18n('selectIdentityContract.label') }
            placeholder={ this.i18n('selectIdentityContract.placeholder') }
            helpBlock={ this.i18n('selectIdentityContract.help') }
            onChange={ this._selectedIdentityContract.bind(this) }
            niceLabel={ (contract) => { return this.identityContractManager.getNiceLabel(contract, false); }}/>
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
                label={ this.i18n('label.validFrom') }/>
            </Basic.Col>
            <Basic.Col lg={ 6 }>
              <Basic.DateTimePicker
                mode="date"
                ref="validTill"
                label={ this.i18n('label.validTill') }/>
            </Basic.Col>
          </Basic.Row>
          <Basic.Checkbox
            ref="copyRoleParameters"
            label={ this.i18n('copyRoleParameters.label') }
            helpBlock={ this.i18n('copyRoleParameters.help') }/>
        </Basic.AbstractForm>
        <Basic.Alert
          rendered={!(existIdentityRoles && selectedIdentity)}
          level="info"
          text={this.i18n('noIdentityRoles')}/>
        <Basic.Div rendered={ existIdentityRoles && identityRoleRoots.length > 0 }>
          <Basic.Div style={{ marginLeft: -15, marginRight: -15 }}>
            <Basic.ContentHeader text={ this.i18n('roleSelectionHeader') } style={{ paddingRight: 15, paddingLeft: 15, marginBottom: 0 }}/>
            <div style={{ display: 'flex' }}>
              <div style={{ flex: 1, borderRight: '1px solid #ddd', overflowY: 'auto', maxHeight: TREE_COMPONENT_HEIGHT, minHeight: TREE_COMPONENT_HEIGHT }}>
                <Advanced.Tree
                  showLoading={ identityRoleShowLoading }
                  ref="identityRoleSelect"
                  uiKey="roles-tree"
                  manager={ identityRoleManager }
                  roots={ identityRoleRoots }
                  multiSelect
                  traverse
                  showRefreshButton={false}
                  nodeNiceLabel={ this._identityRoleNiceLabel.bind(this) }
                  nodeIcon={ this._identityRoleIcon.bind(this)}
                  nodeIconClassName={ null }
                  header={ this.i18n('roleSelect', {'username': this.identityManger.getNiceLabel(selectedIdentity)}) }/>
              </div>

              <div
                className="text-center"
                style={{ marginTop: 100, width: 50 }}>
                <Basic.Row>
                  <Basic.Button
                    onClick={ this._addAllIdentityRoles.bind(this) }
                    title={ this.i18n('buttons.addAllIdentityRoles') }
                    titleDelayShow="0"
                    ref="addAll"
                    level="success"
                    style={ buttonsStyle }>
                    <Basic.Icon icon="fa:chevron-right"/>
                    <Basic.Icon icon="fa:chevron-right"/>
                  </Basic.Button>
                </Basic.Row>
                <Basic.Row>
                  <Basic.Button
                    style={ buttonsStyle }
                    level="success"
                    titleDelayShow="0"
                    title={ this.i18n('buttons.addSelectedIdentityRoles') }
                    onClick={ this._addSelectedIdentityRoles.bind(this) }
                    ref="addSelected">
                    <Basic.Icon icon="fa:chevron-right"/>
                  </Basic.Button>
                </Basic.Row>
                <Basic.Row>
                  <Basic.Button
                    style={ buttonsStyle }
                    level="danger"
                    titleDelayShow="0"
                    title={ this.i18n('buttons.removeSelectedIdentityRoles') }
                    onClick={ this._removeSelectedIdentityRoles.bind(this) }
                    ref="removeSelected">
                    <Basic.Icon icon="fa:chevron-left"/>
                  </Basic.Button>
                </Basic.Row>
                <Basic.Row>
                  <Basic.Button
                    style={ buttonsStyle }
                    level="danger"
                    titleDelayShow="0"
                    title={ this.i18n('buttons.removeAllIdentityRoles') }
                    onClick={ this._removeAllIdentityRoles.bind(this) }
                    ref="removeAll">
                    <Basic.Icon icon="fa:chevron-left"/>
                    <Basic.Icon icon="fa:chevron-left"/>
                  </Basic.Button>
                </Basic.Row>
              </div>

              <div style={{ flex: 1, borderLeft: '1px solid #ddd', overflowY: 'auto', minHeight: TREE_COMPONENT_HEIGHT, maxHeight: TREE_COMPONENT_HEIGHT }}>
                <Advanced.Tree
                  showLoading={ identityRoleShowLoading }
                  ref="selectedIdentityRoles"
                  uiKey="selected-identity-roles-tree"
                  manager={ identityRoleManager }
                  roots={ selectedIdentityRoles }
                  multiSelect
                  traverse
                  showRefreshButton={ false }
                  noData={ this.i18n('noSelectedIdentityRoles') }
                  nodeNiceLabel={ this._identityRoleNiceLabel.bind(this) }
                  nodeIcon={ this._identityRoleIcon.bind(this) }
                  nodeIconClassName={ null }
                  header={ this.i18n('selectedIdentityRoles') }/>
              </div>
            </div>
          </Basic.Div>

          <Basic.Alert level="info" style={{ marginTop: 15 }}>
            <i>{ this.i18n('legend.header') }:</i>
            <div><Basic.Icon value="component:role"/> { this.i18n('legend.role-types.role') }</div>
            <div><Basic.Icon value="component:business-role"/> { this.i18n('legend.role-types.business-role') }</div>
            <div><Basic.Icon value="component:automatic-role"/> { this.i18n('legend.role-types.automatic-role') }</div>
            <div><Basic.Icon value="component:sub-role"/> { this.i18n('legend.role-types.sub-role') }</div>
          </Basic.Alert>
        </Basic.Div>
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
