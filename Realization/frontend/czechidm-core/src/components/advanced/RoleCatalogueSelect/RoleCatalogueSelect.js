import React, { PropTypes } from 'react';
import _ from 'lodash';
//
import * as Basic from '../../basic';
import { RoleCatalogueManager } from '../../../redux';
import EntitySelectBox from '../EntitySelectBox/EntitySelectBox';
import Tree from '../Tree/Tree';

const roleCatalogueManager = new RoleCatalogueManager();  // default manager in manager in props is not given

/**
* Select role catalogue
*
* TODO: multi select (see role select)
* TODO: onChange support
*
* @author Radek Tomiška
*/
export default class RoleCatalogueSelect extends Basic.AbstractFormComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      selected: null, // modal
      showTree: false
    };
  }

  getComponentKey() {
    return 'component.advanced.Tree';
  }

  getManager() {
    return this.props.manager;
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getValue() {
    return this.refs.roleCatalogue.getValue();
  }

  setValue(value, cb) {
    this.refs.roleCatalogue.setValue(value, cb);
  }

  isValid() {
    return this.refs.roleCatalogue.isValid();
  }

  validate(showValidationError, cb) {
    const { rendered } = this.props;
    const { readOnly } = this.state;
    //
    if (readOnly || !rendered) {
      return true;
    }
    return this.refs.roleCatalogue.validate(showValidationError, cb);
  }

  setState(json, cb) {
    super.setState(json, () => {
      // FIXME: abstract form component everride standard state to show validations => we need to propage this state into component
      if (json && json.showValidationError !== undefined) {
        this.refs.roleCatalogue.setState({ showValidationError: json.showValidationError}, cb);
      } else if (cb) {
        cb();
      }
    });
  }

  /**
   * Focus input field
   */
  focus() {
    this.refs.roleCatalogue.focus();
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
    //
    // set values to tree
    const value = this.getValue();
    let selected = [];
    if (value) {
      selected = _.concat(selected, value); // works for single and multi value
    }
    this.setState({
      showTree: true,
      selected
    });
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
    const { selected } = this.state;
    let _selected = null;
    if (!multiSelect) {
      _selected = nodeId || selected;
    } else {
      _selected = selected || [];
      if (nodeId && !_.includes(_selected, nodeId)) {
        _selected = _.concat(_selected, nodeId);
      }
    }
    this.refs.roleCatalogue.setValue(_selected);
    //
    this.hideTree();
  }

  /**
   * Select box field label
   *
   * @return  {string}
   */
  getLabel() {
    const { label } = this.props;
    if (label !== undefined) {
      return label;
    }
    return this.i18n('entity.RoleCatalogue._type');
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
   * Role catalogue (modal) header
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
    const { readOnly } = this.state;
    //
    return (
      <div>
        <Basic.LabelWrapper label={ this.getLabel() ? (<span style={{ visibility: 'hidden' }}>T</span>) : null }>
          <Basic.Button
            level="default"
            icon="fa:folder-open"
            style={{ marginLeft: 5 }}
            onClick={ this.showTree.bind(this) }
            title={ this.i18n('showTree.link.title') }
            titlePlacement="bottom"
            disabled={ readOnly }/>
        </Basic.LabelWrapper>
      </div>
    );
  }

  render() {
    const { rendered, required, value, multiSelect } = this.props;
    const { showTree, selected, readOnly } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <span>
        <div style={{ display: 'flex' }}>
          <div style={{ flex: 1 }}>
            <EntitySelectBox
              entityType="roleCatalogue"
              ref="roleCatalogue"
              manager={ this.getManager() }
              label={ this.getLabel() }
              placeholder={ this.getPlaceholder() }
              helpBlock={ this.getHelpBlock() }
              readOnly={ readOnly }
              required={ required }
              value={ value }
              multiSelect={ multiSelect }/>
          </div>
          { this._renderShowTreeIcon() }
        </div>

        <Basic.Modal
          show={ showTree }
          onHide={ this.hideTree.bind(this) }
          backdrop="static"
          keyboard>
          <Basic.Modal.Header text={ this.getHeader() } closeButton/>
          <Basic.Modal.Body style={{ padding: 0 }}>
            <Tree
              ref="roleCatalogueTree"
              uiKey={ this.getUiKey() }
              manager={ this.getManager() }
              onChange={ this.onModalSelect.bind(this) }
              onDoubleClick={ (nodeId) => this.onSelect(nodeId) }
              clearable={ false }
              multiSelect={ multiSelect }
              selected={ selected }
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
              disabled={ !selected ? true : false }>
              {this.i18n('button.select')}
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </span>
    );
  }
}

RoleCatalogueSelect.propTypes = {
  ...Basic.AbstractFormComponent.propTypes,
  /**
   * Ui key - identifier for loading data
   */
  uiKey: PropTypes.string,
  /**
   * TreeNodemanager instance - manager controls fetching data etc.
   */
  manager: PropTypes.object,
  /**
   * The component is in multi select mode
   */
  multiSelect: PropTypes.bool,
  /**
   * Role catalogue selectbox label
   */
  label: PropTypes.string,
  /**
   * Role catalogue selectbox placeholder
   */
  placeholder: PropTypes.string,
  /**
   * Role catalogue selectbox help block
   */
  helpBlock: PropTypes.string,
  /**
   * Tree (modal) header
   */
  header: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.element
  ])
};

RoleCatalogueSelect.defaultProps = {
  ...Basic.AbstractFormComponent.defaultProps,
  uiKey: 'role-catalogue-tree',
  manager: roleCatalogueManager,
  multiSelect: false
};
