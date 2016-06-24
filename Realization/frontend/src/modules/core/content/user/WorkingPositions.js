'use strict';

import React, { Component, PropTypes } from 'react';
import Helmet from 'react-helmet';
import { Link }  from 'react-router';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import * as Utils from '../../utils';
import { IdentityWorkingPositionManager, IdentityManager, OrganizationManager, SecurityManager } from '../../redux';

const uiKey = 'identity-working-positions';

class WorkingPositions extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityWorkingPositionManager = new IdentityWorkingPositionManager();
    this.identityManager = new IdentityManager();
    this.organizationManager = new OrganizationManager();
    this.state = {
      detail: {
        show: false,
        entity: {}
      }
    }
  }

  getManager() {
    return this.identityWorkingPositionManager;
  }

  getContentKey() {
    return 'content.user.workingPositions';
  }

  componentDidMount() {
    this.selectSidebarItem('profile-working-positions');
    const { userID } = this.props.params;
    this.context.store.dispatch(this.getManager().fetchWorkingPositions(userID, `${uiKey}-${userID}`));
  }

  showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      manager: entity.id && entity._embedded.manager? entity._embedded.manager.username : null,
      organization: entity.id && entity._embedded.organization ? entity._embedded.organization.id : null
    });

    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity: entityFormData
      }
    }, () => {
      this.refs.form.setData(entityFormData);
      this.refs.position.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        ... this.state.detail,
        show: false
      }
    });
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();
    const { userID } = this.props.params;
    entity.identity = this.identityManager.getSelfLink(userID);
    entity.manager = this.identityManager.getSelfLink(entity.manager);
    entity.organization = this.organizationManager.getSelfLink(entity.organization);
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(this.identityWorkingPositionManager.createEntity(entity, `${uiKey}-${userID}`, (savedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('create.success', { position: entity.position, username: userID }) });
          this._afterSave(error);
        } else if (error.statusCode === 202) {
          this.addMessage({ level: 'info', message: this.i18n('create.accepted', { position: entity.position, username: userID }) });
          this.closeDetail();
        } else {
          this._afterSave(error);
        }
      }));
    } else {
      this.context.store.dispatch(this.identityWorkingPositionManager.patchEntity(entity, `${uiKey}-${userID}`, (savedEntity, error) => {
        this._afterSave(error);
        if (!error) {
          this.addMessage({ message: this.i18n('edit.success', { position: entity.position, username: userID }) });
        }
      }));
    }
  }

  _afterSave(error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.closeDetail();
  }

  onDelete(entity, event) {
    if (event) {
      event.preventDefault();
    }
    const { userID } = this.props.params;
    this.refs['confirm-delete'].show(
      this.i18n(`action.delete.message`, { count: 1, record: entity.position }),
      this.i18n(`action.delete.header`, { count: 1 })
    ).then(result => {
      this.context.store.dispatch(this.identityWorkingPositionManager.deleteEntity(entity, `${uiKey}-${userID}`, (deletedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('delete.success', { position: entity.position, username: userID }) });
        } else {
          this.addError(error);
        }
      }));
    }, (err) => {
      //Rejected
    });
  }

  render() {
    const { _entities, _showLoading} = this.props;
    const { detail } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('header')}
        </Basic.ContentHeader>

        {
          _showLoading
          ?
          <Basic.Loading showLoading={true} className="static"/>
          :
          <Basic.Panel className="no-border last">
            <Basic.Toolbar rendered={SecurityManager.isAdmin()}>
              <div className="pull-right">
                <Basic.Button level="success" className="btn-xs" onClick={this.showDetail.bind(this, {})}>
                  <Basic.Icon value="fa:plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              </div>
              <div className="clearfix"></div>
            </Basic.Toolbar>

            <Basic.Table
              data={_entities}
              noData={this.i18n('workingPositions.empty')}
              rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>
              <Basic.Column
                className="detail-button"
                cell={
                  ({rowIndex, data, property, ...props}) => {
                    return (
                      <Advanced.DetailButton onClick={this.showDetail.bind(this, data[rowIndex])}/>
                    );
                  }
                }
                rendered={SecurityManager.isAdmin()}/>
              <Basic.Column
                header={this.i18n('entity.IdentityWorkingPosition.position')}
                property="position"
              />
              <Basic.Column
                property="validFrom"
                header={this.i18n('entity.IdentityWorkingPosition.validFrom')}
                cell={<Basic.DateCell format={this.i18n('format.date')}/>}
              />
              <Basic.Column
                property="validTill"
                header={this.i18n('entity.IdentityWorkingPosition.validTill')}
                cell={<Basic.DateCell format={this.i18n('format.date')}/>}/>
              <Basic.Column
                property="manager"
                header={this.i18n('entity.IdentityWorkingPosition.manager')}
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <span>
                        {
                          !data[rowIndex]._embedded
                          ||
                          this.identityManager.getNiceLabel(data[rowIndex]._embedded.manager)
                        }
                      </span>
                    );
                  }
                }
              />
              <Basic.Column
                property="organization"
                header={this.i18n('entity.IdentityWorkingPosition.organization')}
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <span>
                        {
                          !data[rowIndex]._embedded
                          ||
                          this.organizationManager.getNiceLabel(data[rowIndex]._embedded.organization)
                        }
                      </span>
                    );
                  }
                }
              />
              <Basic.Column
                header={this.i18n('label.action')}
                className="action"
                cell={
                  ({rowIndex, data, property, ...props}) => {
                    return (
                      <Basic.Button
                        level="danger"
                        onClick={this.onDelete.bind(this, data[rowIndex])}
                        className="btn-xs"
                        title={this.i18n('button.delete')}
                        titlePlacement="bottom">
                        <Basic.Icon icon="trash"/>
                      </Basic.Button>
                    );
                  }
                }
                rendered={SecurityManager.isAdmin()}/>
            </Basic.Table>
          </Basic.Panel>
        }

        <Basic.Modal
          bsSize="default"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this)}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={detail.entity.id === undefined}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { role: detail.entity.role })} rendered={detail.entity.id !== undefined}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" showLoading={_showLoading}>
                <Basic.TextField
                  ref="position"
                  label={this.i18n('entity.IdentityWorkingPosition.position')}
                  required/>
                <Basic.DateTimePicker
                  mode="date"
                  ref="validFrom"
                  label={this.i18n('label.validFrom')}/>
                <Basic.DateTimePicker
                  mode="date"
                  ref="validTill"
                  label={this.i18n('label.validTill')}/>
                <Basic.SelectBox
                  ref="manager"
                  manager={this.identityManager}
                  label={this.i18n('entity.IdentityWorkingPosition.manager')}/>
                <Basic.SelectBox
                  ref="organization"
                  manager={this.organizationManager}
                  label={this.i18n('entity.IdentityWorkingPosition.organization')}/>
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
                showLoadingIcon={true}
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

WorkingPositions.propTypes = {
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(React.PropTypes.object)
};
WorkingPositions.defaultProps = {
  _showLoading: true,
  _entities: []
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-${component.params.userID}`),
    _entities: Utils.Ui.getEntities(state, `${uiKey}-${component.params.userID}`)
  };
}

export default connect(select)(WorkingPositions);
