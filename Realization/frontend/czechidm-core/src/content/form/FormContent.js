import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import { FormDefinitionManager } from '../../redux';
import FormDetail from './FormDetail';

const manager = new FormDefinitionManager();

/**
 * Form content -> detail
 */
class FormContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.setState({
      _showLoading: true
    });
  }

  getContentKey() {
    return 'content.formDefinitions';
  }

  componentDidMount() {
    this.selectNavigationItem('forms');
    const { entityId } = this.props.params;

    this.getLogger().debug(`[FormContent] loading entity detail [id:${entityId}]`);
    this.context.store.dispatch(manager.fetchEntity(entityId), entityId, () => {
      this.setState({
        _showLoading: false
      });
    });
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        {
          !entity
          ||
          <Basic.PageHeader>
            <Basic.Icon value="fa:wpforms"/>
            {' '}
            <span>{entity.name} <small>{this.i18n('edit')}</small></span>
          </Basic.PageHeader>
        }
        <Basic.Panel showLoading={showLoading}>
          {
            !entity
            ||
            <FormDetail entity={entity} definitionManager={manager} showLoading={showLoading} />
          }
        </Basic.Panel>

      </div>
    );
  }
}

FormContent.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
FormContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(FormContent);
