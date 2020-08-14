import React from 'react';
import { connect } from 'react-redux';
//
import uuid from 'uuid';
import * as Basic from '../../../components/basic';
import { TreeNodeManager, TreeTypeManager, SecurityManager } from '../../../redux';
import NodeTable from './NodeTable';

// Table uiKey
const uiKey = 'tree-node-content';

/**
 * Tree Nodes list.
 *
 * @author Radek Tomiška
 */
class Nodes extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: true,
      isNoType: false,
    };
    this.treeNodeManager = new TreeNodeManager();
    this.treeTypeManager = new TreeTypeManager();
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (this._getTypeIdFromParam()) {
      this.context.store.dispatch(this.getTypeManager().autocompleteEntityIfNeeded(this._getTypeIdFromParam(), uiKey, (type) => {
        // TODO 404
        this.setState({
          showLoading: false,
          type
        });
      }));
    } else {
      const searchParameters = this.getTypeManager().getDefaultSearchParameters().setName('autocomplete');
      this.context.store.dispatch(this.getTypeManager().fetchEntities(searchParameters, uiKey, (types) => {
        const isNoType = !!(!types || types._embedded.treeTypes.length === 0);

        if (types && !isNoType) {
          this.context.history.push('/tree/nodes/', { type: types._embedded.treeTypes[0].id });
        }

        this.setState({
          showLoading: false,
          type: types ? types._embedded.treeTypes[0] : null,
          isNoType
        });
      }));
    }
  }

  getContentKey() {
    return 'content.organizations';
  }

  getNavigationKey() {
    return 'tree-nodes';
  }

  getManager() {
    return this.treeNodeManager;
  }

  getTypeManager() {
    return this.treeTypeManager;
  }

  _getTypeIdFromParam() {
    const { query } = this.props.location;
    return (query) ? query.type : null;
  }

  onCreateType(event) {
    if (event) {
      event.preventDefault();
    }
    //
    const uuidId = uuid.v1();
    this.context.history.push(`/tree/types/${ uuidId }?new=1`);
  }

  render() {
    const { showLoading, type, isNoType } = this.state;
    //
    return (
      <Basic.Div>
        { this.renderPageHeader() }
        {
          showLoading
          ?
          <Basic.Loading isStatic showLoading/>
          :
          <span>
            {
              isNoType
              ?
              <Basic.Alert
                level="info"
                buttons={[
                  <Basic.Button
                    level="primary"
                    text={ this.i18n('content.tree.newType.label') }
                    rendered={ SecurityManager.hasAuthority('TREE_TYPE_CREATE') }
                    onClick={ this.onCreateType.bind(this) }/>
                ]}>
                { this.i18n('content.tree.typeNotFound') }
                {' '}
                {
                  !SecurityManager.hasAuthority('TREE_TYPE_CREATE')
                  ||
                  <a href="#" className="alert-link" onClick={ this.onCreateType.bind(this) }>{ this.i18n('content.tree.newType.title') }</a>
                }
              </Basic.Alert>
              :
              <NodeTable uiKey={ uiKey } treeNodeManager={ this.getManager() } type={ type } activeTab={ 2 }/>
            }
          </span>
        }
      </Basic.Div>
    );
  }
}

Nodes.propTypes = {
};

Nodes.defaultProps = {
};

export default connect()(Nodes);
