

import React, { PropTypes } from 'react';
import _ from 'lodash';
import moment from 'moment';
import uuid from 'uuid';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import {RoleManager, IdentityManager} from '../../../redux';

/**
* Table for keep identity role concept. Input are all current assigned user's permissions
* Designed for use in task detail.
*/

const roleManager = new RoleManager();
const identityManager = new IdentityManager();

export class IdentityRoleConceptTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      conceptData: {},
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {},
        add: false
      }
    };
  }

  componentDidMount() {
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

  getAddedIdentityRoles() {
    return this.state.addedIdentityRoles;
  }

  getRemovedIdentityRolesIds() {
    return this.state.removedIdentityRoles;
  }

  getChangedIdentityRoles() {
    return this.state.changedIdentityRoles;
  }

  /**
   * Set input arrays (with current, added, removed, changed data) to state (first do clone) and call compile conceptData
   * @param  {array}  identityRoles         Original not modified data
   * @param  {array}  addedIdentityRoles    Added data
   * @param  {array}  removedIdentityRoles  Removed data (ids)
   * @param  {array}  changedIdentityRoles} Changed data (every object in array contains only changed fields and ID)
   */
  _setConcept({ identityRoles, addedIdentityRoles, removedIdentityRoles, changedIdentityRoles}) {
    this.setState({identityRoles: _.merge([], identityRoles),
       addedIdentityRoles: _.merge([], addedIdentityRoles), removedIdentityRoles: _.merge([], removedIdentityRoles),
       changedIdentityRoles: _.merge([], changedIdentityRoles)}, ()=>{
      this.setState({conceptData: this._compileConceptData(this.state)});
    });
  }

  /**
   * Show modal dialog
   * @param  {Object}  entity           Entity show in dialog
   * @param  {Boolean} isEdit = false   If is false then form in dialog will be read only
   */
  _showDetail(entity, isEdit = false, multiAdd = false) {
    const entityFormData = _.merge({}, entity, {
      role: entity._embedded && entity._embedded.role ? entity._embedded.role.name : null
    });

    this.setState({
      detail: {
        show: true,
        edit: isEdit,
        entity: entityFormData,
        add: multiAdd
      }
    }, () => {
      this.refs.form.setData(entityFormData);
      this.refs.role.focus();
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
      }
    });
  }

  /**
   * Save added or changed entities to arrays and recompile concept data.
   */
  _saveConcept(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const {addedIdentityRoles} = this.state;
    const {identityUsername} = this.props;

    const entity = this.refs.form.getData();

    if (entity._added) {
      if (!entity._virtualId && entity.role instanceof Array) {
        for (const roleId of entity.role) {
          const uuidId = uuid.v1();
          const identityRole = _.merge({}, entity, {_virtualId: uuidId, _added: true});
          identityRole._virtualId = uuidId;
          identityRole._embedded = {};
          identityRole._embedded.identity = identityManager.getEntity(this.context.store.getState(), identityUsername);
          identityRole._embedded.role = roleManager.getEntity(this.context.store.getState(), roleId);
          addedIdentityRoles.push(identityRole);
        }
      } else {
        const addedIdentityRole = this._findAddedIdentityRoleByVirtualId(entity._virtualId);
        entity._embedded = {};
        entity._embedded.identity = identityManager.getEntity(this.context.store.getState(), identityUsername);
        entity._embedded.role = roleManager.getEntity(this.context.store.getState(), entity.role);
        if (addedIdentityRole) {
          _.merge(addedIdentityRole, entity);
        } else {
          addedIdentityRoles.push(entity);
        }
      }
    } else {
      this._findChange('validFrom', entity);
      this._findChange('validTill', entity);
    }
    this.setState({conceptData: this._compileConceptData(this.state)});
    this._closeDetail();
  }

  _findChangedIdentityRoleById(id) {
    const {changedIdentityRoles} = this.state;
    for (const changedIdentityRole of changedIdentityRoles) {
      if (changedIdentityRole.id === id) {
        return changedIdentityRole;
      }
    }
    return null;
  }

  _findAddedIdentityRoleByVirtualId(virtualId) {
    const {addedIdentityRoles} = this.state;
    for (const addedIdentityRole of addedIdentityRoles) {
      if (addedIdentityRole._virtualId === virtualId) {
        return addedIdentityRole;
      }
    }
    return null;
  }

  /**
   * Find and apply changes to changedIdentityRole array
   */
  _findChange(property, entity) {
    const {conceptData, changedIdentityRoles} = this.state;
    const changedPropertyName = '_' + property + 'Changed';
    for (const conceptIdentityRole of conceptData) {
      if (conceptIdentityRole.id === entity.id) {
        const changedIdentityRole = this._findChangedIdentityRoleById(entity.id);
        let propertyWithNewValue = property;
        if (entity.hasOwnProperty(changedPropertyName)) {
          propertyWithNewValue = changedPropertyName;
        }
        if (entity[propertyWithNewValue] !== conceptIdentityRole[propertyWithNewValue]) {
          if (changedIdentityRole) {
            changedIdentityRole[property] = entity[propertyWithNewValue];
          } else {
            changedIdentityRoles.push({id: entity.id, [property]: entity[propertyWithNewValue]});
          }
        }
      }
    }
  }

  /**
   * Delete operation for added (delete whole object from array added entities),
   * changed (delete changes ... object from array changed entities),
   * removed (remove id from array of deleted entities)
   */
  _deleteConcept(data) {
    let messageKey;
    if (data._added) {
      messageKey = 'Added';
    } else if (data._removed) {
      messageKey = 'Removed';
    } else if (data._changed) {
      messageKey = 'Changed';
    } else {
      messageKey = '';
    }
    this.refs['confirm-delete'].show(
      this.i18n(`action.delete${messageKey}.message`),
      this.i18n(`action.delete${messageKey}.header`)
    ).then(() => {
      const {addedIdentityRoles, removedIdentityRoles, changedIdentityRoles} = this.state;
      if (data._added) {
        for (const addedIdentityRole of addedIdentityRoles) {
          if (addedIdentityRole === data) {
            addedIdentityRoles.splice(addedIdentityRoles.indexOf(addedIdentityRole), 1);
            this.setState({conceptData: this._compileConceptData(this.state)});
            return;
          }
        }
      } else if (data._removed) {
        for (const removedIdentityRole of removedIdentityRoles) {
          if (removedIdentityRole === data.id) {
            removedIdentityRoles.splice(removedIdentityRoles.indexOf(removedIdentityRole), 1);
            this.setState({conceptData: this._compileConceptData(this.state)});
            return;
          }
        }
      } else if (data._changed) {
        for (const changedIdentityRole of changedIdentityRoles) {
          if (changedIdentityRole.id === data.id) {
            changedIdentityRoles.splice(changedIdentityRoles.indexOf(changedIdentityRole), 1);
            this.setState({conceptData: this._compileConceptData(this.state)});
            return;
          }
        }
      } else {
        removedIdentityRoles.push(data.id);
        this.setState({conceptData: this._compileConceptData(this.state)});
        return;
      }
    }, () => {
      // Rejected
    });
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
    let concepts = _.merge([], identityRoles);
    for (const addedIdentityRole of addedIdentityRoles) {
      addedIdentityRole._added = true;
    }
    concepts = _.concat(concepts, addedIdentityRoles);
    for (const concept of concepts) {
      if (removedIdentityRoles && removedIdentityRoles.includes(concept.id)) {
        concept._removed = true;
      }
      if (changedIdentityRoles) {
        for (const changedIdentityRole of changedIdentityRoles) {
          if (changedIdentityRole.id === concept.id) {
            concept._changed = true;
            for (const property in changedIdentityRole) {
              if (changedIdentityRole.hasOwnProperty(property)) {
                const key = '_' + property + 'Changed';
                concept[key] = changedIdentityRole[property];
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

  /**
   * Generate cell with actions (buttons)
   */
  _conceptActionsCell({rowIndex, data}) {
    const actions = [];
    const value = data[rowIndex];
    const notModificated = !(value._added || value._removed || value._changed);

    actions.push(
      <Basic.Button
        level={'danger'}
        onClick={this._deleteConcept.bind(this, data[rowIndex])}
        className="btn-xs"
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

  render() {
    const { _showLoading} = this.props;
    const {conceptData, detail} = this.state;
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Basic.Toolbar>
          <div className="pull-right">
            <Basic.Button level="success" className="btn-xs" onClick={this._addConcept.bind(this)}>
              <Basic.Icon value="fa:plus"/>
              {' '}
              {this.i18n('button.add')}
            </Basic.Button>
          </div>
          <div className="clearfix"></div>
        </Basic.Toolbar>
        <Basic.Table
          hover={false}
          data={conceptData}
          rowClass={this._rowClass}
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
            header={this.i18n('entity.IdentityRole.role')}
            property="_embedded.role.name"
            />
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
            header={this.i18n('label.action')}
            className="action"
            cell={this._conceptActionsCell.bind(this)}/>
          </Basic.Table>
          <Basic.Modal
            bsSize="default"
            show={detail.show}
            onHide={this._closeDetail.bind(this)}
            backdrop="static"
            keyboard={!_showLoading}>

            <form onSubmit={this._saveConcept.bind(this)}>
              <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={detail.entity.id === undefined}/>
              <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { role: detail.entity.role })} rendered={detail.entity.id !== undefined}/>
              <Basic.Modal.Body>
                <Basic.AbstractForm className="form-horizontal" ref="form" showLoading={_showLoading} readOnly={!detail.edit}>
                  <Basic.SelectBox
                    ref="role"
                    manager={roleManager}
                    label={this.i18n('entity.IdentityRole.role')}
                    multiSelect={detail.entity._added && detail.add}
                    readOnly={!detail.entity._added}
                    required/>
                  <Basic.DateTimePicker
                    mode="date"
                    className={detail.entity.hasOwnProperty('_validFromChanged') ? 'text-danger' : null}
                    ref={detail.entity.hasOwnProperty('_validFromChanged') ? '_validFromChanged' : 'validFrom'}
                    label={this.i18n('label.validFrom')}/>
                  <Basic.DateTimePicker
                    mode="date"
                    className={detail.entity.hasOwnProperty('_validTillChanged') ? 'text-danger' : null}
                    ref={detail.entity.hasOwnProperty('_validTillChanged') ? '_validTillChanged' : 'validTill'}
                    label={this.i18n('label.validTill')}/>
                </Basic.AbstractForm>
              </Basic.Modal.Body>
              <Basic.Modal.Footer>
                <Basic.Button
                  level="link"
                  onClick={this._closeDetail.bind(this)}
                  showLoading={_showLoading}>
                  {this.i18n('button.close')}
                </Basic.Button>
                <Basic.Button
                  type="submit"
                  level="success"
                  showLoading={_showLoading}
                  showLoadingIcon
                  showLoadingText={this.i18n('button.saving')}
                  rendered={detail.edit}>
                  {this.i18n('button.set')}
                </Basic.Button>
              </Basic.Modal.Footer>
            </form>
          </Basic.Modal>
      </div>
    );
  }
}

IdentityRoleConceptTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  identityUsername: PropTypes.string.isRequired
};

IdentityRoleConceptTable.defaultProps = {
  filterOpened: false,
  _showLoading: false
};


export default IdentityRoleConceptTable;
