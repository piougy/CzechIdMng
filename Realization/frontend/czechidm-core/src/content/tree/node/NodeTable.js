import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import uuid from 'uuid';
import { SecurityManager } from '../../../redux';

// Root key for tree
const rootKey = 'tree_root';

// Table uiKey
const tableUiKey = 'node_table';


/**
* Table of nodes
*/
export class NodeTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true,
      showLoading: true,
      type: props.type
    };
  }

  getContentKey() {
    return 'content.tree.nodes';
  }

  componentDidMount() {
    const { treeNodeManager } = this.props;
    const { type } = this.state;

    const searchParametersRoot = treeNodeManager.getService().getRootSearchParameters().setFilter('treeType', type.id);
    this.context.store.dispatch(treeNodeManager.fetchEntities(searchParametersRoot, rootKey, (loadedRoot) => {
      const root = loadedRoot._embedded.treenodes[0];
      this.setState({
        root,
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
    const selectedEntities = treeNodeManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: treeNodeManager.getNiceLabel(selectedEntities[0]), records: treeNodeManager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: treeNodeManager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(treeNodeManager.deleteEntities(selectedEntities, tableUiKey, () => {
        this.refs.table.getWrappedInstance().reload();
      }));
    }, () => {
      // nothing
    });
  }

  /**
  * Recive new form for create new node else show detail for existing org.
  */
  showDetail(entity, event) {
    const { type } = this.state;
    if (event) {
      event.preventDefault();
    }
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/tree/nodes/${uuidId}?new=1&type=${type.id}`);
    } else {
      this.context.router.push(`/tree/nodes/${entity.id}?type=${type.id}`);
    }
  }

  _changeTree(entity, event) {
    const { treeNodeManager } = this.props;
    if (event) {
      event.preventDefault();
    }

    this.setState({
      showLoading: true
    });

    const searchParametersRoot = treeNodeManager.getService().getRootSearchParameters().setFilter('treeType', entity.id);
    this.context.store.dispatch(treeNodeManager.fetchEntities(searchParametersRoot, rootKey, (loadedRoot) => {
      if (loadedRoot !== null) {
        this.setState({
          root: loadedRoot._embedded.treenodes[0],
          type: entity,
          showLoading: false
        });
      } else {
        this.setState({
          type: entity,
          showLoading: false
        });
      }
      this.refs.table.getWrappedInstance().reload();
    }));
    this.context.router.push('/tree/nodes/?type=' + entity.id);
  }

  /**
  * Decorator for organization tree. Add custom icons and allow filtering after click on node
  */
  _orgTreeHeaderDecorator(props) {
    const style = props.style;
    const iconType = props.node.isLeaf ? 'group' : 'building';
    const iconClass = `fa fa-${iconType}`;
    const iconStyle = { marginRight: '5px' };
    return (
      <div style={style.base}>
        <div style={style.title}>
          <i className={iconClass} style={iconStyle}/>
          <Basic.Button level="link" onClick={this._useFilterByTree.bind(this, props.node.id)} style={{padding: '0px 0px 0px 0px'}}>
            {props.node.name}
          </Basic.Button>

        </div>
      </div>
    );
  }

  render() {
    const { treeNodeManager, treeTypeManager } = this.props;
    const { filterOpened, root, showLoading, type } = this.state;
    return (
      <Basic.Row>
        <div className="col-lg-12">
          <div className="col-lg-3 col-xs-12 pull-left">
            <div className="col-lg-12" style={{ borderBottom: '1px solid #ddd', marginTop: '-11px' }}>
              <h3>{this.i18n('content.tree.typePick')}</h3>
            </div>
            <div className="col-lg-12">
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
                showLoading
                ||
                <Basic.Panel style={{ marginTop: 15 }}>
                  <Advanced.Tree
                    ref="organizationTree"
                    rootNode={{name: root.name, toggled: true, id: root.id}}
                    propertyId="id"
                    propertyParent="parent"
                    showLoading={showLoading}
                    propertyName="name"
                    headerDecorator={this._orgTreeHeaderDecorator.bind(this)}
                    uiKey={ 'orgTree-' + root.id }
                    manager={treeNodeManager}
                    />
                </Basic.Panel>
              }
            </div>
          </div>
          <div className="col-lg-9 col-xs-12 pull-right" style={{ paddingRight: 0, paddingLeft: 0 }}>
            <Basic.Confirm ref="confirm-delete" level="danger"/>
            <Advanced.Table
              ref="table"
              uiKey={tableUiKey}
              forceSearchParameters={treeNodeManager.getDefaultSearchParameters().setFilter('treeType', type.id)}
              manager={treeNodeManager}
              showRowSelection={SecurityManager.hasAuthority('TREENODE_DELETE')}
              rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
              style={{ borderLeft: '1px solid #ddd' }}
              showLoading={showLoading}
              filter={
                <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                  {
                    showLoading
                    ||
                    <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                      <Basic.Row>
                        <div className="col-lg-6">
                          <Advanced.Filter.TextField
                            ref="text"
                            placeholder={this.i18n('entity.TreeNode.name')}
                            label={this.i18n('entity.TreeNode.name')}/>
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
                  }
                </Advanced.Filter>
              }
              filterOpened={!filterOpened}
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
              <Advanced.ColumnLink to={"tree/nodes/:id"} property="name" width="20%" sort face="text"/>
              <Advanced.Column property="parent.name" sort/>
              <Advanced.Column property="treeType.name" sort/>
              <Advanced.Column property="disabled" sort face="bool"/>
              <Advanced.Column property="shortName" sort rendered={false}/>
              <Advanced.Column property="parentId" sort rendered={false}/>
            </Advanced.Table>
          </div>
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

export default connect()(NodeTable);
