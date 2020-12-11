import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import _ from 'lodash';
//
import * as Basic from '../../basic';
import * as Domain from '../../../domain';
import { TreeNodeManager, TreeTypeManager, ConfigurationManager } from '../../../redux';
import EntitySelectBox from '../EntitySelectBox/EntitySelectBox';
import Tree from '../Tree/Tree';

const treeTypeManager = new TreeTypeManager(); // default manager in manager in props is not given
const treeNodeManager = new TreeNodeManager(); // default manager in manager in props is not given

/**
 * Select tree node.
 *
 * @author Radek Tomiška
 */
export default class TreeNodeSelect extends Basic.AbstractFormComponent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      ...this.state,
      defaultTreeType: props.defaultTreeType,
      treeTypeId: null,
      selectedTreeType: null, // modal
      selected: null, // modal
      showTree: false,
      treePaginationRootSize: (
        context && context.store
        ?
        ConfigurationManager.getValue(context.store.getState(), 'idm.pub.app.show.treeNode.tree.pagination.root.size')
        :
        null
      ),
      treePaginationNodeSize: (
        context && context.store
        ?
        ConfigurationManager.getValue(context.store.getState(), 'idm.pub.app.show.treeNode.tree.pagination.node.size')
        :
        null
      )
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (!this.props.defaultTreeType) {
      // fetch default tree type - will be used by default
      // lookout: component doesn't support redux select => class is generalized
      this.context.store.dispatch(this.getTreeTypeManager().fetchDefaultTreeType((defaultTreeType, error) => {
        if (error) {
          this.addError(error);
        } else {
          this.setState({
            defaultTreeType
          });
        }
      }));
    }
  }

  getComponentKey() {
    return 'component.advanced.Tree';
  }

  getManager() {
    return this.props.manager;
  }

  getTreeTypeManager() {
    return this.props.treeTypeManager;
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getValue() {
    if (this.refs.treeNode) {
      return this.refs.treeNode.getValue();
    }
    return undefined;
  }

  setValue(value, cb) {
    if (this.refs.treeNode) {
      this.refs.treeNode.setValue(value, cb);
    }
  }

  isValid() {
    if (this.refs.treeNode) {
      return this.refs.treeNode.isValid();
    }
    return undefined;
  }

  validate(showValidationError, cb) {
    const { rendered } = this.props;
    const { readOnly } = this.state;
    //
    if (readOnly || !rendered) {
      return true;
    }
    return this.refs.treeNode.validate(true, cb);
  }

  setState(json, cb) {
    super.setState(json, () => {
      // FIXME: abstract form component everride standard state to show validations => we need to propage this state into component
      if (json && json.showValidationError !== undefined) {
        if (this.refs.treeNode) {
          this.refs.treeNode.setState({ showValidationError: json.showValidationError}, cb);
        }
      } else if (cb) {
        cb();
      }
    });
  }

  /**
   * Focus input field
   */
  focus() {
    if (this.refs.treeNode) {
      this.refs.treeNode.focus();
    }
  }

  /**
   * Set selected tree tree tree
   *
   * @param {TreeType} treeType
   */
  setTreeType(treeType) {
    this.setState({
      treeTypeId: treeType ? treeType.id : null,
      selectedTreeType: treeType
    }, () => {
      if (this.refs.treeType) {
        this.refs.treeType.setValue(treeType);
      }
    });
  }

  /**
   * Type on form
   *
   * @param  {TreeType} treeType
   */
  onChangeTreeType(treeType) {
    this.setState({
      treeTypeId: treeType ? treeType.id : null,
      selectedTreeType: treeType
    }, () => {
      this.refs.treeNode.setValue(null);
    });
  }

  /**
   * Type on modal
   *
   * @param  {TreeType} treeType
   */
  onChangeSelectedTreeType(treeType) {
    this.setState({
      selectedTreeType: treeType
    });
  }

  /**
   * Show modal with tree
   *
   * @param  {event} event
   */
  showTree(event) {
    if (event) {
      event.preventDefault();
    }
    // set values to tree
    const value = this.getValue();
    let selected = [];
    if (value) {
      selected = _.concat(selected, value); // works for single and multi value
    }
    //
    let treeNodeId = null;
    if (selected.length > 0) {
      treeNodeId = selected[0];
    }
    // load selected tree type, when no type is given.
    // TODO: this not works for form selectbox (find a better place to load tree type)
    if (treeNodeId && !this.state.selectedTreeType) {
      const treeNode = this.getManager().getEntity(this.context.store.getState(), treeNodeId);
      if (treeNode) {
        const treeType = this.getTreeTypeManager().getEntity(this.context.store.getState(), treeNode.treeType);
        if (!treeType) {
          this.setState({
            selected,
            showTree: true,
            selectedTreeType: treeNode.treeType
          });
        } else {
          this.setState({
            selected,
            showTree: true,
            selectedTreeType: this.getTreeTypeManager().getEntity(this.context.store.getState(), treeNode.treeType)
          });
        }
      } else {
        this.setState({
          selected,
          showTree: true
        });
      }
    } else {
      this.setState({
        selected,
        showTree: true
      });
    }
  }

  /**
   * Hide modal with tree
   *
   * @param  {event} event
   */
  hideTree(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      showTree: false
    });
  }

  /**
   * Move selected type and node from tree to form
   *
   * @param  {event} event
   */
  onSelect(nodeId = null, event) {
    if (event) {
      event.preventDefault();
    }
    // selected type and node from tree to form
    const { multiSelect } = this.props;
    const { selectedTreeType, selected } = this.state;
    let _selected = null;
    if (!multiSelect) {
      _selected = nodeId || selected;
      if (_selected && _.isArray(_selected) && _selected.length > 0) {
        _selected = _selected[0];
      }
    } else {
      _selected = selected || [];
      if (nodeId && !_.includes(_selected, nodeId)) {
        _selected = _.concat(_selected, nodeId);
      }
    }
    //
    this.setState({
      treeTypeId: selectedTreeType ? selectedTreeType.id : null,
    }, () => {
      if (this.refs.treeType) {
        // tree type could be hidden
        this.refs.treeType.setValue(selectedTreeType);
      }
      this.refs.treeNode.setValue(_selected);
    });
    //
    this.hideTree();
  }

  /**
   * Tree node field label
   *
   * @return  {string}
   */
  getLabel() {
    const { label } = this.props;
    if (label !== undefined) {
      return label;
    }
    return this.i18n('entity.TreeNode._type');
  }

  /**
   * Select box field placeholder
   *
   * @return  {string}
   */
  getPlaceholder() {
    const { placeholder } = this.props;
    if (placeholder !== undefined) {
      return placeholder;
    }
    return null;
  }

  /**
   * Select box field help block
   *
   * @return  {string}
   */
  getHelpBlock() {
    const { helpBlock } = this.props;
    if (helpBlock !== undefined) {
      return helpBlock;
    }
    return null;
  }

  /**
   * Callback for select node in tree
   *
   * @param  {event} event
   */
  onModalSelect(selected, event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      selected
    });
  }

  /**
   * Tree (modal) header
   *
   * @return  {string}
   */
  getHeader() {
    const { header } = this.props;
    //
    if (header) {
      return header;
    }
    return this.i18n('header');
  }

  _renderShowTreeIcon() {
    const { showTreeType } = this.props;
    const { readOnly } = this.state;
    //
    return (
      <Basic.Div>
        <Basic.LabelWrapper label={ showTreeType || this.getLabel() ? (<span style={{ visibility: 'hidden' }}>T</span>) : null }>
          <Basic.Button
            level="default"
            icon="fa:folder-open"
            style={{ marginLeft: 5 }}
            onClick={ this.showTree.bind(this) }
            title={ this.i18n('showTree.link.title') }
            titlePlacement="bottom"
            disabled={ readOnly }/>
        </Basic.LabelWrapper>
      </Basic.Div>
    );
  }

  render() {
    const {
      rendered,
      useFirstType,
      showTreeType,
      forceSearchParameters,
      required,
      value,
      hidden,
      multiSelect,
      validationErrors,
      validationMessage,
      disableable,
      additionalOptions,
      onChange,
      roots
    } = this.props;
    const {
      treeTypeId,
      showTree,
      selected,
      defaultTreeType,
      readOnly,
      treePaginationRootSize,
      treePaginationNodeSize
    } = this.state;
    //
    // resolve selected tree type from given tree node
    const selectedTreeType = this.state.selectedTreeType || defaultTreeType;
    //
    let _forceTreeType = false;
    if (forceSearchParameters && forceSearchParameters.getFilters().has('treeTypeId')) {
      _forceTreeType = true;
    }
    //
    let formForceSearchParameters = forceSearchParameters || new Domain.SearchParameters();
    if (_forceTreeType) {
      // force tree type is already given => cannot be changed
    } else if (treeTypeId) {
      formForceSearchParameters = formForceSearchParameters.setFilter('treeTypeId', treeTypeId);
    } else if (defaultTreeType) {
      formForceSearchParameters = formForceSearchParameters.setFilter('treeTypeId', defaultTreeType.id);
    } if (showTreeType) {
      formForceSearchParameters = formForceSearchParameters.setFilter('treeTypeId', Domain.SearchParameters.BLANK_UUID);
    }
    let modalForceSearchParameters = new Domain.SearchParameters();
    if (_forceTreeType) {
      modalForceSearchParameters = modalForceSearchParameters.setFilter('treeTypeId', forceSearchParameters.getFilters().get('treeTypeId'));
    } else {
      let selectedTreeTypeId = Domain.SearchParameters.BLANK_UUID;
      if (selectedTreeType) {
        if (_.isObject(selectedTreeType)) {
          selectedTreeTypeId = selectedTreeType.id;
        } else {
          selectedTreeTypeId = selectedTreeType;
        }
      }
      modalForceSearchParameters = modalForceSearchParameters.setFilter('treeTypeId', selectedTreeTypeId);
    }
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <span className={ classNames({ hidden }) }>
        {
          !showTreeType || _forceTreeType
          ||
          <Basic.Div style={{ display: 'flex' }}>
            <Basic.Div style={{ flex: 1 }}>
              <EntitySelectBox
                entityType="treeType"
                ref="treeType"
                manager={ this.getTreeTypeManager() }
                label={ this.i18n('entity.TreeNode.treeType.label') }
                onChange={ this.onChangeTreeType.bind(this) }
                useFirst={ useFirstType && !selectedTreeType }
                required={ required }
                validationErrors={ validationErrors }
                validationMessage={ validationMessage }
                readOnly={ readOnly }
                disableable={ disableable }/>
            </Basic.Div>
            { this._renderShowTreeIcon() }
          </Basic.Div>
        }

        <Basic.Div style={ showTreeType ? {} : { display: 'flex' } }>
          <Basic.Div style={ showTreeType ? {} : { flex: 1 } }>
            <EntitySelectBox
              entityType="treeNode"
              ref="treeNode"
              manager={ this.getManager() }
              label={ this.getLabel() }
              placeholder={ this.getPlaceholder() }
              helpBlock={ this.getHelpBlock() }
              forceSearchParameters={ formForceSearchParameters }
              readOnly={ readOnly || (treeTypeId === null && showTreeType) }
              required={ required }
              validationErrors={ validationErrors }
              validationMessage={ validationMessage }
              value={ value }
              multiSelect={ multiSelect }
              disableable={ disableable }
              additionalOptions={ additionalOptions }
              onChange={ onChange }/>
          </Basic.Div>
          {
            showTreeType
            ||
            this._renderShowTreeIcon()
          }
        </Basic.Div>

        <Basic.Modal
          show={ showTree }
          onHide={ this.hideTree.bind(this) }
          backdrop="static"
          keyboard>
          <Basic.Modal.Header text={ this.getHeader() } closeButton/>
          <Basic.Modal.Body style={{ padding: 0 }}>
            <Tree
              ref="organizationTree"
              uiKey={ this.getUiKey() }
              manager={ this.getManager() }
              forceSearchParameters={ modalForceSearchParameters }
              ŕendered={ showTree }
              traverse={ false }
              clearable={ !((required && !multiSelect)) }
              multiSelect={ multiSelect }
              selected={ !selected || _.isArray(selected) ? selected : [ selected ] }
              header={
                _forceTreeType
                ?
                null
                :
                <EntitySelectBox
                  entityType="treeType"
                  useFirst={ useFirstType && !selectedTreeType }
                  ref="selectedTreeType"
                  value={ selectedTreeType || null }
                  manager={ this.getTreeTypeManager() }
                  onChange={ this.onChangeSelectedTreeType.bind(this) }
                  clearable={ false }
                  className="small"
                  style={{ marginBottom: 0 }}
                  disableable={ disableable }/>
              }
              onChange={ this.onModalSelect.bind(this) }
              onDoubleClick={ (nodeId) => this.onSelect(nodeId) }
              disableable={ disableable }
              paginationRootSize={ treePaginationRootSize }
              paginationNodeSize={ treePaginationNodeSize }
              roots={ roots }
            />
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={ this.hideTree.bind(this) }>
              { this.i18n('button.cancel') }
            </Basic.Button>

            <Basic.Button
              level="success"
              showLoadingIcon
              onClick={ this.onSelect.bind(this, null) }
              disabled={ !!(!selected || (_.isArray(selected) && selected.length === 0)) }>
              {this.i18n('button.select')}
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </span>
    );
  }
}

