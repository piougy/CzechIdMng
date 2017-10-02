import React from 'react';
import { connect } from 'react-redux';
//
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
    this.state = ({
      showLoading: true
    });
  }

  getContentKey() {
    return 'content.tree.nodes';
  }

  getNavigationKey() {
    return 'tree-nodes';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    const isNew = this._isNew();

    if (isNew) {
      this.context.store.dispatch(treeNodeManager.receiveEntity(entityId, { }, null, entity => {
        this.setState({
          showLoading: false,
          node: entity
        });
      }));
    } else {
      this.getLogger().debug(`[NodeContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(treeNodeManager.fetchEntity(entityId, null, entity => {
        this.setState({
          showLoading: false,
          node: entity
        });
      }));
    }
  }

  _isNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  /**
  * Method _getDefaultType return treeType from params if exist,
  * else return type from loaded node
  */
  _getDefaultType() {
    const { query } = this.props.location;
    const { node } = this.state;

    if (node._embedded) {
      return node._embedded.treeType.id;
    }
    return (query) ? query.type : null;
  }

  render() {
    const { node } = this.state;
    return (
      <Basic.Row>
        <div className={this._isNew() ? 'col-lg-offset-1 col-lg-10' : 'col-lg-12'}>
          {
            !node
            ||
            <NodeDetail entity={node} type={this._getDefaultType()} />
          }
        </div>
      </Basic.Row>
    );
  }
}

NodeContent.propTypes = {
};

NodeContent.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(NodeContent);
