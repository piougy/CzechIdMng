import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { FormAttributeManager } from '../../redux';
import FormAttributeDetail from './FormAttributeDetail';

const manager = new FormAttributeManager();

/**
 * Form attribute detail -> detail with vertical menu
 *
 * @author Roman Kučera
 * @author Radek Tomiška
 */
class FormAttributeRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.formAttributes';
  }

  componentDidMount() {
    super.componentDidMount();
    //
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
    const { query } = this.props.location;
    if (query) {
      return !!query.new;
    }
    return false;
  }

  _getFormDefinitionId() {
    const { query } = this.props.location;
    return (query) ? query.formDefinition : null;
  }

  render() {
    const { entity, showLoading } = this.props;
    //
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
            back={ `/form-definitions/${ entity ? entity.formDefinition : this._getFormDefinitionId() }/attributes` }>
            { manager.getNiceLabel(entity)} <small> { this.i18n('edit.title') }</small>
          </Advanced.DetailHeader>
        }
        {
          this._getIsNew()
          ?
          <FormAttributeDetail isNew formDefinition={ this._getFormDefinitionId() } match={ this.props.match } />
          :
          <Advanced.TabPanel position="left" parentId="forms-attributes" match={ this.props.match }>
            { this.getRoutes() }
          </Advanced.TabPanel>
        }
      </Basic.Div>
    );
  }
}

FormAttributeRoutes.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
FormAttributeRoutes.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(FormAttributeRoutes);
