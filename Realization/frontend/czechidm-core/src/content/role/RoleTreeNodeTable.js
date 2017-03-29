import React, { PropTypes } from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import RecursionTypeEnum from '../../enums/RecursionTypeEnum';
//
import { RoleManager, TreeNodeManager, SecurityManager } from '../../redux';

const roleManager = new RoleManager();
const treeNodeManager = new TreeNodeManager();

/**
* Table of automatic roles
*
* @author Radek Tomiška
*/
export class RoleTreeNodeTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.tree-nodes';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getManager() {
    return this.props.manager;
  }

  showDetail(entity) {
    const { forceSearchParameters } = this.props;
    //
    let roleId = null;
    let treeNodeId = null;
    if (forceSearchParameters.getFilters().has('roleId')) {
      roleId = forceSearchParameters.getFilters().get('roleId');
    }
    if (forceSearchParameters.getFilters().has('treeNodeId')) {
      treeNodeId = forceSearchParameters.getFilters().get('treeNodeId');
    }
    //
    const entityFormData = _.merge({}, entity, {
      role: entity._embedded && entity._embedded.role ? entity._embedded.role : roleId,
      treeNode: entity._embedded && entity._embedded.treeNode ? entity._embedded.treeNode : treeNodeId
    });
    //
    super.showDetail(entityFormData, () => {
      this.refs.role.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
    }
    //
    super.afterSave(entity, error);
  }

  render() {
    const { uiKey, manager, columns, forceSearchParameters, _showLoading } = this.props;
    const { detail } = this.state;
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={manager}
          forceSearchParameters={forceSearchParameters}
          showRowSelection={SecurityManager.hasAuthority('ROLE_DELETE')}
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={this.showDetail.bind(this, { recursionType: RecursionTypeEnum.findKeyBySymbol(RecursionTypeEnum.NO) })}
                rendered={SecurityManager.hasAuthority('ROLE_WRITE')}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }>

          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column
            property="_embedded.role.name"
            width="40%"
            header={ this.i18n('entity.RoleTreeNode.role') }
            face="text"
            rendered={_.includes(columns, 'role')}/>
          <Advanced.Column
            property="_embedded.treeNode.name"
            width="40%"
            header={ this.i18n('entity.RoleTreeNode.treeNode') }
            face="text"
            rendered={_.includes(columns, 'treeNode')}/>
          <Advanced.Column
            property="recursionType"
            header={ this.i18n('entity.RoleTreeNode.recursionType') }
            face="enum"
            enumClass={RecursionTypeEnum}
            sort
            rendered={_.includes(columns, 'recursionType')}/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name })} rendered={!Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" showLoading={_showLoading}>
                <Basic.SelectBox
                  ref="role"
                  manager={roleManager}
                  label={this.i18n('entity.RoleTreeNode.role')}
                  readOnly={!Utils.Entity.isNew(detail.entity) || !_.includes(columns, 'role')}
                  required/>
                <Basic.SelectBox
                  ref="treeNode"
                  manager={treeNodeManager}
                  label={this.i18n('entity.RoleTreeNode.treeNode')}
                  readOnly={!Utils.Entity.isNew(detail.entity) || !_.includes(columns, 'treeNode')}
                  required/>
                <Basic.EnumSelectBox
                  ref="recursionType"
                  enum={RecursionTypeEnum}
                  label={this.i18n('entity.RoleTreeNode.recursionType')}
                  readOnly={!Utils.Entity.isNew(detail.entity)}
                  required/>
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
                rendered={Utils.Entity.isNew(detail.entity)}
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

RoleTreeNodeTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  //
  _showLoading: PropTypes.bool
};

RoleTreeNodeTable.defaultProps = {
  columns: ['role', 'treeNode', 'recursionType'],
  forceSearchParameters: null,
  _showLoading: false
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
  };
}

export default connect(select)(RoleTreeNodeTable);