TreeNodeSelect.propTypes = {
  ...Basic.AbstractFormComponent.propTypes,
  /**
   * Ui key - identifier for loading data
   */
  uiKey: PropTypes.string,
  /**
   * TreeNodeManager instance - manager controls fetching data etc. Default maganer is constructed if no manager is given.
   */
  manager: PropTypes.object,
  /**
   * TreeTypeManager instance - manager controls fetching tree types, default tree type etc. Default maganer is constructed if no manager is given.
   */
  treeTypeManager: PropTypes.object,
  /**
   * Defautl tree type - if no default tree type is defined, then:
   * - all nodes will be filtered together in the form select box
   * - the first tree type will be preset, when modal tree is shown
   * @type {[type]}
   */
  defaultTreeType: PropTypes.object,
  /**
   * Use the first searched value, if value is empty
   */
  useFirstType: PropTypes.bool,
  /**
   * The component is in multi select mode
   */
  multiSelect: PropTypes.bool,
  /**
   * Tree node selectbox label
   */
  label: PropTypes.string,
  /**
   * Tree node selectbox placeholder
   */
  placeholder: PropTypes.string,
  /**
   * Tree node selectbox help block
   */
  helpBlock: PropTypes.string,
  /**
   * Tree (modal) header
   */
  header: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.element
  ]),
  /**
   * Show tree type select - form tree type select will be hidden.
   * Could be orchestrated with force search parameters with tree type is filled.
   */
  showTreeType: PropTypes.bool,
  /**
   * "Hard filters".
   */
  forceSearchParameters: PropTypes.object,
  /**
   * If disabled option can be selected.
   */
  disableable: PropTypes.bool,
  /**
   * "Hard roots" - roots can be loaded from outside and given as parameter, then root will not be loaded by method getRootSearchParameters().
   * Roots can be given as array of ids only - entities has to be loaded in redux store!
   * Search is disabled, if roots are given.
   */
  roots: PropTypes.arrayOf(PropTypes.oneOfType(
    PropTypes.string,
    PropTypes.object
  ))
};

TreeNodeSelect.defaultProps = {
  ...Basic.AbstractFormComponent.defaultProps,
  manager: treeNodeManager,
  treeTypeManager,
  uiKey: 'tree-node-tree',
  defaultTreeType: null,
  useFirstType: true,
  showTreeType: false,
  multiSelect: false,
  disableable: true
};
