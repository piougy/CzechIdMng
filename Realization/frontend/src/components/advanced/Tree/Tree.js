'use strict';

import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';
import invariant from 'invariant';
import _ from 'lodash';
import Immutable from 'immutable';
//
import * as Basic from '../../basic';
import {Treebeard, decorators} from 'react-treebeard';
import defaultStyle from './styles';

/**
* Advanced tree component
*/
class Tree extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      data: props.rootNode
    }
  }

  /**
  * Manager for entity in tree
  */
  getManager() {
    return this.props.manager;
  }

  componentDidMount() {
    const {rootNode} = this.props;
    if (rootNode && rootNode.toggled){
      this._onToggle(rootNode, rootNode.toggled);
    }
  }

  componentWillReceiveProps(nextProps) {
    const {cursor} = nextProps;
    //cursor is different
    if (nextProps.cursor && this.props.cursor !== cursor){
      const {data} = this.state;
      //We find same node in data and merge new cursor to him
      let oldCursor = this._findNode(cursor.id, data);
      _.merge(oldCursor, cursor);
      this.setState({data: data});
    }
  }

  collapse(){
    const {data} = this.state;
    if (data){
      data.toggled = false;
    }
    this.setState({data: data});
  }

  /**
  * Is call after click on any node in tree
  * @param  {object} node    selected node
  * @param  {boolean} toggled
  */
  _onToggle(node, toggled){
    const {propertyParent, propertyId, uiKey} = this.props;
    let filter = {filter: { operation: 'OR', filters: [{'field': propertyParent, 'value': node[propertyId]}]}};
    if (this.state.cursor){
      this.state.cursor.active = false;
    }
    let loaded = this._loadNode(node, this.context.store.getState());
    if (!loaded){
      node.loading = true;
    }
    node.active = true;
    node.toggled = toggled;
    this.setState({ cursor: node }, ()=>{
      if (!loaded){
        this.context.store.dispatch(this.getManager().fetchEntities(filter, uiKey+node[propertyId]));
      }
    });
  }

  /**
  * Check if is node in Redux state. If is, then set loading attribute to false.
  * @param  {object} node
  * @param  {object} state  Redux state
  * @return {boolean}  Is node loaded
  */
  _loadNode(node, state){
    const {propertyId, uiKey} = this.props;
    let nodeKey =  uiKey+node[propertyId];
    let containsUiKey = this.getManager().containsUiKey(state, nodeKey);
    if (containsUiKey && !node.loading){
      return true;
    }
    if (containsUiKey){
      let children = this.getManager().getEntities(state, nodeKey);
      if (children.length !== 0){
        node.children = children;
        for (let child of children) {
          child.toggled = false;
          if (!child.isLeaf){
            child.children = [];
          }
        }
      }
      node.loading = false;
      node.toggled = true;
      return true;
    }
    return false;
  }

  /**
  * Get decorators (default or custom form props)
  * @return {object} Decorators
  */
  _getDecorators(){
    const {propertyName, loadingDecorator, toggleDecorator, headerDecorator} = this.props;
    return {
      Loading: (props) => {
        if (loadingDecorator){
          return loadingDecorator(props);
        }
        return (
            <div style={props.style}>
                {this.i18n('component.advanced.Tree.loading')}
            </div>
        )
      },
      Toggle: (props) => {
        if (toggleDecorator){
          return toggleDecorator(props);
        }
        return decorators.Toggle(props)
      },
      Header: (props) => {
        if (headerDecorator){
          return headerDecorator(props);
        }
        const style = props.style;
        const iconType = props.node.children ? 'folder' : 'file-text';
        const iconClass = `fa fa-${iconType}`;
        const iconStyle = { marginRight: '5px' };
        return (
          <div style={style.base}>
            <div style={style.title}>
              <i className={iconClass} style={iconStyle}/>
              {props.node[propertyName]}
            </div>
          </div>
        );
      }
    };
  }

  /**
  * Find node with idNode in element (recursively by children)
  * @param  {string} idNode
  * @param  {object} element
  */
  _findNode(idNode, element){
    if (element.id === idNode){
      return element;
    }else if (element.children != null){
      var result = null;
      for (let i = 0; !result && i < element.children.length; i++){
        result = this._findNode(idNode, element.children[i]);
      }
      return result;
    }
    return null;
  }

  render() {
    const { data } = this.state;
    const { propertyName, loadingDecorator, toggleDecorator, headerDecorator, style, showLoading, rendered } = this.props;
    //I have problem with definition Container decorator. I override only Header, Loading and Toggle decorators in default "decorators"
    let customDecorators = this._getDecorators();
    return (
      <div>
        {rendered ?
          (showLoading ?
            <Basic.Well showLoading={true}/>
            :
            <Treebeard
              data={data}
              onToggle={this._onToggle.bind(this)}
              style={style ? style : defaultStyle}
              decorators={{ ...decorators, Header: customDecorators.Header, Loading: customDecorators.Loading /*, Toggle: customDecorators.Toggle*/}}
              />
          )
          :''}
        </div>
      );
    }
}

Tree.propTypes = {
  /**
   * Rendered component
   */
  rendered: PropTypes.bool,
  /**
   * Show loading in component
   */
  showLoading: PropTypes.bool,
  /**
  * EntityManager for fetching entities in tree
  */
  manager: PropTypes.object.isRequired,
  /**
  * Inicial root node of tree
  */
  rootNode: PropTypes.object.isRequired,
  /**
  * Key for save data to redux store
  */
  uiKey: PropTypes.string.isRequired,
  /**
  * Define attribute in entity which will be used for show name of node
  */
  propertyName: PropTypes.string.isRequired,
  /**
  * Define attribute in entity which will be used as node id
  */
  propertyId: PropTypes.string.isRequired,
  /**
  * Define attribute in entity which will be used for search children nodes
  */
  propertyParent: PropTypes.string.isRequired,
  /**
  * Define styles in object
  */
  style: PropTypes.object,
  /**
  * Can be use for override loading decorator
  */
  loadingDecorator: PropTypes.func,
  /**
  * Can be use for override toggle decorator
  */
  toggleDecorator: PropTypes.func,
  /**
  * Can be use for override header decorator
  */
  headerDecorator: PropTypes.func
}

Tree.defaultProps = {
  rendered: true,
  showLoading: false
}

function select(state, component) {
  let wrappedInstance = this ? this.getWrappedInstance() : null;
  if (wrappedInstance && wrappedInstance.state.cursor){
    let cursor = wrappedInstance.state.cursor;
    let haveData = wrappedInstance._loadNode(cursor, state);
    //we need new instance ... we create clone
    cursor = _.merge({}, cursor);
    return{
      cursor: cursor
    }
  }
  return {};
}

export default connect(select, null, null, { withRef: true})(Tree);
