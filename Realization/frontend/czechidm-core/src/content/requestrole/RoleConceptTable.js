import React, { PropTypes } from 'react';
import _ from 'lodash';
import moment from 'moment';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { RoleManager, RoleRequestManager, IdentityManager } from '../../redux';
import RoleSelectByIdentity from './RoleSelectByIdentity';
import RoleConceptDetail from './RoleConceptDetail';
import IncompatibleRoleWarning from '../role/IncompatibleRoleWarning';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import FormInstance from '../../domain/FormInstance';

/**
* Table for keep identity role concept. Input are all current assigned user's permissions
* Designed for use in task detail.
*/

const roleManager = new RoleManager();
const identityManager = new IdentityManager();
const roleRequestManager = new RoleRequestManager();

/**
 * @author VS
 */
export class RoleConceptTable extends Basic.AbstractContent {

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
      validationErrors: null
    };
  }

  componentDidMount() {
    super.componentDidMount();
    // We have to create concept from props here, because same instance this component
    //  could be used in past (in this case may be this.prosp and nextProps same)
    this._setConcept(this.props);
  }

  getContentKey() {
    return 'content.task.IdentityRoleConceptTable';
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps && (
      JSON.stringify(nextProps.identityRoles) !== JSON.stringify(this.props.identityRoles) ||
      JSON.stringify(nextProps.addedIdentityRoles) !== JSON.stringify(this.props.addedIdentityRoles) ||
      JSON.stringify(nextProps.removedIdentityRoles) !== JSON.stringify(this.props.removedIdentityRoles) ||
      JSON.stringify(nextProps.changedIdentityRoles) !== JSON.stringify(this.props.changedIdentityRoles)
    )) {
      this._setConcept(nextProps);
    }
  }

  /**
   * Set input arrays (with current, added, removed, changed data) to state (first do clone) and call compile conceptData
   * @param  {array}  identityRoles         Original not modified data
   * @param  {array}  addedIdentityRoles    Added data
   * @param  {array}  removedIdentityRoles  Removed data (ids)
   * @param  {array}  changedIdentityRoles} Changed data (every object in array contains only changed fields and ID)
   */
  _setConcept({ identityRoles, addedIdentityRoles, removedIdentityRoles, changedIdentityRoles}) {
    this.setState({conceptData: this._compileConceptData({ identityRoles, addedIdentityRoles, removedIdentityRoles, changedIdentityRoles})});
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
      const { identityUsername, createConceptFunc, updateConceptFunc } = this.props;

      const entity = form.getData();
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
          this.setState({
            conceptData: this._compileConceptData(this.props),
            showLoading: false
          }, () => {
            this._closeDetail();
          });
        }
      };
      //
      if (entity._added) {
        if (!entity._virtualId && !entity.id && entity.role instanceof Array) {
          for (const roleId of entity.role) {
            const uuidId = uuid.v1();
            const identityRole = _.merge({}, entity, {_virtualId: uuidId, _added: true});
            identityRole._virtualId = uuidId;
            identityRole._embedded = {};
            identityRole._embedded.identity = identityManager.getEntity(this.context.store.getState(), identityUsername);
            identityRole._embedded.role = roleManager.getEntity(this.context.store.getState(), roleId);
            createConceptFunc(identityRole, 'ADD', eavValues, cb);
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

  /**
   * Delete operation for added (delete whole object from array added entities),
   * changed (delete changes ... object from array changed entities),
   * removed (remove id from array of deleted entities)
   */
  _deleteConcept(data) {
    // let messageKey;
    // if (data._added) {
    //   messageKey = 'Added';
    // } else if (data._removed) {
    //   messageKey = 'Removed';
    // } else if (data._changed) {
    //   messageKey = 'Changed';
    // } else {
    //   this._internalDeleteConcept(data);
    //   return;
    // }
    // this.refs['confirm-delete'].show(
    //   this.i18n(`action.delete${messageKey}.message`),
    //   this.i18n(`action.delete${messageKey}.header`)
    // ).then(() => {
    // }, () => {
    //   // Rejected
    // });
    this._internalDeleteConcept(data);
  }

  _internalDeleteConcept(data) {
    const {createConceptFunc, removeConceptFunc} = this.props;

    if (data._added) {
      removeConceptFunc(data);
    } else if (data._removed) {
      removeConceptFunc(data.id, 'REMOVE');
    } else if (data._changed) {
      removeConceptFunc(data.id, 'UPDATE');
    } else {
      createConceptFunc(data, 'REMOVE');
      return;
    }
  }

  _sortRoles(one, two) {
    if (!one._embedded.role || !two._embedded.role) {
      return false;
    }
    return one._embedded.role.name.toLowerCase() > two._embedded.role.name.toLowerCase();
  }

  /**
   * Create final data for concept table by input arrays
   * @param  {array}  identityRoles         Original not modified data
   * @param  {array}  addedIdentityRoles    Added data
   * @param  {array}  removedIdentityRoles  Removed data (ids)
   * @param  {array}  changedIdentityRoles} Changed data (every object in array contains only changed fields and ID)
   * @return {array}  conceptData           Final data for concept table
   */
  _compileConceptData({ identityRoles, addedIdentityRoles, removedIdentityRoles, changedIdentityRoles}) {
    // sort added - direct - automatic - sub roles
    const directRoles = [];
    const automaticRoles = [];
    const subRoles = [];
    //
    identityRoles.forEach(identityRole => {
      if (identityRole.directRole) {
        subRoles.push(_.merge({}, identityRole));
      } else if (identityRole.automaticRole) {
        automaticRoles.push(_.merge({}, identityRole));
      } else {
        directRoles.push(_.merge({}, identityRole));
      }
    });
    directRoles.sort(this._sortRoles);
    subRoles.sort(this._sortRoles);
    automaticRoles.sort(this._sortRoles);
    //
    // fill added flag
    if (addedIdentityRoles) {
      for (const addedIdentityRole of addedIdentityRoles) {
        addedIdentityRole._added = true;
      }
    }
    const concepts = _.concat(addedIdentityRoles, directRoles, automaticRoles, subRoles);
    //
    for (const concept of concepts) {
      if (removedIdentityRoles && removedIdentityRoles.includes(concept.id)) {
        concept._removed = true;
      }
      if (changedIdentityRoles) {
        for (const changedIdentityRole of changedIdentityRoles) {
          if (changedIdentityRole.identityRole === concept.id) {
            concept._changed = true;
            concept._eav = changedIdentityRole._eav;
            for (const property in concept) {
              if (changedIdentityRole.hasOwnProperty(property) && property !== '_embedded' && property !== 'id') {
                const key = '_' + property + 'Changed';
                if (JSON.stringify(concept[property]) !== JSON.stringify(changedIdentityRole[property])) {
                  concept[key] = changedIdentityRole[property];
                }
              }
            }
          }
        }
      }
    }
    return concepts;
  }

  /**
   * Compute background color row (added, removed, changed)
   */
  _rowClass({rowIndex, data}) {
    if (data[rowIndex]._added) {
      return 'bg-success';
    }
    if (data[rowIndex]._removed) {
      return 'bg-danger';
    }
    if (data[rowIndex]._changed) {
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
    });
  }

  _hideRoleByIdentitySelect() {
    this.setState({
      showRoleByIdentitySelect: false
    });
  }

  _executeRoleRequestByIdentity(event) {
    const { reloadComponent } = this.props;
    if (event) {
      event.preventDefault();
    }
    this.setState({
      showLoading: true
    }, () => {
      const roleRequestByIdentity = this.refs.roleSelectByIdentity.getWrappedInstance().createRoleRequestByIdentity();
      this.context.store.dispatch(roleRequestManager.copyRolesByIdentity(roleRequestByIdentity, null, () => {
        // We also need fetch request for new form attributes
        this._hideRoleByIdentitySelect();
        reloadComponent();
        this.setState({
          showLoading: false
        });
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
    //
    actions.push(
      <Basic.Button
        level={'danger'}
        onClick={this._deleteConcept.bind(this, data[rowIndex])}
        className="btn-xs"
        disabled={readOnly || !manualRole}
        showLoading={showLoadingButtonRemove}
        role="group"
        title={this.i18n('button.delete')}
        titlePlacement="bottom">
        <Basic.Icon icon={notModificated ? 'trash' : 'remove'}/>
      </Basic.Button>
    );
    if (!value._removed) {
      actions.push(
        <Basic.Button
          level={'warning'}
          onClick={this._showDetail.bind(this, data[rowIndex], true, false)}
          className="btn-xs"
          disabled={readOnly || !manualRole}
          role="group"
          title={this.i18n('button.edit')}
          titlePlacement="bottom">
          <Basic.Icon icon={'edit'}/>
        </Basic.Button>
      );
    }
    return (
      <div className="btn-group" role="group">
        {actions}
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
          <Advanced.EavForm
            key={ _.uniqueId(`${rowIndex}-${value.id}`) }
            ref="eavForm"
            formInstance={ _formInstance }
            validationErrors={ formInstance.validationErrors }
            readOnly
            useDefaultValue={false}/>
        );
    }
    return (
      <Basic.Div className="abstract-form condensed" style={{minWidth: 150, padding: 0}}>
        {result}
      </Basic.Div>
    );
  }

  // To delete
  _conceptActionsCellSimple(value) {
    const {readOnly, showLoadingButtonRemove} = this.props;
    const actions = [];
    const notModificated = !(value._added || value._removed || value._changed);
    const manualRole = !value.automaticRole && !value.directRole;
    //
    actions.push(
      <Basic.Button
        level={'danger'}
        onClick={this._deleteConcept.bind(this, value)}
        className="btn-xs"
        disabled={readOnly || !manualRole}
        showLoading={showLoadingButtonRemove}
        role="group"
        title={this.i18n('button.delete')}
        titlePlacement="bottom">
        <Basic.Icon icon={notModificated ? 'trash' : 'remove'}/>
      </Basic.Button>
    );
    if (!value._removed) {
      actions.push(
        <Basic.Button
          level={'warning'}
          onClick={this._showDetail.bind(this, value, true, false)}
          className="btn-xs"
          disabled={readOnly || !manualRole}
          role="group"
          title={this.i18n('button.edit')}
          titlePlacement="bottom">
          <Basic.Icon icon={'edit'}/>
        </Basic.Button>
      );
    }
    return (
      <div className="btn-group" role="group">
        {actions}
      </div>
    );
  }

  // To delete
  generateTable(data) {
    const trs = [];
    data.forEach(concept => {
      let rowClass;
      if (concept._added) {
        rowClass = 'bg-success';
      }
      if (concept._removed) {
        rowClass = 'bg-danger';
      }
      if (concept._changed) {
        rowClass = 'bg-warning';
      }
      const row = [];
      row.push(<td><Advanced.DetailButton
        title={this.i18n('button.detail')}
        onClick={this._showDetail.bind(this, concept, !concept._removed, false)}/></td>);
      row.push(<td>{concept._embedded.role.name}</td>);
      row.push(<td>{concept._embedded.identityContract ? concept._embedded.identityContract.id : null}</td>);
      row.push(<td>{this._conceptActionsCellSimple(concept)}</td>);
      trs.push(<tr className={rowClass}>{row}</tr>);
    });

    return <table>{trs}</table>;
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

  render() {
    const {
      identityUsername,
      readOnly,
      className,
      _currentIdentityRoles,
      request
    } = this.props;
    const {
      conceptData,
      detail,
      showRoleByIdentitySelect,
      validationErrors
    } = this.state;

    const showLoading = this.props.showLoading || this.state.showLoading;

    const result = (
      <div>
        <Basic.Panel showLoading={showLoading} rendered={ request !== null && _currentIdentityRoles !== null && !detail.show && !showRoleByIdentitySelect}>
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Basic.Toolbar rendered={!detail.show && !showRoleByIdentitySelect}>
            <div className="pull-right">
              <Basic.Button
                level="success"
                className="btn-xs"
                disabled={readOnly}
                onClick={this._addConcept.bind(this)}>
                <Basic.Icon value="fa:plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
              {' '}
              <Basic.Button
                level="success"
                className="btn-xs"
                disabled={readOnly}
                onClick={this._showRoleByIdentitySelect.bind(this)}>
                <Basic.Icon value="fa:plus"/>
                {' '}
                {this.i18n('addByIdentity.header')}
              </Basic.Button>
            </div>
            <div className="clearfix"></div>
          </Basic.Toolbar>
          {/* this.generateTable(conceptData)*/}
          <Basic.Table
            rendered={!detail.show && !showRoleByIdentitySelect}
            hover={false}
            showLoading={showLoading}
            data={conceptData}
            rowClass={this._rowClass}
            className={className}
            showRowSelection={false}
            noData={this.i18n('component.basic.Table.noData')}>
            <Basic.Column
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={this.i18n('button.detail')}
                      onClick={this._showDetail.bind(this, data[rowIndex], !data[rowIndex]._removed, false)}/>
                  );
                }
              }
              sort={false}/>
            <Basic.Column
              header={ this.i18n('entity.IdentityRole.role') }
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data }) => {
                  const role = data[rowIndex]._embedded.role;
                  if (!role) {
                    return '';
                  }
                  const content = [];
                  //
                  content.push(
                    <IncompatibleRoleWarning incompatibleRoles={ this._getIncompatibleRoles(role) }/>
                  );
                  content.push(
                    <Advanced.EntityInfo
                      entityType="role"
                      entityIdentifier={ role.id }
                      entity={ role }
                      face="popover" />
                  );
                  //
                  return content;
                }
              }
              />
            <Basic.Column
              header={this.i18n('entity.ConceptRoleRequest.state')}
              rendered={request && request.state !== 'CONCEPT'}
              cell={
                ({rowIndex, data}) => {
                  return <Basic.EnumValue value={ data[rowIndex].state} enum={RoleRequestStateEnum}/>;
                }
              }/>
            <Basic.Column
              header={this.i18n('content.task.IdentityRoleConceptTable.identityRoleAttributes.header')}
              cell={
                ({rowIndex, data}) => {
                  return this._conceptAttributesCell({ rowIndex, data });
                }
              }/>
            <Basic.Column
              header={this.i18n('entity.IdentityRole.identityContract.title')}
              cell={
                ({rowIndex, data}) => {
                  const contract = data[rowIndex]._embedded.identityContract;
                  if (!contract) {
                    return '';
                  }
                  return (
                    <Advanced.IdentityContractInfo entityIdentifier={ contract.id } entity={ contract } showIdentity={ false } showIcon face="popover" />
                  );
                }
              }/>
            <Basic.Column
              header={this.i18n('entity.Role.description')}
              property="_embedded.role.description"
              rendered={false}
              />
            <Basic.Column
              property="validFrom"
              header={this.i18n('label.validFrom')}
              cell={this._conceptDateCell.bind(this)}/>
            <Basic.Column
              property="validTill"
              header={this.i18n('label.validTill')}
              cell={this._conceptDateCell.bind(this)}/>
            <Basic.Column
              property="directRole"
              header={this.i18n('entity.IdentityRole.directRole.label')}
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
            <Basic.Column
              property="automaticRole"
              header={<Basic.Cell className="column-face-bool">{this.i18n('entity.IdentityRole.automaticRole.label')}</Basic.Cell>}
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data }) => {
                  return (
                    <Basic.BooleanCell propertyValue={ data[rowIndex].automaticRole !== null } className="column-face-bool"/>
                  );
                }
              }/>
            <Basic.Column
              header={this.i18n('label.action')}
              className="action"
              cell={this._conceptActionsCell.bind(this)}/>
          </Basic.Table>
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
              onClick={ this._executeRoleRequestByIdentity.bind(this) }
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

RoleConceptTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  identityUsername: PropTypes.string.isRequired,
  className: PropTypes.string,
  request: PropTypes.object,
  identityRoles: PropTypes.array,
  addedIdentityRoles: PropTypes.array,
  changedIdentityRoles: PropTypes.array,
  removedIdentityRoles: PropTypes.array,
  removeConceptFunc: PropTypes.func,
  createConceptFunc: PropTypes.func,
  updateConceptFunc: PropTypes.func,
  conceptRoleRequestManager: PropTypes.object,
  /**
   * Loaded incompatible roles, which should be shown in role concept table as warning
   */
  _incompatibleRoles: PropTypes.array,
};

RoleConceptTable.defaultProps = {
  filterOpened: false,
  showLoading: false,
  showLoadingButtonRemove: false
};

export default RoleConceptTable;
