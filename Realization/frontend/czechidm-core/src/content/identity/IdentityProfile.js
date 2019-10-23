import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import IdentityDetail from './IdentityDetail';

const identityManager = new IdentityManager();

/**
 * Identity detail route
 * - full identity profile ~ detail
 *
 * @author Radek Tomiška
 */
class Profile extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      showLoading: false,
      showLoadingIdentityTrimmed: false,
    };
  }

  getContentKey() {
    return 'content.identity.profile';
  }

  getNavigationKey() {
    return 'profile-personal';
  }

  UNSAFE_componentWillMount() {
    this.setState({
      showLoading: true
    });
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    this.context.store.dispatch(identityManager.fetchEntity(entityId, null, (entity, error) => {
      this.handleError(error);
    }));
  }

  render() {
    const { identity } = this.props;
    const { entityId } = this.props.match.params;
    return (
      <div className="tab-pane-text-body">
        <IdentityDetail identity={identity} entityId={entityId} />
      </div>
    );
  }
}

Profile.propTypes = {
};

Profile.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(Profile);
