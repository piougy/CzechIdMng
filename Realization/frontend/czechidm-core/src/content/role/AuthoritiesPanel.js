import React, { PropTypes } from 'react';
import Immutable from 'immutable';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { DataManager } from '../../redux';

const AVAILABLE_AUTHORITIES_UIKEY = 'available-authorities';

/**
* Panel of identities
*/
export class AuthoritiesPanel extends Basic.AbstractContextComponent {

  componentDidMount() {
    const { roleManager } = this.props;
    //
    this.context.store.dispatch(roleManager.fetchAvailableAuthorities(AVAILABLE_AUTHORITIES_UIKEY));
  }

  constructor(props, context) {
    super(props, context);
    this.state = {
      openedAuthorities: new Immutable.Set(),
      filledAuthorities: this.prepareFilledAuthorities(this.props.authorities, this.props.availableAuthorities)
    };
  }

  componentWillReceiveProps(nextProps) {
    const { authorities, availableAuthorities } = nextProps;
    // cursor is different
    if (availableAuthorities) {
      this.setState({
        filledAuthorities: this.prepareFilledAuthorities(authorities, availableAuthorities)
      });
    }
  }

  prepareFilledAuthorities(authorities, availableAuthorities) {
    let filledAuthorities = new Immutable.OrderedMap();
    if (availableAuthorities) {
      // sort authorities by name
      availableAuthorities = _.sortBy(availableAuthorities, function sort(authority) {
        return authority.name;
      });
      const authorityNames = !authorities ? [] : authorities.map(authority => {
        return authority.authority;
      });
      availableAuthorities.forEach(authorityGroup => {
        let permissions = new Immutable.OrderedMap();
        authorityGroup.permissions.forEach(permission => {
          permissions = permissions.set(permission, _.includes(authorityNames, `${authorityGroup.name}_${permission}`));
        });
        filledAuthorities = filledAuthorities.set(authorityGroup.name, permissions);
      });
    }
    return filledAuthorities;
  }

  onPermissionSelect(authorityGroup, permission, event) {
    this.setState({
      filledAuthorities: this.state.filledAuthorities.setIn([authorityGroup, permission], event.currentTarget.checked)
    });
  }

  onAuthorityGroupToogle(authorityGroup, event) {
    if (event) {
      event.preventDefault();
    }
    const { openedAuthorities } = this.state;
    this.setState({
      openedAuthorities: openedAuthorities.has(authorityGroup) ? openedAuthorities.delete(authorityGroup) : openedAuthorities.clear().add(authorityGroup)
    });
  }

  isAllAuthorityGroupSelected(authorityGroup) {
    const { filledAuthorities } = this.state;
    return filledAuthorities.get(authorityGroup).reduce((result, selected) => { return result && selected; }, true);
  }

  isSomeAuthorityGroupSelected(authorityGroup) {
    const { filledAuthorities } = this.state;
    return filledAuthorities.get(authorityGroup).reduce((result, selected) => { return result || selected; }, false);
  }

  onBulkAuthorityGroupSelect(authorityGroup) {
    let { filledAuthorities } = this.state;
    const isSomeSelected = this.isSomeAuthorityGroupSelected(authorityGroup);
    filledAuthorities.get(authorityGroup).forEach((selected, permission) => {
      filledAuthorities = filledAuthorities.setIn([authorityGroup, permission], !isSomeSelected);
    });
    this.setState({
      filledAuthorities
    });
  }

  /**
   * Returns selected authorities as IdmRoleAuthority object ({ target, action})
   *
   * @return {array[object]}
   */
  getSelectedAuthorities() {
    const { filledAuthorities } = this.state;
    //
    const selectedAuthorities = [];
    filledAuthorities.forEach((permissions, authorityGroupName) => {
      permissions.forEach((selected, permission) => {
        if (selected) {
          selectedAuthorities.push({
            target: authorityGroupName,
            action: permission
          });
        }
      });
    });
    return selectedAuthorities;
  }

