import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import IdentityDetail from './IdentityDetail';

const identityManager = new IdentityManager();

/**
 * Identity detail route
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

  componentWillMount() {
    this.setState({
      showLoading: true
    });
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['identity-profile', 'profile-personal']);
    this.context.store.dispatch(identityManager.fetchEntity(entityId, null, (entity, error) => {
      this.handleError(error);
    }));
  }

  render() {
    const { identity } = this.props;
    const { entityId } = this.props.params;
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
  const { entityId } = component.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(Profile);
