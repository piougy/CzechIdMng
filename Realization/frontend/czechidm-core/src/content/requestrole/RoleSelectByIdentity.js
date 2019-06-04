import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { RoleManager, IdentityManager, IdentityRoleManager, IdentityContractManager } from '../../redux';
import ConfigLoader from '../../utils/ConfigLoader';
//
import SearchParameters from '../../domain/SearchParameters';

const identityRoleManager = new IdentityRoleManager();
const identityManager = new IdentityManager();

const TREE_COMPONENT_HEIGHT = 400;

const IDENTITY_ROLE_BY_IDENTITY_UIKEY = 'identity-role-by-identity';

/**
* Component for select roles by identity.
*
* @author Ondrej Kopr
*/
class RoleSelectByIdentity extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.roleManager = new RoleManager();
    this.identityContractManager = new IdentityContractManager();

    this.state = {
      selectedIdentity: null,
      identityRoleRoots: [],
      selectedIdentityRoles: [],
      showOnlyDirectRoles: true, // first initial value for show only directed roles
      selectedIdentityContract: null,
      environment: ConfigLoader.getConfig('role.table.filter.environment', [])
    };
  }

  getComponentKey() {
    return 'content.task.IdentityRoleConceptTable.addByIdentity';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getIdentityManager() {
    return identityManager;
  }

  getValue() {
    const { selectedIdentityRoles } = this.state;
    return selectedIdentityRoles;
  }

  focus() {
    this.refs.select.focus();
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
  _changeIdentityOrContract(identity, identityContract, showOnlyDirectRoles, environment) {
    if (identity && identity.id) {
      const identityRoleRoots = [];
      const searchParameters = identityRoleManager.getSearchParameters()
            .setFilter('identityId', identity.id)
            .setFilter('identityContractId', identityContract ? identityContract.id : null)
            .setFilter('directRole', showOnlyDirectRoles === true ? true : null) // When is filter false we want all roles
            .setFilter('roleEnvironment', environment)
            .setSize(100000);
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
          identityRoleRoots,
          selectedIdentityContract: identityContract,
          showOnlyDirectRoles
        });
      }));
    } else {
      this.setState({
        selectedIdentity: null,
        identityRoleRoots: [],
        selectedIdentityContract: identityContract,
        showOnlyDirectRoles
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
      return (
        <span>
          { `${this.roleManager.getNiceLabel(role)} - (${this.identityContractManager.getNiceLabel(identityContract, false)})` }
          <Basic.ShortText
            value={ role.description }
            maxLength={ 100 }
            style={{ display: 'block', marginLeft: 15, fontSize: '0.95em', fontStyle: 'italic' }}/>
        </span>
      );
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

  /**
   * Method catch event from checbox that hides or shows directly roles
   */
  _showDirectRoles(event) {
    if (event) {
      event.preventDefault();
    }

    const currentTargetValue = event.currentTarget.checked;
    const { selectedIdentity, selectedIdentityContract, environment } = this.state;
    this._changeIdentityOrContract(selectedIdentity, selectedIdentityContract, currentTargetValue, environment);
  }

  _selectedIdentityContract(identityContract) {
    const { selectedIdentity, showOnlyDirectRoles, environment } = this.state;
    this._changeIdentityOrContract(selectedIdentity, identityContract, showOnlyDirectRoles, environment);
  }

  _selectIdentity(identity) {
    const { showOnlyDirectRoles, environment } = this.state;
    this.refs.selectedIdentityContract.setValue(null);
    this._changeIdentityOrContract(identity, null, showOnlyDirectRoles, environment);
  }

  /**
   * Reload both trees. Tree are not reloaded itselft after change props
   */
  _reloadTrees() {
    this.refs.identityRoleSelect.getWrappedInstance().reload();
    this.refs.selectedIdentityRoles.getWrappedInstance().reload();
  }

  _onEnvironmentChange(value) {
    const codes = [];
    if (value) {
      if (_.isArray(value)) { // codelist is available - list of object
        value.forEach(v => {
          codes.push(v.value);
        });
      } else if (value.currentTarget) { // is event (~text field onchange)
        codes.push(value.currentTarget.value);
      }
    }
    //
    const { selectedIdentity, selectedIdentityContract, showOnlyDirectRoles } = this.state;
    this.setState({
      environment: codes
    }, () => {
      this._changeIdentityOrContract(selectedIdentity, selectedIdentityContract, showOnlyDirectRoles, codes);
    });
  }

  render() {
    const { identityRoles, identityRoleShowLoading, identityUsername, identity } = this.props;
    const {
      selectedIdentity,
      identityRoleRoots,
      selectedIdentityRoles,
      showOnlyDirectRoles,
      selectedIdentityContract,
      environment
    } = this.state;

    const existIdentityRoles = identityRoles && identityRoles.length > 0;
    const buttonsStyle = { width: 34, height: 34, fontSize: 8, marginTop: 5 };
    return (
      <div>
        <Basic.AbstractForm ref="form">
          <Basic.Row>
            <Basic.Col lg={ 6 }>
              <Basic.ContentHeader
                text={
                  <span>
                    { this.i18n('source.header') }
                    <small style={{ fontSize: '0.7em', marginLeft: 5 }}>
                      <Basic.ShortText text={ identityManager.getNiceLabel(selectedIdentity) } maxLength={ 30 } cutChar={ '' }/>
                    </small>
                  </span>
                }
                className="marginable"
                style={{ paddingTop: 0 }}
                icon="component:identity"/>

              <Basic.SelectBox
                ref="select"
                label={ this.i18n('selectUser.label') }
                helpBlock={ this.i18n('selectUser.help') }
                onChange={ this._selectIdentity.bind(this) }
                manager={ this.getIdentityManager() }/>

              <Basic.SelectBox
                ref="selectedIdentityContract"
                readOnly={ !selectedIdentity }
                manager={ this.identityContractManager }
                forceSearchParameters={ new SearchParameters().setFilter('identity', selectedIdentity ? selectedIdentity.username : null) }
                label={ this.i18n('selectIdentityContract.label') }
                placeholder={ this.i18n('selectIdentityContract.placeholder') }
                helpBlock={ this.i18n('selectIdentityContract.help') }
                onChange={ this._selectedIdentityContract.bind(this) }
                niceLabel={ (contract) => { return this.identityContractManager.getNiceLabel(contract, false); }}/>

              <Advanced.CodeListSelect
                code="environment"
                label={ this.i18n('entity.Role.environment.label') }
                placeholder={ this.i18n('entity.Role.environment.help') }
                multiSelect
                onChange={ this._onEnvironmentChange.bind(this) }
                value={ environment }
                helpBlock={ this.i18n('environment.help') }/>
            </Basic.Col>
            <Basic.Col lg={ 6 }>
              <Basic.ContentHeader
                text={
                  <span>
                    { this.i18n('target.header') }
                    <small style={{ fontSize: '0.7em', marginLeft: 5 }}>
                      <Basic.ShortText text={ identityManager.getNiceLabel(identity) } maxLength={ 30 } cutChar={ '' }/>
                    </small>
                  </span>
                }
                className="marginable"
                style={{ paddingTop: 0 }}
                icon="fa:arrow-right"/>

              <Basic.SelectBox
                ref="identityContract"
                manager={ this.identityContractManager }
                forceSearchParameters={ new SearchParameters().setFilter('identity', identityUsername).setFilter('validNowOrInFuture', true) }
                label={ this.i18n('targetIdentityContract.label') }
                placeholder={ this.i18n('entity.IdentityRole.identityContract.placeholder') }
                helpBlock={ this.i18n('entity.IdentityRole.identityContract.help') }
                returnProperty={ false }
                niceLabel={ (contract) => { return this.identityContractManager.getNiceLabel(contract, false); }}
                value={ selectedIdentityContract }
                required
                useFirst/>

              <Basic.DateTimePicker
                mode="date"
                ref="validFrom"
                label={ this.i18n('label.validFrom') }/>

              <Basic.DateTimePicker
                mode="date"
                ref="validTill"
                label={ this.i18n('label.validTill') }/>

              <Basic.Checkbox
                ref="copyRoleParameters"
                label={ this.i18n('copyRoleParameters.label') }
                helpBlock={ this.i18n('copyRoleParameters.help') }/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
        <Basic.Alert
          rendered={ !existIdentityRoles }
          level="info"
          text={ selectedIdentity ? this.i18n('noIdentityRoles') : this.i18n('noIdentity') }/>
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
                  header={ this.i18n('roleSelect', {'username': identityManager.getNiceLabel(selectedIdentity)}) }
                  bodyStyle={{ overflowX: 'visible' }}/>
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
                  header={ this.i18n('selectedIdentityRoles') }
                  bodyStyle={{ overflowX: 'visible' }}/>
              </div>
            </div>
          </Basic.Div>

          <Basic.Checkbox
            ref="showOnlyDirectRoles"
            value={ showOnlyDirectRoles }
            label={ this.i18n('showOnlyDirectRoles.label', { escape: false }) }
            helpBlock={ this.i18n('showOnlyDirectRoles.help', { escape: false }) }
            onChange={ this._showDirectRoles.bind(this) }/>

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

function select(state, component) {
  return {
    identity: identityManager.getEntity(state, component.identityUsername),
    identityRoles: identityRoleManager.getEntities(state, IDENTITY_ROLE_BY_IDENTITY_UIKEY),
    identityRoleShowLoading: identityRoleManager.isShowLoading(state, IDENTITY_ROLE_BY_IDENTITY_UIKEY)
  };
}

export default connect(select, null, null, { withRef: true})(RoleSelectByIdentity);
