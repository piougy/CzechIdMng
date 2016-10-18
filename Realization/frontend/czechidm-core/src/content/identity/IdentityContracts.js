import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { IdentityContractManager, IdentityManager, TreeTypeManager, TreeNodeManager, SecurityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import ManagersInfo from './ManagersInfo';

const uiKey = 'identity-contracts';

class IdentityContracts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityContractManager = new IdentityContractManager();
    this.identityManager = new IdentityManager();
    this.treeTypeManager = new TreeTypeManager();
    this.treeNodeManager = new TreeNodeManager();
    this.state = {
      detail: {
        show: false,
        entity: {}
      },
      treeTypeId: null,
      forceSearchParameters: new SearchParameters().setFilter('treeType', -1)
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
    const treeTypeId = entity._embedded && entity._embedded.workingPosition ? entity._embedded.workingPosition.treeType.id : null;
    const entityFormData = _.merge({}, entity, {
      guarantee: entity._embedded && entity._embedded.guarantee ? entity._embedded.guarantee.id : null,
      workingPosition: entity._embedded && entity._embedded.workingPosition ? entity._embedded.workingPosition.id : null,
      treeTypeId
    });

    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity: entityFormData
      },
      treeTypeId,
      forceSearchParameters: this.state.forceSearchParameters.setFilter('treeType', treeTypeId || -1)
    }, () => {
      this.refs.form.setData(entityFormData);
      this.refs.treeTypeId.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        ... this.state.detail,
        show: false
      },
      treeTypeId: null,
      forceSearchParameters: this.state.forceSearchParameters.setFilter('treeType', -1)
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

  onChangeTreeType(treeType) {
    const treeTypeId = treeType ? treeType.id : null;
    this.setState({
      treeTypeId,
      forceSearchParameters: this.state.forceSearchParameters.setFilter('treeType', treeTypeId || -1)
    }, () => {
      // focus automatically - maybe will be usefull?
      // this.refs.workingPosition.focus();
    });
    this.refs.workingPosition.reload();
  }

  render() {
    const { _entities, _showLoading} = this.props;
    const { detail, forceSearchParameters, treeTypeId } = this.state;

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
                width="175px"
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <span>
                        {
                          data[rowIndex]._embedded && data[rowIndex]._embedded.workingPosition
                          ?
                          this.treeNodeManager.getNiceLabel(data[rowIndex]._embedded.workingPosition)
                          :
                          data[rowIndex].position
                        }
                      </span>
                    );
                  }
                }
              />
              <Basic.Column
                property="treeType"
                header={this.i18n('entity.IdentityContract.treeType')}
                width="175px"
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <span>
                        {
                          !data[rowIndex]._embedded || !data[rowIndex]._embedded.workingPosition
                          ||
                          this.treeTypeManager.getNiceLabel(data[rowIndex]._embedded.workingPosition.treeType)
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
                header={<span title={this.i18n('entity.IdentityContract.managers.title')}>{this.i18n('entity.IdentityContract.managers.label')}</span>}
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <ManagersInfo identityContract={data[rowIndex]}/>
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
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { position: this.getManager().getNiceLabel(detail.entity) })} rendered={!Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" showLoading={_showLoading} className="form-horizontal">
                <Basic.TextField
                  ref="position"
                  label={this.i18n('entity.IdentityContract.position')}/>
                <Basic.SelectBox
                  ref="treeTypeId"
                  manager={this.treeTypeManager}
                  label={this.i18n('entity.IdentityContract.treeType')}
                  onChange={this.onChangeTreeType.bind(this)}/>
                <Basic.SelectBox
                  ref="workingPosition"
                  manager={this.treeNodeManager}
                  label={this.i18n('entity.IdentityContract.workingPosition')}
                  forceSearchParameters={forceSearchParameters}
                  hidden={treeTypeId === null}/>
                <Basic.SelectBox
                  ref="guarantee"
                  manager={this.identityManager}
                  label={this.i18n('entity.IdentityContract.guarantee')}/>
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