  render() {
    const { _showLoading, showLoading, disabled, rendered } = this.props;
    const { openedAuthorities, filledAuthorities } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    if (_showLoading || showLoading) {
      return (
        <Basic.Loading showLoading isStatic/>
      );
    }
    //
    return (
      <div>
        {
          !filledAuthorities
          ||
          filledAuthorities.map((permissions, authorityGroupName) => {
            return (
              <div>
                <Basic.Panel style={{ marginBottom: 2 }}>
                  <Basic.PanelHeader style={{ padding: '0 10px 0 0' }}>
                    <div className="pull-left">
                      <Basic.Button
                        level="link"
                        onClick={ this.onBulkAuthorityGroupSelect.bind(this, authorityGroupName) }
                        style={{ color: '#333', textDecoration: 'none' }}
                        title={ this.isSomeAuthorityGroupSelected(authorityGroupName) ? this.i18n('content.roles.setting.authority.select.none') : this.i18n('content.roles.setting.authority.select.all') }
                        titlePlacement="left"
                        titleDelayShow={1000}
                        disabled={disabled}>
                        <Basic.Icon value="fa:check-square-o" rendered={ this.isAllAuthorityGroupSelected(authorityGroupName) }/>
                        <Basic.Icon value="fa:minus-square-o" rendered={ this.isSomeAuthorityGroupSelected(authorityGroupName) && !this.isAllAuthorityGroupSelected(authorityGroupName) }/>
                        <Basic.Icon value="fa:square-o" rendered={ !this.isSomeAuthorityGroupSelected(authorityGroupName) }/>
                        {' '}
                        { authorityGroupName }
                      </Basic.Button>
                    </div>
                    <div className="pull-right">
                      <Basic.Button
                        className="btn-xs"
                        onClick={this.onAuthorityGroupToogle.bind(this, authorityGroupName)}
                        style={{ display: 'inline-block', marginTop: 6 }}
                        title={openedAuthorities.has(authorityGroupName) ? this.i18n('content.roles.setting.authority.group.hide') : this.i18n('content.roles.setting.authority.group.show') }
                        titleDelayShow={ 500 }
                        titlePlacement="bottom">
                        <Basic.Icon value={openedAuthorities.has(authorityGroupName) ? 'fa:angle-double-up' : 'fa:angle-double-down'}/>
                      </Basic.Button>
                    </div>
                    <div className="clearfix"></div>
                  </Basic.PanelHeader>
                  <Basic.Collapse in={openedAuthorities.has(authorityGroupName)}>
                    <Basic.PanelBody style={{ paddingTop: 0, paddingBottom: 0 }}>
                      {
                        permissions.map((selected, permission) => {
                          return (
                            <div className="checkbox">
                              <label>
                                <input
                                  type="checkbox"
                                  onChange={this.onPermissionSelect.bind(this, authorityGroupName, permission)}
                                  style={{ marginBottom: 0 }}
                                  checked={selected}
                                  disabled={disabled}/>
                                  {permission}
                              </label>
                            </div>
                          );
                        })
                      }
                    </Basic.PanelBody>
                  </Basic.Collapse>
                </Basic.Panel>
              </div>
            );
          })
        }
      </div>
    );
  }
}

AuthoritiesPanel.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  authorities: PropTypes.arrayOf(PropTypes.object),
  availableAuthorities: PropTypes.arrayOf(PropTypes.object),
  roleManager: PropTypes.object.isRequired,
  disabled: PropTypes.bool
};

AuthoritiesPanel.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  authorities: [],
  availableAuthorities: [],
  disabled: false
};

function select(state) {
  return {
    availableAuthorities: DataManager.getData(state, AVAILABLE_AUTHORITIES_UIKEY),
    _showLoading: Utils.Ui.isShowLoading(state, AVAILABLE_AUTHORITIES_UIKEY)
  };
}

export default connect(select, null, null, { withRef: true })(AuthoritiesPanel);
