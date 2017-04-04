import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { RoleManager } from '../../redux';
import RoleDetail from './RoleDetail';
import RoleTypeEnum from '../../enums/RoleTypeEnum';

const roleManager = new RoleManager();

/**
 * Role content with role form - first tab on role detail
 *
 * @author Radek Tomiška
 */
class Content extends Basic.AbstractContent {

  constructor(props) {
    super(props);
  }

  getContentKey() {
    return 'content.roles';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['roles', 'role-detail']);
    if (this._isNew()) {
      this.context.store.dispatch(roleManager.receiveEntity(entityId, { roleType: RoleTypeEnum.findKeyBySymbol(RoleTypeEnum.TECHNICAL) }));
    } else {
      this.context.store.dispatch(roleManager.fetchEntity(entityId));
    }
  }

  componentDidUpdate() {
  }

  _isNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { role, showLoading } = this.props;
    return (
      <Basic.Row>
        <div className={this._isNew() ? 'col-lg-offset-1 col-lg-10' : 'col-lg-12'}>
          {
            !role
            ||
            <RoleDetail entity={role} showLoading={showLoading}/>
          }
        </div>
      </Basic.Row>
    );
  }
}
Content.propTypes = {
};

Content.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    role: roleManager.getEntity(state, entityId),
    showLoading: roleManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(Content);
