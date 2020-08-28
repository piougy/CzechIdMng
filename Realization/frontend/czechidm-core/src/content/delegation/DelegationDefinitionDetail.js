import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import moment from 'moment';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { DelegationDefinitionManager, DataManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';

const manager = new DelegationDefinitionManager();

/**
* Delegation definition detail.
*
* @author Vít Švanda
* @since 10.4.0
*/
class DelegationDefinitionDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {};
    //
    const { delegationId } = props.match.params;
    const { isNew, entity } = props;
    // Get delegator from a URL.
    const delegator = Utils.Ui.getUrlParameter(this.props.location, 'delegatorId');
    if (delegator) {
      this.state = _.merge(this.state, {delegatorMode: true});
    }
    //
    if (isNew) {
      this.state = _.merge(this.state, this._initDefinition(entity ||
        {
          delegator,
          validFrom: moment(),
          type: 'default-delegation-type'
        }));
    } else {
      this.context.store.dispatch(manager.fetchEntity(delegationId, null, (definition) => {
        this.state = _.merge(this.state, this._initDefinition(definition));
      }));
    }
    this.context.store.dispatch(manager.fetchSupportedTypes());
  }

  /**
   * Returns true, if is component included in under other route (for example is tab on identity).
   */
  isIncluded() {
    // Entity ID indicates ID of identity ... component is inculded.
    return !!this.props.match.params.entityId;
  }

  getContentKey() {
    return 'content.delegation-definitions';
  }

  componentDidMount() {
    super.componentDidMount();
    this.refs.type.focus();
  }

  _initDefinition(definition) {
    return {
      definition,
      selectedDelegator: definition ? definition.delegator : null
    };
  }

  getNavigationKey() {
    if (this.isIncluded()) {
      return 'identity-delegation-definition-detail';
    }
    return 'delegation-definition-detail';
  }

  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.refs.form.processStarted();
    const entity = this.refs.form.getData();
    // save
    if (entity.id === undefined) {
      this.context.store.dispatch(manager.createEntity(entity, `${ uiKey }-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(manager.updateEntity(entity, `${ uiKey }-detail`, this._afterSave.bind(this)));
    }
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    const { isNew } = this.props;
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    //
    this.refs.form.processEnded();
    this.addMessage({ message: this.i18n('save.success', { code: entity.code }) });
    if (isNew) {
      this.context.history.goBack();
    }
  }

  _getSupportedTypes() {
    const { supportedTypes } = this.props;
    const { definition } = this.state;

    let localSupportedTypes = null;
    const _supportedTypes = [];
    if (supportedTypes) {
      // Clone the array.
      localSupportedTypes = supportedTypes.slice(0);
    } else if (definition && definition.type && definition._embedded && definition._embedded.delegationType) {
      // If delegation types are not loaded, but definition yes, then we will use the type from definition as a default option.
      localSupportedTypes = [definition._embedded.delegationType];
    } else {
      // If delegation types are not loaded, then we will create default value.
      localSupportedTypes = [{id: 'default-delegation-type', module: 'core', supportsDelegatorContract: false}];
    }

    localSupportedTypes.forEach(type => {
      _supportedTypes.push(this._toDefinitionTypeOption(type));
    });

    return _supportedTypes;
  }

  _toDefinitionTypeOption(type) {
    const localizationKey = `${type.module}:content.delegation-definitions.types.${type.id}`;

    return {
      localizationKey,
      niceLabel: this.i18n(`${localizationKey}.label`, type.id),
      description: this.i18n(`${localizationKey}.info.text`, ''),
      value: type.id,
      disabled: !type.canBeCreatedManually,
      supportsDelegatorContract: type.supportsDelegatorContract
    };
  }

  onChangeDelegationType(type) {
    this.setState({
      type
    });
  }

  _onChangeDelegator(value) {
    this.setState({selectedDelegator: value ? value.id : null});
    return true;
  }

  render() {
    const { uiKey, showLoading, _permissions } = this.props;
    const { definition, selectedDelegator, type, delegatorMode } = this.state;
    //
    const _supportedTypes = this._getSupportedTypes();
    let _selectedType = type;
    if (!_selectedType && definition) {
      _selectedType = _supportedTypes.filter((supportsType) => supportsType.value === definition.type);
      if (_selectedType.length > 0) {
        _selectedType = _selectedType[0];
      }
    }

    const includedStyle = this.isIncluded() ? { paddingRight: 15, paddingLeft: 15, paddingTop: 0 } : { padding: 0 };
    const displayDelegatorMode = delegatorMode ? 'none' : '';

    return (
      <form onSubmit={ this.save.bind(this) }>
        <Basic.Panel className={ Utils.Entity.isNew(definition) ? '' : 'no-border last' }>
          <Basic.PanelHeader
            rendered={!this.isIncluded()}
            style={ this.isIncluded() ? { paddingRight: 15, paddingLeft: 15 } : {}}
            text={
              Utils.Entity.isNew(definition)
              ?
              this.i18n('create.header')
              :
              this.i18n('content.delegation-definitions.detail.title')
            }/>
          <Basic.PanelBody
            style={ Utils.Entity.isNew(definition) ? { paddingTop: 0, paddingBottom: 0 } : includedStyle}>
            <Basic.AbstractForm
              ref="form"
              uiKey={ uiKey }
              data={ definition }
              readOnly={ !manager.canSave(definition, _permissions) }>
              <Basic.EnumSelectBox
                ref="type"
                options={ _supportedTypes }
                onChange={ this.onChangeDelegationType.bind(this) }
                style={{maxWidth: 500}}
                label={ this.i18n('entity.DelegationDefinition.type.label') }
                helpBlock={_selectedType ? _selectedType.description : ''}
                clearable={ false }
                required/>
              <Advanced.IdentitySelect
                ref="delegator"
                style={{maxWidth: 500, display: displayDelegatorMode}}
                readOnly={!!delegatorMode || !manager.canSave(definition, _permissions)}
                rendered={!!_selectedType}
                onChange={ this._onChangeDelegator.bind(this) }
                label={ this.i18n('entity.DelegationDefinition.delegator.label') }
                disableable={ false }
                required/>
              <Advanced.EntitySelectBox
                ref="delegatorContract"
                rendered={!!selectedDelegator && _selectedType && !!_selectedType.supportsDelegatorContract}
                entityType="identityContract"
                useFirst
                readOnly={!manager.canSave(definition, _permissions)}
                forceSearchParameters={
                  new SearchParameters()
                    .setFilter('identity', selectedDelegator)
                }
                style={{maxWidth: 500}}
                label={ this.i18n('entity.DelegationDefinition.delegatorContract.label') }
                required/>
              <Advanced.IdentitySelect
                ref="delegate"
                rendered={!!_selectedType}
                readOnly={!manager.canSave(definition, _permissions)}
                style={{maxWidth: 500}}
                label={ this.i18n('entity.DelegationDefinition.delegate.label') }
                required/>
              <Basic.DateTimePicker
                mode="date"
                ref="validFrom"
                label={ this.i18n('label.validFrom') }/>
              <Basic.DateTimePicker
                mode="date"
                ref="validTill"
                label={ this.i18n('label.validTill') }/>
              <Basic.TextArea
                ref="description"
                label={ this.i18n('entity.DelegationDefinition.description.label') }
                rows={ 4 }
                max={ 1000 }/>
            </Basic.AbstractForm>
          </Basic.PanelBody>
          <Basic.PanelFooter showLoading={ showLoading } >
            <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
              { this.i18n('button.back') }
            </Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoading={ showLoading }
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }
              rendered={ manager.canSave(definition, _permissions) }>
              { this.i18n('button.save') }
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </form>
    );
  }
}

DelegationDefinitionDetail.propTypes = {
  uiKey: PropTypes.string,
  definitionManager: PropTypes.object,
  isNew: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
DelegationDefinitionDetail.defaultProps = {
  isNew: false,
  _permissions: null
};

function select(state, component) {
  const { delegationId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, delegationId),
    showLoading: manager.isShowLoading(state, null, delegationId),
    _permissions: manager.getPermissions(state, null, delegationId),
    supportedTypes: DataManager.getData(state, DelegationDefinitionManager.UI_KEY_SUPPORTED_TYPES)
  };
}

export default connect(select)(DelegationDefinitionDetail);
