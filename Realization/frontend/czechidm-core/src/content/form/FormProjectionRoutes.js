import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { FormProjectionManager } from '../../redux';
import FormProjectionDetail from './FormProjectionDetail';

const manager = new FormProjectionManager();

/**
 * Form projection -> detail with vertical menu.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class FormProjectionRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.form-projections';
  }

  componentDidMount() {
    const { entityId } = this.props.match.params;

    if (!this._getIsNew()) {
      this.getLogger().debug(`[FormContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    return !!Utils.Ui.getUrlParameter(this.props.location, 'new');
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Div>
        {
          this._getIsNew()
          ?
          <Helmet title={ this.i18n('create.title') } />
          :
          <Helmet title={ this.i18n('edit.title') } />
        }
        {
          (this._getIsNew() || !entity)
          ||
          <Advanced.DetailHeader
            entity={ entity }
            showLoading={ showLoading }
            back="/forms/form-projections">
            { manager.getNiceLabel(entity)} <small> { this.i18n('edit') }</small>
          </Advanced.DetailHeader>
        }
        {
          this._getIsNew()
          ?
          <FormProjectionDetail isNew match={ this.props.match } />
          :
          <Advanced.TabPanel position="left" parentId="form-projections" match={ this.props.match }>
            { this.getRoutes() }
          </Advanced.TabPanel>
        }

      </Basic.Div>
    );
  }
}

FormProjectionRoutes.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
FormProjectionRoutes.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(FormProjectionRoutes);
