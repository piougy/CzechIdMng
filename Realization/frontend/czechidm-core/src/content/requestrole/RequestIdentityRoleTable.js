import React, { PropTypes } from 'react';
import _ from 'lodash';
import moment from 'moment';
import uuid from 'uuid';
import Switch from 'react-toggle-switch';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import {RequestIdentityRoleManager, RoleManager, RoleRequestManager, IdentityManager, IdentityContractManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import RoleSelectByIdentity from './RoleSelectByIdentity';
import RoleConceptDetail from './RoleConceptDetail';
import IncompatibleRoleWarning from '../role/IncompatibleRoleWarning';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import FormInstance from '../../domain/FormInstance';
import ConfigLoader from '../../utils/ConfigLoader';
import { SecurityManager } from '../../redux';


/**
* Table for keep identity role concepts.
*
* @author Vít Švanda
*/

const requestIdentityRoleManager = new RequestIdentityRoleManager();
const roleManager = new RoleManager();
const identityManager = new IdentityManager();
const roleRequestManager = new RoleRequestManager();
const identityContractManager = new IdentityContractManager();

export class RequestIdentityRoleTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      conceptData: [],
      filterOpened: this.props.filterOpened,
      showRoleByIdentitySelect: false,
      detail: {
        show: false,
        entity: {},
        add: false
      },
      filter: {
        roleEnvironment: ConfigLoader.getConfig('concept-role.table.filter.environment', [])
      },
      sortSearchParameters: new SearchParameters(), // concept data are sorted by sections (direct / automatic / sub) by default
      validationErrors: null,
      showChangesOnly: false
    };
  }

  componentDidMount() {
    super.componentDidMount();
  }

  // getContentKey() {
  //   return 'content.task.IdentityRoleConceptTable';
  // }

  getManager() {
    return requestIdentityRoleManager;
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      filter: SearchParameters.getFilterData(this.refs.filterForm)
    });
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      filter: null
    }, () => {
      this.refs.filterForm.setData({
        roleEnvironment: []
      });
    });
  }

  /**
   * Apply FE filter
   *
   * @param  {array} conceptData
   * @param  {object} filter
   * @return {array}
   */
  applyFilter(conceptData, filter) {
    if (!filter) {
      return conceptData;
    }
    const _filterSearchParameters = SearchParameters.getSearchParameters(filter);
    if (_filterSearchParameters.getFilters().size === 0) {
      return conceptData;
    }
    return conceptData
      .filter(concept => {
        if (!_filterSearchParameters.getFilters().has('roleEnvironment')) {
          return true;
        }
        const roleEnvironments = _filterSearchParameters.getFilters().get('roleEnvironment');
        if (roleEnvironments.length === 0) {
          return true;
        }
        if (!concept._embedded || !concept._embedded.role) {
          // never undefined, bud just for sure ...
          return true;
        }
        return _.includes(roleEnvironments, concept._embedded.role.environment);
      })
      .filter(concept => {
        if (!_filterSearchParameters.getFilters().has('roleId')) {
          return true;
        }
        const roleId = _filterSearchParameters.getFilters().get('roleId');
        if (!roleId) {
          return true;
        }
        return roleId === concept.role;
      })
      .filter(concept => {
        if (!_filterSearchParameters.getFilters().has('identityContractId')) {
          return true;
        }
        const identityContractId = _filterSearchParameters.getFilters().get('identityContractId');
        if (!identityContractId) {
          return true;
        }
        return identityContractId === concept.identityContract;
      });
  }

  /**
   * Apply FE sort
   *
   * @param  {array} conceptData
   * @param  {SearchParameters} sortSearchParameters [description]
   * @return {[type]}                      [description]
   */
  applySort(conceptData, sortSearchParameters) {
    if (!conceptData || !sortSearchParameters || sortSearchParameters.getSorts().size === 0) {
      return conceptData;
    }
    //
    let _conceptData = conceptData;
    sortSearchParameters.getSorts().forEach((ascending, property) => {
      _conceptData = _conceptData.sort((one, two) => {
        if (!one._embedded || !one._embedded.role
            || !two._embedded || !two._embedded.role) {
          return 0;
        }
        const roleOne = one._embedded.role;
        const roleTwo = two._embedded.role;
        //
        let result = 0;
        if (!roleOne[property] && !roleTwo[property]) {
          result = 0;
        } else if (!roleOne[property]) { // null at end in asc
          result = 1;
        } else if (!roleTwo[property]) {
          result = -1;
        } else {
          result = roleOne[property].localeCompare(roleTwo[property]);
        }
        // console.log(roleOne[property], roleTwo[property], ascending ? result : !result);
        //
        return ascending ? result : -result;
      });
    });
    //
    return _conceptData;
  }

  /**
   * Close modal dialog
   */
  _closeDetail() {
    this.setState({
      detail: {
        ... this.state.detail,
        show: false,
        add: false
      },
      validationErrors: null
    });
  }

  /**
   * Save added or changed entities to arrays and recompile concept data.
   */
  _saveConcept(requestId, data, roleRequestCb) {
    const form = this.refs.roleConceptDetail.getWrappedInstance().getForm();
    const eavForm = this.refs.roleConceptDetail.getWrappedInstance().getEavForm();
    if (!form.isFormValid()) {
      return;
    }
    if (eavForm && !eavForm.isValid()) {
      return;
    }
    //
    this.setState({
      showLoading: true
    }, () => {
      const { identityUsername, createConceptFunc, updateConceptFunc, reloadComponent } = this.props;

      const entity = form.getData();
      entity.roleRequest = requestId;
      let eavValues = null;
      if (eavForm) {
        eavValues = {values: eavForm.getValues()};
      }
      // after concept is sent to BE - hide modal
      const cb = (validatedEntity, error) => {
        if (error) {
          // TODO: only one modal is shown => one validationErrors in the state
          this.setState({
            validationErrors: error.parameters ? error.parameters.attributes : null,
            showLoading: false
          });
        } else {
          if (roleRequestCb) {
            roleRequestCb();
          }
          this.setState({
            showLoading: false
          }, () => {
            this._closeDetail();
            if (reloadComponent) {
              reloadComponent();
            }
          });
        }
      };
      //
      if (entity._added) {
        if (!entity._virtualId && !entity.id && entity.role instanceof Array) {
          let index = 0;
          for (const roleId of entity.role) {
            index++;
            const uuidId = uuid.v1();
            const identityRole = _.merge({}, entity, {_virtualId: uuidId, _added: true});
            identityRole._virtualId = uuidId;
            identityRole._embedded = {};
            identityRole._embedded.identity = identityManager.getEntity(this.context.store.getState(), identityUsername);
            identityRole._embedded.role = roleManager.getEntity(this.context.store.getState(), roleId);
            // call calback on the last entity only
            createConceptFunc(identityRole, 'ADD', eavValues, index === entity.role.length ? cb : null);
          }
        } else {
          const addedIdentityRole = this._findAddedIdentityRoleById(entity.id);
          entity._embedded = {};
          entity._embedded.identity = identityManager.getEntity(this.context.store.getState(), identityUsername);
          if (entity.role instanceof Array) {
            entity.role = entity.role[0];
          }
          entity._embedded.role = roleManager.getEntity(this.context.store.getState(), entity.role);
          if (addedIdentityRole) {
            updateConceptFunc(entity, 'ADD', eavValues, cb);
          } else {
            createConceptFunc(entity, 'ADD', eavValues, cb);
          }
        }
      } else {
        const changedIdentityRole = _.merge({}, this._findChangedIdentityRoleById(entity.id));
        let changed = false;
        const resultValidFrom = this._findChange('validFrom', entity);
        const resultValidTill = this._findChange('validTill', entity);

        if (resultValidFrom.changed) {
          changedIdentityRole.validFrom = resultValidFrom.value;
          changed = true;
        }
        if (resultValidTill.changed) {
          changedIdentityRole.validTill = resultValidTill.value;
          changed = true;
        }

        if (changed && changedIdentityRole && changedIdentityRole.id) {
          updateConceptFunc(changedIdentityRole, 'UPDATE', eavValues, cb);
        } else {
          createConceptFunc(entity, 'UPDATE', eavValues, cb);
        }
      }
    });
  }

  _findChangedIdentityRoleById(id) {
    const {changedIdentityRoles} = this.props;
    for (const changedIdentityRole of changedIdentityRoles) {
      if (changedIdentityRole.identityRole === id) {
        return changedIdentityRole;
      }
    }
    return null;
  }

  _findAddedIdentityRoleById(virtualId) {
    const {addedIdentityRoles} = this.props;
    for (const addedIdentityRole of addedIdentityRoles) {
      if (addedIdentityRole.id === virtualId) {
        return addedIdentityRole;
      }
    }
    return null;
  }

  /**
   * Find and apply changes to changedIdentityRole array
   */
  _findChange(property, entity) {
    const {conceptData} = this.state;
    const changedPropertyName = '_' + property + 'Changed';
    for (const conceptIdentityRole of conceptData) {
      if (conceptIdentityRole.id === entity.id) {
        let propertyWithNewValue = property;
        if (entity.hasOwnProperty(changedPropertyName)) {
          propertyWithNewValue = changedPropertyName;
        }
        if (entity[propertyWithNewValue] !== conceptIdentityRole[propertyWithNewValue]) {
          return {changed: true, value: entity[propertyWithNewValue]};
        }
      }
    }
    return {changed: false};
  }

  _sortRoles(one, two) {
    if (!one._embedded.role || !two._embedded.role) {
      return false;
    }
    return one._embedded.role.name.toLowerCase() > two._embedded.role.name.toLowerCase();
  }

  /**
   * Compute background color row (added, removed, changed)
   */
  _getRowClass({rowIndex, data}) {
    const operation = data[rowIndex].operation;
    if (!operation) {
      return null;
    }
    if (operation === 'ADD') {
      return 'bg-success';
    }
    if (operation === 'REMOVE') {
      return 'bg-danger';
    }
    if (operation === 'UPDATE') {
      return 'bg-warning';
    }
    return null;
  }

  /**
   * Generate date cell for concept. If is entity changed, will use 'changed' property. In title show old value.
   */
  _conceptDateCell({rowIndex, data, property}) {
    const changedProperty = '_' + property + 'Changed';
    // Load old value and add to title
    const format = this.i18n('format.date');
    const propertyOldValue = Basic.Cell.getPropertyValue(data[rowIndex], property);
    const dataOldValue = propertyOldValue ? moment(propertyOldValue).format(format) : null;
    const oldValueMessage = dataOldValue ? this.i18n('oldValue', {oldValue: dataOldValue}) : this.i18n('oldValueNotExist');
    const changedPropertyExist = data[rowIndex].hasOwnProperty(changedProperty);
    return (
      <Basic.DateCell
        className={changedPropertyExist ? 'text-danger' : ''}
        property={changedPropertyExist ? changedProperty : property}
        rowIndex={rowIndex}
        data={data}
        title={changedPropertyExist ? oldValueMessage : null}
        format={format}/>
    );
  }

  /**
   * Create new IdentityRoleConcet with virtual ID (UUID)
   */
  _addConcept() {
    const newIdentityRoleConcept = {_added: true};
    this._showDetail(newIdentityRoleConcept, true, true);
  }

  _showDetail(entity, isEdit = false, multiAdd = false) {
    this.setState({
      detail: {
        show: true,
        edit: isEdit && !entity.automaticRole && !entity.directRole,
        entity,
        add: multiAdd
      }
    });
  }

  _showRoleByIdentitySelect() {
    this.setState({
      showRoleByIdentitySelect: true
    }, () => {
      this.refs.roleSelectByIdentity.getWrappedInstance().focus();
    });
  }

  _hideRoleByIdentitySelect() {
    this.setState({
      showRoleByIdentitySelect: false
    });
  }

  _executeRoleRequestByIdentity(requestId, data, roleRequestCb) {
    const { reloadComponent } = this.props;
    this.setState({
      showLoading: true
    }, () => {
      const roleRequestByIdentity = this.refs.roleSelectByIdentity.getWrappedInstance().createRoleRequestByIdentity();
      roleRequestByIdentity.roleRequest = requestId;
      this.context.store.dispatch(roleRequestManager.copyRolesByIdentity(roleRequestByIdentity, null, () => {
        // We also need fetch request for new form attributes
        this._hideRoleByIdentitySelect();
        reloadComponent();
        this.setState({
          showLoading: false
        });
        if (roleRequestCb) {
          roleRequestCb();
        }
      }));
    });
  }

  /**
   * Generate cell with actions (buttons)
   */
  _conceptActionsCell({rowIndex, data}) {
    const {readOnly, showLoadingButtonRemove} = this.props;
    const actions = [];
    const value = data[rowIndex];
    const notModificated = !(value._added || value._removed || value._changed);
    const manualRole = !value.automaticRole && !value.directRole;
    const operation = value.operation;
    //
    actions.push(
      <Basic.Button
        level="danger"
        onClick={ this._internalDelete.bind(this, data[rowIndex]) }
        className="btn-xs"
        disabled={ readOnly || !manualRole }
        showLoading={ showLoadingButtonRemove }
        role="group"
        title={ this.i18n('button.delete') }
        titlePlacement="bottom"
        icon={ notModificated ? 'trash' : 'remove' }/>
    );
    if (operation !== 'REMOVE') {
      actions.push(
        <Basic.Button
          level="warning"
          onClick={ this._showDetail.bind(this, data[rowIndex], true, false) }
          className="btn-xs"
          disabled={ readOnly || !manualRole }
          role="group"
          title={ this.i18n('button.edit') }
          titlePlacement="bottom"
          icon="edit"/>
      );
    }
    return (
      <div className="btn-group" role="group">
        { actions }
      </div>
    );
  }

  _conceptAttributesCell({rowIndex, data}) {
    const value = data[rowIndex];
    const result = [];
    if ( value
      && value._eav
      && value._eav.length === 1
      && value._eav[0].formDefinition) {
      const formInstance = value._eav[0];
      const _formInstance = new FormInstance(formInstance.formDefinition, formInstance.values);
      result.push(
        <div onClick={ !value._removed ? this._showDetail.bind(this, value, true, false) : null }>
          <Advanced.EavForm
            key={ _.uniqueId(`${rowIndex}-${value.id}`) }
            ref="eavForm"
            formInstance={ _formInstance }
            validationErrors={ formInstance.validationErrors }
            readOnly
            useDefaultValue={ false }/>
        </div>
      );
    }
    return (
      <Basic.Div className="abstract-form condensed" style={{minWidth: 150, padding: 0}}>
        { result }
      </Basic.Div>
    );
  }

  _getIncompatibleRoles(role) {
    const { _incompatibleRoles } = this.props;
    //
    if (!_incompatibleRoles) {
      return [];
    }
    //
    return _incompatibleRoles.filter(ir => ir.directRole.id === role.id);
  }

  _filterOpen(open) {
    this.setState({
      filterOpened: open
    });
  }

  /**
   * Save concept. If request does not exist, the is created first.
   */
  _saveConceptWithRequest(event) {
    if (event) {
      event.preventDefault();
    }
    // form validation at first => prevent to create emtpy request before form is valid
    const form = this.refs.roleConceptDetail.getWrappedInstance().getForm();
    const eavForm = this.refs.roleConceptDetail.getWrappedInstance().getEavForm();
    if (!form.isFormValid()) {
      return;
    }
    if (eavForm && !eavForm.isValid()) {
      return;
    }
    //
    const { getRequest } = this.props;
    getRequest(this._saveConcept, this);
  }

  /**
   * Remove concept. If request does not exist, the is created first.
   */
  _removeConceptWithRequest(data) {
    const {getRequest} = this.props;
    getRequest(this._internalDeleteConcept, this, data);
  }

  /**
   * Execute role request by identity. If request does not exist, the is created first.
   */
  _executeRoleRequestByIdentityWithRequest(event) {
    if (event) {
      event.preventDefault();
    }
    const {getRequest} = this.props;
    getRequest(this._executeRoleRequestByIdentity, this);
  }

  /**
   * Gets duplicated concept or identity role for the given concept
   */
  _getOriginalForDuplicate(conceptData, concept) {
    if (!conceptData || !concept.duplicate || !concept._embedded || !concept._embedded.duplicates) {
      return null;
    }
    const duplicates = concept._embedded.duplicates;
    if (duplicates.identityRoles.length > 0) {
      return conceptData.find(c => {
        return c.id === duplicates.identityRoles[0];
      });
    }
    if (duplicates.concepts.length > 0) {
      return conceptData.find(c => {
        return c.id === duplicates.concepts[0];
      });
    }
    //
    return null;
  }

  _handleSort(property, order) {
    const { sortSearchParameters } = this.state;
    //
    this.setState({
      sortSearchParameters: sortSearchParameters.clearSort().setSort(property, order !== 'DESC')
    });
  }

  // NEW ...
  _toggleShowChangesOnly() {
    this.setState(prevState => {
      return {
        showChangesOnly: !prevState.showChangesOnly
      };
    });
  }

  _internalDelete(data) {
    const {request} = this.props;
    data.roleRequest = request.id;
    this.context.store.dispatch(this.getManager().deleteEntity(data, null, (json) => {
      if (!request.id) {
        this.context.router.replace(`/role-requests/${json.roleRequest}/detail`);
      } else {
        this.reload();
      }
    }));
  }

  reload() {
    this.refs.table.getWrappedInstance().reload();
  }

  render() {
    const {
      readOnly,
      request,
      identityId,
      showRowSelection
    } = this.props;
    const {
      showChangesOnly,
      conceptData,
      detail,
      showRoleByIdentitySelect,
      validationErrors,
      filterOpened,
      filter,
      sortSearchParameters
    } = this.state;

    const identityUsername = request && request.applicant;
    let forceSearchParameters = new SearchParameters();
    if (request) {
      forceSearchParameters = forceSearchParameters.setFilter('roleRequestId', request.id);
    }
    if (identityId) {
      forceSearchParameters = forceSearchParameters.setFilter('identityId', identityId);
    }
    forceSearchParameters = forceSearchParameters.setFilter('onlyChanges', showChangesOnly);
    //
    const showLoading = this.props.showLoading || this.state.showLoading;
    const contractForceSearchparameters = new SearchParameters().setFilter('identity', identityUsername);
    //
    const result = (
      <div>
        <Basic.Panel rendered={ request !== null} className={ detail.show || showRoleByIdentitySelect ? 'hidden' : '' }>
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Basic.Toolbar>
            <div>
              <div className="pull-left" style={{display: 'flex', alignItems: 'middle'}}>
                  <Switch onClick={this._toggleShowChangesOnly.bind(this)} on={showChangesOnly}/>
                  <span style={{marginLeft: '5px', marginTop: '3px', fontSize: '16px'}}>Show changes only</span>
              </div>
              <div className="pull-right">
                <Basic.Button
                  level="success"
                  className="btn-xs"
                  disabled={readOnly}
                  onClick={this._addConcept.bind(this)}
                  icon="fa:plus"
                  text={ this.i18n('button.add') }/>
                <Basic.Button
                  level="success"
                  className="btn-xs"
                  disabled={ readOnly }
                  onClick={ this._showRoleByIdentitySelect.bind(this) }
                  icon="fa:plus"
                  text={ this.i18n('addByIdentity.header') }
                  style={{ marginLeft: 3 }}/>
                <Advanced.Filter.ToogleButton
                  filterOpen={ this._filterOpen.bind(this) }
                  filterOpened={ filterOpened }
                  style={{ marginLeft: 3 }}
                  searchParameters={ SearchParameters.getSearchParameters(filter) }/>
              </div>
              <div className="clearfix"></div>
            </div>
            <Basic.Collapse in={ filterOpened }>
              <div>
                <Basic.Div className="advanced-filter">
                  <Basic.AbstractForm ref="filterForm" data={ filter }>
                    <Basic.Row className="last">
                      <Basic.Col lg={ 3 }>
                        <Advanced.Filter.RoleSelect
                          ref="roleId"
                          label={ null }
                          placeholder={ this.i18n('content.identity.roles.filter.role.placeholder') }
                          header={ this.i18n('content.identity.roles.filter.role.placeholder') }/>
                      </Basic.Col>
                      <Basic.Col lg={ 3 }>
                        <Advanced.CodeListSelect
                          ref="roleEnvironment"
                          code="environment"
                          label={ null }
                          placeholder={ this.i18n('entity.Role.environment.label') }
                          multiSelect/>
                      </Basic.Col>
                      <Basic.Col lg={ 3 }>
                        <Basic.Div>
                          <Advanced.Filter.SelectBox
                            ref="identityContractId"
                            placeholder={ this.i18n('entity.IdentityRole.identityContract.title') }
                            manager={ identityContractManager }
                            forceSearchParameters={ contractForceSearchparameters }
                            niceLabel={ (entity) => identityContractManager.getNiceLabel(entity, false) }/>
                        </Basic.Div>
                      </Basic.Col>
                      <Basic.Col lg={ 3 } className="text-right">
                        <Basic.Button onClick={ this.cancelFilter.bind(this) } style={{ marginRight: 5 }}>
                          { this.i18n('button.filter.cancel') }
                        </Basic.Button>
                        <Basic.Button level="primary" onClick={ this.useFilter.bind(this) } >
                          { this.i18n('button.filter.use') }
                        </Basic.Button>
                      </Basic.Col>
                    </Basic.Row>
                  </Basic.AbstractForm>
                </Basic.Div>
              </div>
            </Basic.Collapse>
          </Basic.Toolbar>
          <Advanced.Table
            ref="table"
            uiKey="request-identity-role-table"
            manager={requestIdentityRoleManager}
            showRowSelection={ showRowSelection }
            actions={
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'),
                 action: this.onDelete.bind(this), disabled: false }]
            }
            rowClass={this._getRowClass}
            forceSearchParameters={forceSearchParameters}>
            <Advanced.Column
              header={ this.i18n('entity.IdentityRole.role') }
              title={ this.i18n('entity.Role.name') }
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data }) => {
                  const role = data[rowIndex]._embedded.role;
                  if (!role) {
                    return '';
                  }
                  return (
                    <Advanced.EntityInfo
                        entityType="role"
                        entityIdentifier={ role.id }
                        entity={ role }
                        face="popover"
                        showIcon
                        showCode={ false }
                        showEnvironment={ false }/>
                  );
                }
              }/>
            <Advanced.Column
              property="_embedded.role.baseCode"
              face="text"
              header={ this.i18n('entity.Role.baseCode.label') }
              sort/>
            <Advanced.Column
              property="_embedded.role.environment"
              face="text"
              header={ this.i18n('entity.Role.environment.label') }
              sort/>
            <Advanced.Column
              header={this.i18n('content.task.IdentityRoleConceptTable.identityRoleAttributes.header')}
              cell={
                ({rowIndex, data}) => {
                  return this._conceptAttributesCell({ rowIndex, data });
                }
              }/>
            <Advanced.Column
              header={ this.i18n('entity.IdentityRole.identityContract.title') }
              cell={
                ({rowIndex, data}) => {
                  const contract = data[rowIndex]._embedded.identityContract;
                  if (!contract) {
                    return '';
                  }
                  return (
                    <Advanced.IdentityContractInfo
                      entityIdentifier={ contract.id }
                      entity={ contract }
                      showIdentity={ false }
                      showIcon
                      face="popover" />
                  );
                }
              }/>
            <Advanced.Column property="validFrom" face="date" header={this.i18n('entity.ConceptRoleRequest.validFrom')} sort/>
            <Advanced.Column property="validTill" face="date" header={this.i18n('entity.ConceptRoleRequest.validTill')} sort/>
            <Advanced.Column
              property="directRole"
              header={ this.i18n('entity.IdentityRole.directRole.label') }
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data, property }) => {
                  if (!data[rowIndex][property]) {
                    return null;
                  }
                  //
                  return (
                    <Advanced.EntityInfo
                      entityType="identityRole"
                      entityIdentifier={ data[rowIndex][property] }
                      entity={ data[rowIndex]._embedded[property] }
                      showIdentity={ false }
                      face="popover" />
                  );
                }
              }
              width={ 150 }/>
            <Advanced.Column
              property="automaticRole"
              header={ <Basic.Icon value="component:automatic-role" title={ this.i18n('entity.IdentityRole.automaticRole.help') }/> }
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data }) => {
                  return (
                    <Basic.BooleanCell propertyValue={ data[rowIndex].automaticRole !== null } className="column-face-bool"/>
                  );
                }
              }/>
            <Advanced.Column
                header={ this.i18n('label.action') }
                className="action"
                cell={ this._conceptActionsCell.bind(this) }/>
          </Advanced.Table>
        </Basic.Panel>
        <Basic.Modal
          bsSize="large"
          show={showRoleByIdentitySelect}
          onHide={ this._hideRoleByIdentitySelect.bind(this) }
          backdrop="static"
          keyboard={!showLoading}>
          <Basic.Modal.Header
            closeButton={ !showLoading }
            text={ this.i18n('create.headerByIdentity') }
            rendered={ Utils.Entity.isNew(detail.entity) }/>
          <Basic.Modal.Body>
            <RoleSelectByIdentity
              ref="roleSelectByIdentity"
              showLoading={ showLoading }
              identityUsername={identityUsername}
              request={request}/>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              showLoading={ showLoading }
              onClick={ this._hideRoleByIdentitySelect.bind(this) }>
              { this.i18n('button.close') }
            </Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoading={ showLoading }
              onClick={ this._executeRoleRequestByIdentityWithRequest.bind(this) }
              showLoadingIcon>
              { this.i18n('button.set') }
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={ this._closeDetail.bind(this) }
          backdrop="static"
          keyboard={!showLoading}>

          <form onSubmit={ this._saveConceptWithRequest.bind(this) }>
            <Basic.Modal.Header
              closeButton={ !showLoading }
              text={ this.i18n('create.header') }
              rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              closeButton={ !showLoading }
              text={ this.i18n('edit.header', { role: detail.entity.role }) }
              rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <RoleConceptDetail
                ref="roleConceptDetail"
                identityUsername={identityUsername}
                showLoading={showLoading}
                readOnly={readOnly}
                entity={detail.entity}
                isEdit={detail.edit}
                multiAdd={detail.add}
                validationErrors={ validationErrors }/>
            </Basic.Modal.Body>
            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this._closeDetail.bind(this) }
                showLoading={ showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ showLoading }
                showLoadingIcon
                rendered={ detail.edit && !readOnly }>
                { this.i18n('button.set') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );

    return result;
  }
}

RequestIdentityRoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  identityId: PropTypes.string.isRequired,
  className: PropTypes.string,
  request: PropTypes.object,
  showRowSelection: PropTypes.bool,
  readOnly: PropTypes.bool
};

RequestIdentityRoleTable.defaultProps = {
  filterOpened: false,
  showLoading: false,
  showRowSelection: true
};

export default RequestIdentityRoleTable;
