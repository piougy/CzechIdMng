import React, { PropTypes } from 'react';
import * as Basic from '../../../components/basic';
import { TreeNodeManager, TreeTypeManager, SecurityManager } from '../../../redux';
import _ from 'lodash';

/**
 * Node detail content
 */
export default class NodeDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.treeNodeManager = new TreeNodeManager();
    this.treeTypeManager = new TreeTypeManager();
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'content.tree.nodes';
  }

  componentDidMount() {
    const { node, type } = this.props;
    this.selectNavigationItem('tree-nodes');
    if (node !== undefined) {
      const loadedNode = _.merge({ }, node);
      // if exist _embedded - edit node, if not exist create new
      if (node._embedded) {
        if (node._embedded.parent) {
          loadedNode.parent = node._embedded.parent.id;
        }
        loadedNode.treeType = node._embedded.treeType.id;
      } else {
        loadedNode.treeType = type;
      }
      this.refs.form.setData(loadedNode);
      this.refs.code.focus();
    }
  }

  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    const entity = this.refs.form.getData();

    if (entity.parent) {
      entity.parent = this.treeNodeManager.getSelfLink(entity.parent);
    }
    if (entity.treeType) {
      entity.treeType = this.treeTypeManager.getSelfLink(entity.treeType);
    }

    if (entity.id === undefined) {
      this.context.store.dispatch(this.treeNodeManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.treeNodeManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.context.store.dispatch(this.treeNodeManager.clearEntities());
    // go back to tree types or organizations
    this.context.router.goBack();
  }

  closeDetail() {
  }

  render() {
    const { uiKey, type } = this.props;
    const { showLoading } = this.state;

    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
          <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('TREENODE_WRITE')} >
            <Basic.SelectBox
              ref="treeType"
              label={this.i18n('entity.TreeNode.treeType.name')}
              manager={this.treeTypeManager}
              required
              readOnly/>
            <Basic.TextField
              ref="code"
              label={this.i18n('entity.TreeType.code')}
              required
              max={255}/>
            <Basic.TextField
              ref="name"
              label={this.i18n('entity.TreeNode.name')}
              required
              min={0}
              max={255}/>
            <Basic.SelectBox
              ref="parent"
              label={this.i18n('entity.TreeNode.parent.name')}
              forceSearchParameters={this.treeNodeManager.getDefaultSearchParameters().setFilter('treeTypeId', type)}
              manager={this.treeNodeManager}/>
            <Basic.Checkbox
              ref="disabled"
              label={this.i18n('entity.TreeNode.disabled')}/>
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading}>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}
              rendered={SecurityManager.hasAuthority('TREENODE_WRITE')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </form>
      </div>
    );
  }
}

NodeDetail.propTypes = {
  node: PropTypes.object,
  type: PropTypes.string,
  uiKey: PropTypes.string.isRequired,
};
