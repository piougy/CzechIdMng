

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
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
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.user.roles';
  }

  componentDidMount() {
  }

  componentDidUpdate() {
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

  _showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      role: entity.id ? entity._embedded.role.name : null
    });

    this.setState({
      detail: {
        show: true,
        showLoading: false,
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

  _deleteConcept() {
    console.log("deleteConcept");
  }

  render() {
    const { _showLoading, identityRoles} = this.props;
    const { detail } = this.state;
    return (
      <div>
        <Basic.Table
          data={identityRoles}
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
            cell={<Basic.DateCell format={this.i18n('format.date')}/>}
            />
          <Basic.Column
            property="validTill"
            header={this.i18n('label.validTill')}
            cell={<Basic.DateCell format={this.i18n('format.date')}/>}/>
          <Basic.Column
            header={this.i18n('label.action')}
            className="action"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Basic.Button
                    level="danger"
                    onClick={this._deleteConcept.bind(this, data[rowIndex])}
                    className="btn-xs"
                    title={this.i18n('button.delete', { delegate: data[rowIndex]._embedded.role.name })}
                    titlePlacement="bottom">
                    <Basic.Icon icon="trash"/>
                  </Basic.Button>
                );
              }
            }/>
          </Basic.Table>
          <Basic.Modal
            bsSize="default"
            show={detail.show}
            onHide={this.closeDetail.bind(this)}
            backdrop="static"
            keyboard={!_showLoading}>

            <form onSubmit={this._saveConcept.bind(this)}>
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
                    ref="validFrom"
                    label={this.i18n('label.validFrom')}/>
                  <Basic.DateTimePicker
                    mode="date"
                    ref="validTill"
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
            </form>
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
