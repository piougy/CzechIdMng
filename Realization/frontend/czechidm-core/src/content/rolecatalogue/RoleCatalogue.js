import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { RoleCatalogueManager } from '../../redux';
import * as Advanced from '../../components/advanced';

const manager = new RoleCatalogueManager();

/**
 * Role's catalogue tab panel
 *
 * @author Roman Kučera
 * @author Radek Tomiška
 */
class RoleCatalogue extends Basic.AbstractContent {

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId, null, (entity, error) => {
      this.handleError(error);
    }));
  }

  render() {
    const { entity, showLoading } = this.props;

    return (
      <Basic.Div>
        <Advanced.DetailHeader
          icon="fa:list-alt"
          entity={ entity }
          showLoading={ showLoading }
          to="/role-catalogues">
          { manager.getNiceLabel(entity)} <small> { this.i18n('content.roleCatalogues.edit.header') }</small>
        </Advanced.DetailHeader>

        <Advanced.TabPanel parentId="role-catalogues" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

RoleCatalogue.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
RoleCatalogue.defaultProps = {
  entity: null,
  showLoading: false
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(RoleCatalogue);
