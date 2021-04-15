import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Advanced } from 'czechidm-core';
import { UniformPasswordManager } from '../../redux';
//
const manager = new UniformPasswordManager();

/**
 * Uniform password detail with menu
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 */
class UniformPassword extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Div>
        <Advanced.DetailHeader
          icon="fa:key"
          entity={ entity }
          showLoading={ !entity && showLoading }
          back="/uniform-password">
          { manager.getNiceLabel(entity) } <small> { this.i18n('acc:content.uniformPassword.edit.header') }</small>
        </Advanced.DetailHeader>

        <Advanced.TabPanel parentId="uniform-password" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

UniformPassword.propTypes = {
};
UniformPassword.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(UniformPassword);
