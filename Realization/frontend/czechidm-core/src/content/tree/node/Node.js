import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import { TreeNodeManager } from '../../../redux';
import * as Advanced from '../../../components/advanced';

const manager = new TreeNodeManager();

/**
 * Trre node detail
 *
 * @author Radek Tomiška
 */
class Node extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
  }

  render() {
    const { entity, showLoading } = this.props;

    return (
      <Basic.Div>
        <Advanced.DetailHeader
          icon="apple"
          entity={ entity }
          showLoading={ !entity && showLoading }
          back={ entity ? `/tree/nodes?type=${ entity.treeType }` : null }>
          { manager.getNiceLabel(entity) } <small> { this.i18n('content.tree.node.detail.header') }</small>
        </Advanced.DetailHeader>

        <Advanced.TabPanel parentId="tree-nodes" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

Node.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
Node.defaultProps = {
  entity: null,
  showLoading: false
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(Node);
