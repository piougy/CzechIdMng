import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
import faker from 'faker';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { SecurityManager, TreeTypeManager } from '../../../redux';
import SearchParameters from '../../../domain/SearchParameters';

// Root nodes  key for tree
const rootNodesKey = 'tree-node-table-roots';

// Table uiKey
const tableUiKey = 'tree-node-table';
const treeTypeManager = new TreeTypeManager();

/**
* Table of nodes
*/
export class NodeTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true,
      showLoading: true,
      type: props.type,
      rootNodes: null,
      rootNodesCount: null
    };
  }

  getContentKey() {
    return 'content.tree.nodes';
  }

  componentDidMount() {
    const { treeNodeManager } = this.props;
    const { type } = this.state;

    const searchParametersRoots = treeNodeManager.getService().getRootSearchParameters().setFilter('treeType', type.id);
    this.context.store.dispatch(treeNodeManager.fetchEntities(searchParametersRoots, rootNodesKey, (loadedRoots) => {
      // get redux state for get total roots count
      const uiState = Utils.Ui.getUiState(this.context.store.getState(), rootNodesKey);
      const rootNodes = loadedRoots._embedded[treeNodeManager.getCollectionType()];
      this.setState({
        rootNodes,
        rootNodesCount: uiState.total,
        showLoading: false
      });
    }));
  }

  componentWillUnmount() {
    this.cancelFilter();
  }

  useFilter(event) {
    const { type } = this.props;

    if (event) {
      event.preventDefault();
    }
    const data = {
      ... this.refs.filterForm.getData(),
      parent: this.refs.filterForm.getData().parent,
      treeType: type.id
    };
    this.refs.table.getWrappedInstance().useFilterData(data);
  }

  _useFilterByTree(nodeId, event) {
    if (event) {
      event.preventDefault();
      // Stop propagation is important for prohibition of node tree expand.
      // After click on link node, we want only filtering ... not node expand.
      event.stopPropagation();
    }
    if (!nodeId) {
      return;
    }
    const data = {
      ... this.refs.filterForm.getData(),
      parent: nodeId
    };
    this.refs.parent.setValue(nodeId);
    this.refs.table.getWrappedInstance().useFilterData(data);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  onDelete(bulkActionValue, selectedRows) {
    const { treeNodeManager } = this.props;
    const { type } = this.state;
    const selectedEntities = treeNodeManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: treeNodeManager.getNiceLabel(selectedEntities[0]), records: treeNodeManager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: treeNodeManager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(treeNodeManager.deleteEntities(selectedEntities, tableUiKey, (entity, error, successEntities) => {
        if (entity && error) {
          this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: treeNodeManager.getNiceLabel(entity) }) }, error);
        }
        if (!error && successEntities) {
          this.context.store.dispatch(treeNodeManager.clearEntities());
          this.refs.table.getWrappedInstance().reload();
          this._changeTree(type);
        }
      }));
    }, () => {
      // nothing
    });
  }

  /**
   * Recive new form for create new node else show detail for existing org.
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (entity.id === undefined) {
      const { type } = this.state;
      const uuidId = uuid.v1();
      this.context.router.push(`/tree/nodes/${uuidId}?new=1&type=${type.id}`);
    } else {
      this.context.router.push(`/tree/nodes/${entity.id}`);
    }
  }

  _changeTree(entity, event) {
    const { treeNodeManager } = this.props;
    if (event) {
      event.preventDefault();
    }
    if (!entity.id) {
      return;
    }

    this.setState({
      showLoading: true
    }, () => {
      const searchParametersRoot = treeNodeManager.getService().getRootSearchParameters().setFilter('treeType', entity.id);
      this.context.store.dispatch(treeNodeManager.fetchEntities(searchParametersRoot, rootNodesKey, (loadedRoot) => {
        if (loadedRoot !== null) {
          this.setState({
            rootNodes: loadedRoot._embedded[treeNodeManager.getCollectionType()],
            type: entity,
            showLoading: false
          });
        } else {
          this.setState({
            type: entity,
            showLoading: false
          });
        }
        this.cancelFilter();
      }));
      this.context.router.push('/tree/nodes/?type=' + entity.id);
    });
  }

/**
 * Decorator for organization tree. Add custom icons and allow filtering after click on node
 */
  _orgTreeHeaderDecorator(props) {
    const style = props.style;
    const icon = props.node.isLeaf ? 'group' : 'building';
    return (
      <div style={style.base}>
        <div style={style.title}>
          <Basic.Icon type="fa" icon={icon} style={{ marginRight: '5px' }}/>
          <Basic.Button level="link" onClick={this._useFilterByTree.bind(this, props.node.id)} style={{padding: '0px 0px 0px 0px'}}>
            { props.node.name }
            {
              !props.node.childrenCount
              ||
              <small style={{ color: '#aaa' }}>{' '}({props.node.childrenCount})</small>
            }
          </Basic.Button>
        </div>
      </div>
    );
  }

  showTypeDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/tree/types/${uuidId}?new=1&b=nodes`);
    } else {
      this.context.router.push('/tree/types/' + entity.id);
    }
  }

  test() {
    for (let i = 1; i <= 7896; i++) {
      this._create(i, 1566);
    }
  }

  _create(id, parent, cb) {
    const { treeNodeManager } = this.props;
    this.context.store.dispatch(treeNodeManager.createEntity({
      code: `f-${id}`,
      name: faker.company.companyName(),
      treeType: treeTypeManager.getSelfLink(this.state.type.id),
      parent: !parent ? null : treeNodeManager.getSelfLink(parent)
    },
    `bulk-create`,
    (createdEntity, error) => {
      if (!error) {
        if (cb) {
          cb(createdEntity);
        }
      } else {
        this.addError(error);
      }
    }));
  }

  render() {
    const { treeNodeManager } = this.props;
    const { filterOpened, rootNodes, showLoading, type, rootNodesCount } = this.state;
    const showTree = !showLoading && rootNodes && rootNodes.length !== 0;
    return (
      <Basic.Row>
        <div className="col-lg-3" style={{ paddingRight: 0, paddingLeft: 0, marginLeft: 15, marginRight: -15 }}>
          <div className="basic-toolbar">
            <div className="pull-left">
              <h3 style={{ margin: 0 }}>{this.i18n('content.tree.typePick')}</h3>
            </div>
            <div className="pull-right">
              <Basic.Button
                level="success"
                className="btn-xs"
                style={{ marginRight: 3 }}
                onClick={this.test.bind(this)}
                rendered={false}>
                T
              </Basic.Button>
              <Basic.Button
                level="success"
                title={this.i18n('addType')}
                titlePlacement="bottom"
                className="btn-xs"
                style={{ marginRight: 3 }}
                onClick={this.showTypeDetail.bind(this, {})}
                rendered={SecurityManager.hasAuthority('TREETYPE_WRITE')}>
                <Basic.Icon value="fa:plus"/>
              </Basic.Button>
              <Basic.Button
                level="primary"
                title={this.i18n('reloadTree')}
                titlePlacement="bottom"
                className="btn-xs"
                onClick={this._changeTree.bind(this, type)}>
                <Basic.Icon value="fa:refresh"/>
              </Basic.Button>
            </div>
            <div className="clearfix"></div>
          </div>
          <div style={{ paddingLeft: 15, paddingRight: 15 }}>
            {
              !type
              ||
              <Basic.AbstractForm ref="treePick" uiKey="tree-pick" className="form-horizontal" >
                <span>
                  <Basic.SelectBox
                    ref="treeType"
                    value={type.name}
                    manager={treeTypeManager}
                    onChange={this._changeTree.bind(this)}
                    componentSpan="col-sm-12"
                    clearable={false} />
                </span>
              </Basic.AbstractForm>
            }
            {
              !showTree
              ||
              <Basic.Panel>
                <Advanced.Tree
                  ref="organizationTree"
                  rootNodes={ rootNodes }
                  rootNodesCount={ rootNodesCount }
                  headerDecorator={this._orgTreeHeaderDecorator.bind(this)}
                  uiKey="orgTree"
                  manager={treeNodeManager}
                  />
              </Basic.Panel>
            }
          </div>
        </div>

        <div className="col-lg-9">
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Advanced.Table
            ref="table"
            uiKey={tableUiKey}
            forceSearchParameters={new SearchParameters().setFilter('treeType', type.id)}
            manager={treeNodeManager}
            showRowSelection={SecurityManager.hasAuthority('TREENODE_DELETE')}
            rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
            style={{ borderLeft: '1px solid #ddd' }}
            showLoading={showLoading}
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                  <Basic.Row>
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={this.i18n('entity.TreeNode.code') + ' / ' + this.i18n('entity.TreeNode.name') }
                        label={this.i18n('entity.TreeNode.code') + ' / ' + this.i18n('entity.TreeNode.name') }/>
                    </div>
                    <div className="col-lg-6 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                  <Basic.Row className="last">
                    <div className="col-lg-6">
                      <Advanced.Filter.SelectBox
                        ref="parent"
                        placeholder={this.i18n('entity.TreeNode.parentId')}
                        label={this.i18n('entity.TreeNode.parent.name')}
                        forceSearchParameters={treeNodeManager.getDefaultSearchParameters().setFilter('treeType', type.id)}
                        manager={treeNodeManager}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            filterOpened={filterOpened}
            actions={
              [
                { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
              ]
            }
            buttons={
              [
                <Basic.Button level="success" key="add_button" className="btn-xs" onClick={this.showDetail.bind(this, {})} rendered={SecurityManager.hasAuthority('TREENODE_WRITE')}>
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
            <Advanced.Column property="code" width="125px" sort face="text"/>
            <Advanced.ColumnLink to="/tree/nodes/:id" property="name" width="20%" sort face="text"/>
            <Advanced.Column property="parent.name" sort/>
            <Advanced.Column property="treeType.name" sort/>
            <Advanced.Column property="disabled" sort face="bool"/>
            <Advanced.Column property="shortName" sort rendered={false}/>
            <Advanced.Column property="parentId" sort rendered={false}/>
          </Advanced.Table>
        </div>
      </Basic.Row>
    );
  }
}

NodeTable.propTypes = {
  type: PropTypes.object.isRequired,
  treeNodeManager: PropTypes.object.isRequired,
  treeTypeManager: PropTypes.object.isRequired
};

NodeTable.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(NodeTable);
