import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import { IdentityContractManager, IdentityManager } from '../../../redux';
import IdentityContractDetail from './IdentityContractDetail';

const manager = new IdentityContractManager();
const identityManager = new IdentityManager();

/**
 * Identity contract's content with detail form.
 *
 * @author Radek Tomiška
 */
class IdentityContractContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.identity-contract.detail';
  }

  componentDidMount() {
    this.selectSidebarItem('identity-contract-detail');
    const { entityId, identityId } = this.props.match.params;
    //
    if (this._isNew()) {
      // load form projection from identity
      this.context.store.dispatch(identityManager.fetchEntity(identityId, null, (entity, error) => {
        if (error) {
          this.addError(error);
        } else {
          // TODO: filter basic fields form definitions only
          this.context.store.dispatch(manager.receiveEntity(entityId, {
            _eav: entity._eav
          }));
        }
      }));
    } else {
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  componentDidUpdate() {
  }

  _isNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading, match } = this.props;
    return (
      <Basic.Row>
        <div className={ this._isNew() ? 'col-lg-offset-1 col-lg-10' : 'col-lg-12' }>
          {
            !entity || showLoading
            ?
            <Basic.Loading isStatic show/>
            :
            <IdentityContractDetail
              uiKey="identity-contract-detail"
              entity={ entity }
              match={ match } />
          }
        </div>
      </Basic.Row>
    );
  }
}
IdentityContractContent.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};

IdentityContractContent.defaultProps = {
  entity: null,
  showLoading: true
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(IdentityContractContent);
