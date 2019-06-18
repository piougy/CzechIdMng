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

  getContentKey() {
    return 'content.task.IdentityRoleConceptTable';
  }

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
  _saveConcept(event) {
    if (event) {
      event.preventDefault();
    }

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
      const { request} = this.props;

      const entity = form.getData();
      entity.roleRequest = request.id;
      let eavValues = null;
      if (eavForm) {
        eavValues = {values: eavForm.getValues()};
      }

      // Conversions
      if ( entity.identityContract && _.isObject(entity.identityContract)) {
        entity.identityContract = entity.identityContract.id;
      }
      if ( entity.role && _.isArray(entity.role)) {
        entity.roles = entity.role;
        entity.role = null;
      }
      // Add EAV to entity
      entity._eav = [eavValues];
      // Save entity
      this.context.store.dispatch(requestIdentityRoleManager.createEntity(entity, null, (createdEntity, error) => {
        if (error) {
          this.setState({
            validationErrors: error.parameters ? error.parameters.attributes : null,
            showLoading: false
          });
        } else {
          this.setState({
            showLoading: false
          }, () => {
            if (!request.id) {
              this.context.router.replace(`/role-requests/${createdEntity.roleRequest}/detail`);
            } else {
              this._closeDetail();
              this.reload();
            }
          });
        }
      }));
    });
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
   * Create new IdentityRoleConcet with virtual ID (UUID)
   */
  _addConcept() {
    const newIdentityRoleConcept = {operation: 'ADD'};
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
   * Execute role request by identity. If request does not exist, the is created first.
   */
  _executeRoleRequestByIdentityWithRequest(event) {
    if (event) {
      event.preventDefault();
    }
    const {getRequest} = this.props;
    getRequest(this._executeRoleRequestByIdentity, this);
  }

  _toggleShowChangesOnly() {
    this.setState(prevState => {
      return {
        showChangesOnly: !prevState.showChangesOnly
      };
    });
  }

  _internalDelete(data) {
    const {request} = this.props;
    this.setState({showLoadingActions: true}, () => {
      data.roleRequest = request.id;
      this.context.store.dispatch(this.getManager().deleteEntity(data, null, (json) => {
        this.setState({showLoadingActions: false});
        if (!request.id) {
          this.context.router.replace(`/role-requests/${json.roleRequest}/detail`);
        } else {
          this.reload();
        }
      }));
    });
  }

  beforeDelete(bulkActionValue, selectedEntities) {
    const {request} = this.props;

    for (const entity of selectedEntities) {
      entity.roleRequest = request.id;
    }
  }

  afterDelete(entities) {
    const {request} = this.props;

    if (!request.id && entities && entities.length > 0) {
      this.context.router.replace(`/role-requests/${entities[0].roleRequest}/detail`);
    } else {
      this.reload();
    }
  }

  reload() {
    this.refs.table.getWrappedInstance().reload();
  }

  _handleSort(property, order) {
    const { sortSearchParameters } = this.state;
    //
    console.log("_handleSort", property, order, sortSearchParameters.clearSort().setSort(property, order !== 'DESC'));
    this.setState({
      sortSearchParameters: sortSearchParameters.clearSort().setSort(property, order !== 'DESC')
    });
  }

  /**
   * Generate cell with actions (buttons)
   */
  renderConceptActionsCell({rowIndex, data}) {
    const {readOnly} = this.props;
    const {showLoadingActions} = this.state;

    const actions = [];
    const value = data[rowIndex];
    const manualRole = !value.automaticRole && !value.directRole;
    const operation = value.operation;
    //
    actions.push(
      <Basic.Button
        level="danger"
        onClick={ this._internalDelete.bind(this, data[rowIndex]) }
        className="btn-xs"
        disabled={ readOnly || !manualRole }
        showLoading={ showLoadingActions }
        role="group"
        title={ this.i18n('button.delete') }
        titlePlacement="bottom"
        icon={'trash'}/>
    );
    if (operation !== 'REMOVE') {
      actions.push(
        <Basic.Button
          level="warning"
          showLoading={ showLoadingActions }
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

  renderConceptAttributesCell({rowIndex, data}) {
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
    console.log("sort", sortSearchParameters);

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
        <Basic.Panel rendered={ request !== null}>
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
            defaultSearchParameters={ sortSearchParameters }
            forceSearchParameters={forceSearchParameters}>
            <Advanced.Column
              title={ this.i18n('entity.Role.name') }
              header={
                <Basic.BasicTable.SortHeaderCell
                  header={ this.i18n('entity.IdentityRole.role') }
                  title={ this.i18n('entity.Role.name') }
                  sortHandler={ this._handleSort.bind(this) }
                  sortProperty="name"
                  searchParameters={ sortSearchParameters }/>
              }
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
                  return this.renderConceptAttributesCell({ rowIndex, data });
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
                cell={ this.renderConceptActionsCell.bind(this) }/>
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

          <form onSubmit={ this._saveConcept.bind(this) }>
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
  showRowSelection: true,
  readOnly: false
};

export default RequestIdentityRoleTable;
