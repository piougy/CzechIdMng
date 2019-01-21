import React, { PropTypes } from 'react';
import _ from 'lodash';
import classNames from 'classnames';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import { RoleManager, RoleCatalogueManager, SecurityManager } from '../../../redux';
//
import EntitySelectBox from '../EntitySelectBox/EntitySelectBox';
import Table from '../Table/Table';
import Column from '../Table/Column';
import Tree from '../Tree/Tree';
import SearchParameters from '../../../domain/SearchParameters';

const manager = new RoleManager();
const roleCatalogueManager = new RoleCatalogueManager();

/**
* Component for select roles by role catalogue
*
* @author Ondrej Kopr
* @author Radek Tomiška
*/
export default class RoleSelect extends Basic.AbstractFormComponent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      ...this.state,
      selectedRows: [], // contains ids
      roleCatalogue: null,
      showRoleCatalogue: false
    };
  }

  getComponentKey() {
    return 'component.advanced.Tree';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getManager() {
    return this.props.manager;
  }

  getRoleCatalogueManager() {
    return this.props.roleCatalogueManager;
  }

  getValue() {
    const { rendered } = this.props;
    if (!rendered) {
      return null;
    }
    //
    return this.refs.role.getValue();
  }

  setValue(value) {
    const { rendered } = this.props;
    if (!rendered) {
      return;
    }
    //
    this.refs.role.setValue(value);
  }

  /**
  * Defines if component works with complex value.
  * That is using for correct set input value in form component.
  * Complex value could be exist in _embedded map and we need to now if
  * should be used value from field (UUID) or _embedded (entity).
  *
  */
  isValueComplex() {
    return true;
  }

  setState(json, cb) {
    super.setState(json, () => {
      // FIXME: abstract form component everride standard state to show validations => we need to propage this state into component
      if (json && json.showValidationError !== undefined) {
        this.refs.role.setState({ showValidationError: json.showValidationError}, cb);
      } else if (cb) {
        cb();
      }
    });
  }

  isValid() {
    return this.refs.role.isValid();
  }

  validate(showValidationError, cb) {
    const { rendered } = this.props;
    const { readOnly } = this.state;
    //
    if (readOnly || !rendered) {
      return true;
    }
    return this.refs.role.validate(showValidationError, cb);
  }

  /**
   * Focus input field
   */
  focus() {
    if (this.refs.role) {
      this.refs.role.focus();
    }
  }

  /**
   * Show modal with tree
   *
   * @param  {event} event
   */
  showRoleCatalogue(event) {
    if (event) {
      event.preventDefault();
    }
    // set values to table
    const value = this.getValue();
    let selectedRows = [];
    if (value) {
      selectedRows = _.concat(selectedRows, value); // works for single and multi value
    }
    //
    this.setState({
      showRoleCatalogue: true,
      selectedRows
    });
  }

  /**
   * Hide modal with tree
   *
   * @param  {event} event
   */
  hideRoleCatalogue(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      roleCatalogue: null, // TODO: test
      showRoleCatalogue: false
    });
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
    return this.i18n('entity.Role._type');
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
   * Role (modal) header
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

  /**
   * Move selected roles from modal to form
   *
   * @param  {event} event
   */
  onSelect(event) {
    if (event) {
      event.preventDefault();
    }
    const { multiSelect } = this.props;
    const { selectedRows } = this.state;
    let value;
    //
    if (selectedRows.length === 0) {
      value = null;
    } else if (multiSelect) {
      value = selectedRows;
    } else {
      value = selectedRows[0];
    }
    //
    let result = true;
    if (this.props.onChange) {
      result = this.props.onChange(value);
    }
    // if onChange listener returns false, then we can end
    if (result === false) {
      return;
    }
    //
    this.refs.role.setValue(value);
    this.hideRoleCatalogue();
  }

  _addRole(index, value, event) {
    //
    if (event) {
      event.preventDefault();
    }
    let { selectedRows } = this.state;
    const { multiSelect } = this.props;
    //
    if (multiSelect) {
      selectedRows.push(value.id);
      this.setState({
        selectedRows
      });
    } else {
      selectedRows = [];
      selectedRows.push(value.id);
      this.setState({
        selectedRows
      });
    }
  }

  _removeRole(index, value, event) {
    if (event) {
      event.preventDefault();
    }
    //
    let { selectedRows } = this.state;
    //
    selectedRows = _.pull(selectedRows, value.id);
    this.setState({
      selectedRows
    });
  }

  /**
  * Method filtering by roleCatalogue. Filtr si applied to role table
  */
  _filterByRoleCatalogue(roleCatalogueId, event) {
    if (event) {
      event.preventDefault();
      // Stop propagation is important for prohibition of node tree expand.
      // After click on link node, we want only filtering ... not node expand.
      event.stopPropagation();
    }
    this.setState({
      roleCatalogue: roleCatalogueId
    });
  }

  _onRowClick(event, rowIndex, data) {
    if (event) {
      event.preventDefault();
    }
    //
    const { selectedRows } = this.state;
    const role = data[rowIndex];
    const isSelected = _.includes(selectedRows, role.id);
    //
    if (isSelected) {
      this._removeRole(rowIndex, role);
    } else {
      this._addRole(rowIndex, role);
    }
  }

  _onChange(value, event) {
    if (event) {
      event.preventDefault();
    }
    //
    const { onChange, multiSelect } = this.props;
    const selectedRows = [];
    //
    if (value) {
      if (_.isArray(value)) {
        for (const index in value) {
          if (value.hasOwnProperty(index)) {
            if (value[index] && value[index].id) {
              selectedRows.push(value[index].id);
            }
          }
        }
      } else {
        if (value.id) {
          selectedRows.push(value.id);
        }
      }
    }
    //
    let result = true;
    if (onChange) {
      if (multiSelect) {
        result = onChange(selectedRows, value);
      } else {
        result = onChange(selectedRows.length > 0 ? selectedRows[0] : null, value);
      }
    }
    // if onChange listener returns false, then we can end
    if (result === false) {
      return;
    }
    //
    this.setState({
      selectedRows
    });
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
            onClick={ this.showRoleCatalogue.bind(this) }
            title={this.i18n('content.roles.select.showRoleCatalogue')}
            titlePlacement="bottom"
            disabled={ readOnly }/>
        </Basic.LabelWrapper>
      </div>
    );
  }

  render() {
    const {
      columns,
      multiSelect,
      showActionButtons,
      selectRowClass,
      hidden,
      required,
      value,
      rendered
    } = this.props;
    //
    const {
      selectedRows,
      roleCatalogue,
      showRoleCatalogue,
      readOnly
    } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    const showTree = SecurityManager.hasAuthority('ROLECATALOGUE_AUTOCOMPLETE');
    //
    const forceSearchParameters = this.getManager()
      .getDefaultSearchParameters()
      .setName(SearchParameters.NAME_AUTOCOMPLETE)
      .setFilter('roleCatalogue', roleCatalogue);
    //
    // TODO: add onRowClick={this._onRowClick.bind(this)}
    return (
      <span className={ classNames({ hidden }) }>
        <div style={{ display: 'flex' }}>
          <div style={{ flex: 1 }}>
            <EntitySelectBox
              ref="role"
              manager={ this.getManager() }
              label={ this.getLabel() }
              placeholder={ this.getPlaceholder() }
              helpBlock={ this.getHelpBlock() }
              multiSelect={ multiSelect }
              onChange={ this._onChange.bind(this) }
              readOnly={ readOnly }
              entityType="role"
              required={ required }
              value={ value }/>
          </div>
          { this._renderShowTreeIcon() }
        </div>

        <Basic.Modal
          show={ showRoleCatalogue }
          onHide={ this.hideRoleCatalogue.bind(this) }
          bsSize="large"
          backdrop="static"
          keyboard>
          <Basic.Modal.Header text={ this.getHeader() } closeButton/>
          <Basic.Modal.Body style={{ padding: 0 }}>
            <Basic.Row>
              <Basic.Col
                lg={ 3 }
                style={{ paddingRight: 0 }}
                rendered={ showTree === true }>
                <Tree
                  ref="roleCatalogueTree"
                  uiKey={ `${this.getUiKey()}-tree` }
                  manager={ this.getRoleCatalogueManager() }
                  onChange={ this._filterByRoleCatalogue.bind(this) }
                  header={ this.i18n('content.roles.select.chooseFolder') }
                  rendered={ showTree }/>
              </Basic.Col>
              <Basic.Col
                lg={ showTree ? 9 : 12 }
                style={{ paddingLeft: 0 }}>
                <Table
                  ref="table"
                  condensed
                  className={ showTree ? '' : 'marginable' }
                  style={ showTree ? { borderLeft: '1px solid #ddd' } : {} }
                  forceSearchParameters={ forceSearchParameters }
                  showToolbar={false}
                  uiKey={ `${this.getUiKey()}-table` }
                  manager={ this.getManager() }
                  showPageSize={false}
                  rowClass={({rowIndex, data}) => {
                    return _.includes(selectedRows, data[rowIndex].id) ? selectRowClass : Utils.Ui.getDisabledRowClass(data[rowIndex]);
                  }}>
                  <Column
                    property=""
                    header=""
                    width="5px"
                    rendered={showActionButtons}
                    cell={
                      ({ rowIndex, data }) => {
                        const isSelected = _.includes(selectedRows, data[rowIndex].id);
                        if (data[rowIndex].disabled) {
                          return (
                            null
                          );
                        }
                        return (
                          <span>
                            {
                              !isSelected
                              ?
                              <input
                                readOnly={readOnly}
                                type="checkbox"
                                checked={false}
                                onMouseDown={this._addRole.bind(this, rowIndex, data[rowIndex])}/>
                              :
                              <input
                                readOnly={readOnly}
                                type="checkbox"
                                checked
                                onMouseDown={this._removeRole.bind(this, rowIndex, data[rowIndex])}/>
                            }
                          </span>
                        );
                      }
                    }/>
                  <Column property="name" sort={false} face="text" rendered={_.includes(columns, 'name')}/>
                  <Column property="baseCode" sort={false} face="text" rendered={_.includes(columns, 'baseCode')}/>
                  <Column property="environment" sort={false} face="text" rendered={_.includes(columns, 'environment')}/>
                </Table>
              </Basic.Col>
            </Basic.Row>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={ this.hideRoleCatalogue.bind(this) }>
              { this.i18n('button.cancel') }
            </Basic.Button>

            <Basic.Button
              level="success"
              showLoadingIcon
              onClick={ this.onSelect.bind(this) }>
              { this.i18n('button.select') }
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </span>
    );
  }
}

RoleSelect.propTypes = {
  ...Basic.AbstractFormComponent.propTypes,
  uiKey: PropTypes.string,
  /**
   * RoleManager instance - manager controls fetching data etc. Default maganer is constructed if no manager is given.
   */
  manager: PropTypes.object,
  /**
   * RoleCatalogueManager instance - manager controls fetching catalogue. Default maganer is constructed if no manager is given.
   */
  roleCatalogueManager: PropTypes.object,
  columns: PropTypes.arrayOf(PropTypes.string),
  showActionButtons: PropTypes.bool,
  selectRowClass: PropTypes.string,
  /**
   * The component is in multi select mode
   */
  multiSelect: PropTypes.bool
};
RoleSelect.defaultProps = {
  ...Basic.AbstractFormComponent.defaultProps,
  uiKey: 'role-table-select',
  manager,
  roleCatalogueManager,
  columns: ['name', 'baseCode', 'environment'],
  multiSelect: false,
  showActionButtons: true,
  selectRowClass: 'success'
};
