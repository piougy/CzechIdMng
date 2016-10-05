import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import { TreeNodeManager, TreeTypeManager } from '../../../redux';
import NodeTable from './NodeTable';
import uuid from 'uuid';

// Table uiKey
const uiKey = 'nodes_uikey';

/**
* Nodes list
*/
class Nodes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.tree.nodes';
  }

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
    this.selectNavigationItem('tree-nodes');

    if (this._getTypeIdFromParam()) {
      this.context.store.dispatch(this.getTypeManager().fetchEntity(this._getTypeIdFromParam(), uiKey, (type) => {
        // TODO 404
        this.setState({
          showLoading: false,
          type
        });
      }));
    } else {
      const searchParameters = this.getTypeManager().getDefaultSearchParameters();
      this.context.store.dispatch(this.getTypeManager().fetchEntities(searchParameters, uiKey, (types) => {
        const isNoType = types._embedded.treetypes.length === 0 ? true : false;

        if (!isNoType) {
          this.props.history.push('/tree/nodes/', {type: types._embedded.treetypes[0].id});
        }

        this.setState({
          showLoading: false,
          type: types._embedded.treetypes[0],
          isNoType
        });
      }));
    }
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

  newType(event) {
    if (event) {
      event.preventDefault();
    }
    const uuidId = uuid.v1();
    this.context.router.push(`/tree/types/${uuidId}?new=1`);
  }

  render() {
    const { showLoading, type, isNoType } = this.state;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.PageHeader>
          <Basic.Icon value="apple"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          {
            showLoading
            ?
            <Basic.Loading isStatic showLoading/>
            :
            <span>
              {
                !isNoType
                ?
                <NodeTable treeNodeManager={this.getManager()} treeTypeManager={this.getTypeManager()} type={type} />
                :
                <div className="alert alert-info">
                  {this.i18n('content.tree.typeNotFound')} <a href="#" className="alert-link" onClick={this.newType.bind(this)}>{this.i18n('content.tree.newType')}</a>
                </div>
              }
            </span>
          }
        </Basic.Panel>
      </div>
    );
  }
}

Nodes.propTypes = {
};

Nodes.defaultProps = {
};

export default connect()(Nodes);
