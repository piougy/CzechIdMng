import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { IdentityContractManager, IdentityManager, TreeNodeManager, SecurityManager } from '../../redux';

const uiKey = 'identity-contracts';

class IdentityContracts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityContractManager = new IdentityContractManager();
    this.identityManager = new IdentityManager();
    this.treeNodeManager = new TreeNodeManager();
    this.state = {
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getManager() {
    return this.identityContractManager;
  }

  getContentKey() {
    return 'content.identity.identityContracts';
  }

  componentDidMount() {
    this.selectSidebarItem('profile-contracts');
    const { entityId } = this.props.params;
    this.context.store.dispatch(this.getManager().fetchContracts(entityId, `${uiKey}-${entityId}`));
  }

  showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      guarantee: entity._embedded && entity._embedded.guarantee ? entity._embedded.guarantee.id : null,
      workingPosition: entity._embedded && entity._embedded.workingPosition ? entity._embedded.workingPosition.id : null
    });

    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity: entityFormData
      }
    }, () => {
      this.refs.form.setData(entityFormData);
      this.refs.workingPosition.focus();
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
    const { entityId } = this.props.params;
    entity.identity = this.identityManager.getSelfLink(entityId);
    entity.guarantee = this.identityManager.getSelfLink(entity.guarantee);
    entity.workingPosition = this.treeNodeManager.getSelfLink(entity.workingPosition);
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(this.getManager().createEntity(entity, `${uiKey}-${entityId}`, (savedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('create.success', { position: this.getManager().getNiceLabel(savedEntity), username: entityId }) });
          this._afterSave(error);
        } else if (error.statusCode === 202) {
          this.addMessage({ level: 'info', message: this.i18n('create.accepted', { position: this.getManager().getNiceLabel(savedEntity), username: entityId }) });
          this.closeDetail();
        } else {
          this._afterSave(error);
        }
      }));
    } else {
      this.context.store.dispatch(this.getManager().patchEntity(entity, `${uiKey}-${entityId}`, (savedEntity, error) => {
        this._afterSave(error);
        if (!error) {
          this.addMessage({ message: this.i18n('edit.success', { position: this.getManager().getNiceLabel(savedEntity), username: entityId }) });
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
    const { entityId } = this.props.params;
    this.refs['confirm-delete'].show(
      this.i18n(`action.delete.message`, { count: 1, record: entity.position }),
      this.i18n(`action.delete.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(this.getManager().deleteEntity(entity, `${uiKey}-${entityId}`, (deletedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('delete.success', { position: this.getManager().getNiceLabel(entity), username: entityId }) });
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // Rejected
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
          <Basic.Loading showLoading className="static"/>
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
              noData={this.i18n('identityContracts.empty')}
              rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>
              <Basic.Column
                className="detail-button"
                cell={
                  ({rowIndex, data}) => {
                    return (
                      <Advanced.DetailButton onClick={this.showDetail.bind(this, data[rowIndex])}/>
                    );
                  }
                }
                rendered={SecurityManager.isAdmin()}/>
              <Basic.Column
                property="workingPosition"
                header={this.i18n('entity.IdentityContract.workingPosition')}
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <span>
                        {
                          !data[rowIndex]._embedded
                          ||
                          this.treeNodeManager.getNiceLabel(data[rowIndex]._embedded.workingPosition)
                        }
                      </span>
                    );
                  }
                }
              />
              <Basic.Column
                property="validFrom"
                header={this.i18n('entity.IdentityContract.validFrom')}
                cell={<Basic.DateCell format={this.i18n('format.date')}/>}
              />
              <Basic.Column
                property="validTill"
                header={this.i18n('entity.IdentityContract.validTill')}
                cell={<Basic.DateCell format={this.i18n('format.date')}/>}/>
              <Basic.Column
                property="guarantee"
                header={this.i18n('entity.IdentityContract.guarantee')}
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <span>
                        {
                          !data[rowIndex]._embedded
                          ||
                          this.identityManager.getNiceLabel(data[rowIndex]._embedded.guarantee)
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
                  ({rowIndex, data}) => {
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
              <Basic.AbstractForm ref="form" showLoading={_showLoading} className="form-horizontal">
                <Basic.TextField
                  ref="position"
                  label={this.i18n('entity.IdentityContract.position')}/>
                <Basic.SelectBox
                  ref="workingPosition"
                  manager={this.treeNodeManager}
                  label={this.i18n('entity.IdentityContract.workingPosition')}/>
                <Basic.DateTimePicker
                  mode="date"
                  ref="validFrom"
                  label={this.i18n('label.validFrom')}/>
                <Basic.DateTimePicker
                  mode="date"
                  ref="validTill"
                  label={this.i18n('label.validTill')}/>
                <Basic.SelectBox
                  ref="guarantee"
                  manager={this.identityManager}
                  label={this.i18n('entity.IdentityContract.guarantee')}/>
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

IdentityContracts.propTypes = {
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(React.PropTypes.object)
};
IdentityContracts.defaultProps = {
  _showLoading: true,
  _entities: []
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-${component.params.entityId}`),
    _entities: Utils.Ui.getEntities(state, `${uiKey}-${component.params.entityId}`)
  };
}

export default connect(select)(IdentityContracts);
