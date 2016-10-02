import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../../components/basic';
import { TreeNodeManager } from '../../../redux';
import NodeDetail from './NodeDetail';

const treeNodeManager = new TreeNodeManager();

/**
 * Node detail content
 */
class NodeContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.tree.nodes';
  }

  componentDidMount() {
    this.selectNavigationItem('tree-nodes');
    const { entityId } = this.props.params;
    const isNew = this._getIsNew();

    if (isNew) {
      this.context.store.dispatch(treeNodeManager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[NodeContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(treeNodeManager.fetchEntity(entityId));
    }
  }

  _getIsRoot() {
    const { node } = this.props;
    if (node._embedded !== undefined) {
      return node._embedded.parent === undefined;
    }
  }

  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  _getNodeType(node) {
    if (node && node._embedded) {
      return node._embedded.treeType.id;
    }
    const { query } = this.props.location;
    return (query) ? query.type : null;
  }

  render() {
    const { node, showLoading } = this.props;
    return (
      <div>
        <Helmet title={this.i18n('title')} rendered={!this._getIsNew()} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        {
          !node
          ||
          <Basic.PageHeader>
            <Basic.Icon value="apple"/>
            {' '}
            {
              this._getIsNew()
              ?
              this.i18n('create')
              :
              <span>{node.name} <small>{this.i18n('edit')}</small></span>
            }
          </Basic.PageHeader>
        }
        <Basic.Panel>
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !node
            ||
            <NodeDetail node={node} type={this._getNodeType(node)} isNew={this._getIsNew()} isRoot={this._getIsRoot()} />
          }
        </Basic.Panel>

      </div>
    );
  }
}

NodeContent.propTypes = {
  node: PropTypes.object,
  showLoading: PropTypes.bool
};
NodeContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    node: treeNodeManager.getEntity(state, entityId),
    showLoading: treeNodeManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(NodeContent);
