

import React, { PropTypes } from 'react';
import _ from 'lodash';
import moment from 'moment';
//
import * as Basic from '../../../../../components/basic';
import * as Advanced from '../../../../../components/advanced';
import {RoleManager } from '../../../redux';

/**
* Table for keep identity role concept
*/

const roleManager = new RoleManager();

export class IdentityRoleConceptTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      conceptData: {},
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    };
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
      this.setState({identityRoles: _.merge([], nextProps.identityRoles),
         addedIdentityRoles: _.merge([], nextProps.addedIdentityRoles), removedIdentityRoles: _.merge([], nextProps.removedIdentityRoles),
         changedIdentityRoles: _.merge([], nextProps.changedIdentityRoles)}, ()=>{
        this.setState({conceptData: this._compileConceptData(this.state)});
      });
    }
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  _useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  _cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  _showDetail(entity, isEdit = false) {
    const entityFormData = _.merge({}, entity, {
      role: entity.id ? entity._embedded.role.name : null
    });

    this.setState({
      detail: {
        show: true,
        edit: isEdit,
        entity: entityFormData
      }
    }, () => {
      this.refs.form.setData(entityFormData);
      this.refs.role.focus();
    });
  }

  _closeDetail() {
    this.setState({
      detail: {
        ... this.state.detail,
        show: false
      }
    });
  }

  _saveConcept() {
    console.log("doConcept");
  }

  _deleteConcept(data) {
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
  }

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

  _conceptDateCell({rowIndex, data, property}) {
    const changedProperty = '_' + property + 'Changed';
    const format = this.i18n('format.date');
    const propertyValue = Basic.Cell.getPropertyValue(data[rowIndex], property);
    const dataValue = propertyValue ? moment(propertyValue).format(format) : null;
    const oldValue = oldValue ? this.i18n('oldValue', {oldValue: dataValue}) : this.i18n('oldValueNotExist');
    const changedPropertyExist = data[rowIndex].hasOwnProperty(changedProperty);
    return (
      <Basic.DateCell
        className={changedPropertyExist ? 'text-danger' : ''}
        property={changedPropertyExist ? changedProperty : property}
        rowIndex={rowIndex}
        data={data}
        title={changedPropertyExist ? oldValue : null}
        format={format}/>
    );
  }

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
          onClick={this._showDetail.bind(this, data[rowIndex])}
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
        <Basic.Table
          hover={false}
          data={conceptData}
          rowClass={this._rowClass}
          showRowSelection={false}>
          <Basic.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this._showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Basic.Column
            header={this.i18n('entity.IdentityRole.role')}
            property="_embedded.role.name"
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
            onHide={this.closeDetail.bind(this)}
            backdrop="static"
            keyboard={!_showLoading}>


              <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={detail.entity.id === undefined}/>
              <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { role: detail.entity.role })} rendered={detail.entity.id !== undefined}/>
              <Basic.Modal.Body>
                <Basic.AbstractForm ref="form" showLoading={_showLoading}>
                  <Basic.SelectBox
                    ref="role"
                    manager={roleManager}
                    label={this.i18n('entity.IdentityRole.role')}
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
                  onClick={this.closeDetail.bind(this)}
                  showLoading={_showLoading}>
                  {this.i18n('button.close')}
                </Basic.Button>
                <Basic.Button
                  type="submit"
                  level="success"
                  showLoading={_showLoading}
                  showLoadingIcon
                  showLoadingText={this.i18n('button.saving')}>
                  {this.i18n('button.save')}
                </Basic.Button>
              </Basic.Modal.Footer>
          </Basic.Modal>
      </div>
    );
  }
}

IdentityRoleConceptTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  identityRoleManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  forceSearchParameters: PropTypes.object
};

IdentityRoleConceptTable.defaultProps = {
  columns: ['name', 'validTill', 'validFrom'],
  filterOpened: false,
  _showLoading: false
};


export default IdentityRoleConceptTable;
