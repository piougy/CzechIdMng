import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';
import * as Utils from '../../../utils';
import { TreeNodeManager, TreeTypeManager, SecurityManager } from '../../../redux';

/**
 * Node detail content
 */
export default class NodeDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.treeNodeManager = new TreeNodeManager();
    this.treeTypeManager = new TreeTypeManager();
    this.state = {
      _showLoading: false
    };
  }

  getContentKey() {
    return 'content.tree.node.detail';
  }

  getNavigationKey() {
    return 'tree-node-detail';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entity, type } = this.props;
    if (entity !== undefined) {
      const loadedNode = _.merge({ }, entity);
      // if exist _embedded - edit node, if not exist create new
      if (entity._embedded) {
        if (entity._embedded.parent) {
          loadedNode.parent = entity._embedded.parent.id;
        }
        loadedNode.treeType = entity._embedded.treeType.id;
      } else {
        loadedNode.treeType = type;
      }
      this.refs.form.setData(loadedNode);
      this.refs.code.focus();
    }
  }

  save(afterAction, event) {
    if (event) {
      event.preventDefault();
    }
    //
    const { uiKey } = this.props;
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      _showLoading: true
    }, () => {
      const entity = this.refs.form.getData();
      this.refs.form.processStarted();

      if (entity.parent) {
        entity.parent = this.treeNodeManager.getSelfLink(entity.parent);
      }
      if (entity.treeType) {
        entity.treeType = this.treeTypeManager.getSelfLink(entity.treeType);
      }

      if (entity.id === undefined) {
        this.context.store.dispatch(this.treeNodeManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error, afterAction);
        }));
      } else {
        this.context.store.dispatch(this.treeNodeManager.patchEntity(entity, `${uiKey}-detail`, (patchedEntity, error) => {
          this._afterSave(patchedEntity, error, afterAction);
        }));
      }
    });
  }

  _afterSave(entity, error, afterAction = 'CLOSE') {
    this.setState({
      _showLoading: false
    }, () => {
      this.refs.form.processEnded();
      if (error) {
        this.addError(error);
        return;
      }
      //
      this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      this.context.store.dispatch(this.treeNodeManager.clearEntities());
      //
      if (afterAction === 'CLOSE') {
        // go back to tree types or organizations
        this.context.router.goBack();
      } else {
        this.context.router.replace(`/tree/nodes/${entity.id}/detail`);
      }
    });
  }

  closeDetail() {
  }

  render() {
    const { uiKey, type, entity, showLoading } = this.props;
    const { _showLoading } = this.state;

    return (
      <div>
        <Helmet title={Utils.Entity.isNew(entity) ? this.i18n('create.title') : this.i18n('edit.title')} />

        <form onSubmit={this.save.bind(this, 'CONTINUE')}>
          <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
            <Basic.PanelHeader text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('label')} />

            <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
              <Basic.AbstractForm
                ref="form"
                showLoading={ _showLoading || showLoading }
                uiKey={uiKey}
                readOnly={!SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'TREENODE_CREATE' : 'TREENODE_UPDATE')}>

                <Basic.SelectBox
                  ref="treeType"
                  label={this.i18n('entity.TreeNode.treeType.name')}
                  manager={this.treeTypeManager}
                  required
                  readOnly/>

                <Basic.Row>
                  <div className="col-lg-2">
                    <Basic.TextField
                      ref="code"
                      label={this.i18n('entity.TreeType.code')}
                      required
                      max={255}/>
                  </div>
                  <div className="col-lg-10">
                    <Basic.TextField
                      ref="name"
                      label={this.i18n('entity.TreeNode.name')}
                      required
                      min={0}
                      max={255}/>
                  </div>
                </Basic.Row>

                <Basic.SelectBox
                  ref="parent"
                  label={this.i18n('entity.TreeNode.parent.name')}
                  forceSearchParameters={this.treeNodeManager.getDefaultSearchParameters().setFilter('treeTypeId', type)}
                  manager={this.treeNodeManager}/>
                <Basic.Checkbox
                  ref="disabled"
                  label={this.i18n('entity.TreeNode.disabled')}/>
              </Basic.AbstractForm>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" showLoading={_showLoading} onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>

              <Basic.SplitButton
                level="success"
                title={this.i18n('button.saveAndContinue')}
                onClick={this.save.bind(this, 'CONTINUE')}
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'TREENODE_CREATE' : 'TREENODE_UPDATE')}
                pullRight
                dropup>
                <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'CLOSE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
              </Basic.SplitButton>
            </Basic.PanelFooter>
          </Basic.Panel>
          {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
          <input type="submit" className="hidden"/>
        </form>
      </div>
    );
  }
}

NodeDetail.propTypes = {
  entity: PropTypes.object,
  type: PropTypes.string,
  uiKey: PropTypes.string.isRequired,
};
