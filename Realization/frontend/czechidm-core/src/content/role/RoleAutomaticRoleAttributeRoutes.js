import React from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { AutomaticRoleAttributeManager } from '../../redux';

const manager = new AutomaticRoleAttributeManager();

/**
 * Default content (routes diff) for automatic roles attribue
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
class RoleAutomaticRoleAttributeRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.automaticRoles';
  }

  componentDidMount() {
    const { automaticRoleId } = this.props.params;
    //
    if (!this._getIsNew()) {
      this.context.store.dispatch(manager.fetchEntityIfNeeded(automaticRoleId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    if (query) {
      return query.new ? true : false;
    }
    return false;
  }

  _changeAutomaticRole() {
    const { entity} = this.props;
    const uuidId = uuid.v1();
    this.context.router.push(`/automatic-role-requests/${uuidId}/new?new=1&roleId=${entity.role}&automaticRoleId=${entity.id}`);
  }

  render() {
    return (
      <div>
        <Basic.ContentHeader>
          <Basic.Icon value="fa:universal-access"/> {this.i18n('content.automaticRoles.attribute.header')}
        </Basic.ContentHeader>

        <Basic.Row>
          <Basic.Col lg={ 6 }>
            <Basic.Alert
              level="warning"
              title={ this.i18n('button.change.header') }
              text={ this.i18n('button.change.text') }
              className="no-margin"
              buttons={[
                <Basic.Button
                  level="warning"
                  onClick={ this._changeAutomaticRole.bind(this) }
                  titlePlacement="bottom">
                  <Basic.Icon type="fa" icon="key"/>
                  {' '}
                  { this.i18n('button.change.label') }
                </Basic.Button>
              ]}/>
          </Basic.Col>
        </Basic.Row>
        <Advanced.TabPanel position="top" parentId="role-automatic-role-attribute" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}

RoleAutomaticRoleAttributeRoutes.propTypes = {
};
RoleAutomaticRoleAttributeRoutes.defaultProps = {
};

function select(state, component) {
  const { automaticRoleId } = component.params;
  //
  return {
    entity: manager.getEntity(state, automaticRoleId)
  };
}

export default connect(select)(RoleAutomaticRoleAttributeRoutes);
