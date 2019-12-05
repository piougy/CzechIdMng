import PropTypes from 'prop-types';
import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Warning icon with incompatible roles in popover content
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class IncompatibleRoleWarning extends Basic.AbstractContextComponent {

  render() {
    const { rendered, showLoading, incompatibleRoles, face } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Icon value="refresh" showLoading />
      );
    }
    if (!incompatibleRoles || incompatibleRoles.length === 0) {
      return null;
    }
    let content = incompatibleRoles.map((incompatibleRole, index) => {
      return (
        <Basic.Panel level="warning" style={ index < incompatibleRoles.length - 1 ? { marginBottom: 5 } : {} }>
          <Basic.PanelHeader>
            { this.i18n('entity.IncompatibleRole._type') }
          </Basic.PanelHeader>

          <Basic.Table
            condensed
            hover={ false }
            noHeader
            data={
              [
                {
                  label: this.i18n('entity.IncompatibleRole.superior.label'),
                  value: (
                    <Advanced.EntityInfo
                      entityType="role"
                      entityIdentifier={ incompatibleRole.incompatibleRole.superior }
                      entity={ incompatibleRole.incompatibleRole._embedded.superior }
                      face="link"
                      showIcon/>
                  )
                },
                {
                  label: this.i18n('entity.IncompatibleRole.sub.label'),
                  value: (
                    <Advanced.EntityInfo
                      entityType="role"
                      entityIdentifier={ incompatibleRole.incompatibleRole.sub }
                      entity={ incompatibleRole.incompatibleRole._embedded.sub }
                      face="link"
                      showIcon/>
                  )
                }
              ]
            }>
            <Basic.Column property="label"/>
            <Basic.Column property="value"/>
          </Basic.Table>
        </Basic.Panel>
      );
    });
    //
    if (face === 'popover') {
      content = (
        <Basic.Popover
          trigger={['click']}
          value={ content }
          className="abstract-entity-info-popover">
          {
            <span>
              <Basic.Button
                level="warning"
                icon="fa:warning"
                className="btn-xs"
                style={{ marginLeft: 3 }}
                title={ this.i18n('entity.IncompatibleRole.sub.label') }/>
            </span>
          }
        </Basic.Popover>
      );
    }
    //
    return (
      <span>
        { content }
      </span>
    );
  }
}

IncompatibleRoleWarning.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Loaded incompatible roles
   */
  incompatibleRoles: PropTypes.array.isRequired,
  /**
   * Decorator
   */
  face: PropTypes.oneOf(['popover', 'full']),
};
IncompatibleRoleWarning.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  face: 'popover'
};
