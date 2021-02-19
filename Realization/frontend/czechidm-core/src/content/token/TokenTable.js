import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Domain from '../../domain';
import * as Advanced from '../../components/advanced';
import { TokenManager } from '../../redux';
//
import AuthoritiesPanel from '../role/AuthoritiesPanel';
import TokenTypeEnum from '../../enums/TokenTypeEnum';
import EnumValueDecorator from '../../components/basic/EnumSelectBox/EnumValueDecorator';
import EnumOptionDecorator from '../../components/basic/EnumSelectBox/EnumOptionDecorator';

const manager = new TokenManager(); // default manager
/**
 * Token table.
 *
 * TODO: identity is supported as newly added token owner only.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export class TokenTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false,
        entity: {}
      },
      token: null,
      filterOpened: props.filterOpened
    };
  }

  getManager() {
    return this.props.manager;
  }

  getContentKey() {
    return 'content.tokens';
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  /**
   * Recive new form for create new type else show detail for existing org.
   */
  showDetail(entity, event) {
    const { uiKey, forceSearchParameters } = this.props;
    //
    if (event) {
      event.preventDefault();
    }
    //
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${ uiKey }-detail`));
    } else if (forceSearchParameters
      && forceSearchParameters.getFilters().has('ownerId')
      && forceSearchParameters.getFilters().has('ownerType')
      && forceSearchParameters.getFilters().get('ownerType') === 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity') {
      entity.identityId = forceSearchParameters.getFilters().get('ownerId');
    }
    //
    super.showDetail(entity);
  }

  closeDetail() {
    const { detail } = this.state;
    //
    this.setState({
      detail: {
        ...detail,
        show: false
      },
      token: null
    });
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    //
    const entity = this.refs.form.getData();
    entity.ownerType = 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity';
    entity.ownerId = entity.identityId;
    //
    this.context.store.dispatch(this.getManager().createEntity(entity, `${ this.getUiKey() }-detail`, (createdEntity, error) => {
      if (error) {
        this.addError(error);
        return;
      }
      this.setState({
        token: createdEntity.properties.cidmst
      }, () => {
        this.showDetail(createdEntity);
        this.refs.table.reload();
      });
    }));
  }

  getHelp() {
    let helpContent = new Domain.HelpContent();
    helpContent = helpContent.setHeader(this.i18n('content.identity.authorities.help.header'));
    helpContent = helpContent.setBody(
      <Basic.Div>
        <Basic.Div>
          { this.i18n('content.identity.authorities.help.body.title', { escape: false }) }
        </Basic.Div>
        <Basic.Div style={{ marginTop: 15 }}>
          { this.i18n('content.identity.authorities.help.body.checkbox.title', { escape: false }) }
        </Basic.Div>
        <ul>
          <li className="hidden">
            <Basic.Icon value="fa:square-o" style={{ marginRight: 5 }}/>
            { this.i18n('content.identity.authorities.help.body.checkbox.none', { escape: false }) }
          </li>
          <li>
            <Basic.Icon value="fa:minus-square-o" style={{ marginRight: 5 }}/>
            { this.i18n('content.identity.authorities.help.body.checkbox.some', { escape: false }) }
          </li>
          <li>
            <Basic.Icon value="fa:check-square-o" style={{ marginRight: 5 }}/>
            { this.i18n('content.identity.authorities.help.body.checkbox.all', { escape: false }) }
          </li>
        </ul>
      </Basic.Div>
    );
    //
    return helpContent;
  }

  render() {
    const {
      uiKey,
      columns,
      forceSearchParameters,
      showAddButton,
      className,
      _showLoading,
      _permissions
    } = this.props;
    const { filterOpened, detail, token } = this.state;

    let tokenTypeHelp = this.i18n('entity.Token.tokenType.help');
    if (detail.entity.tokenType === 'CIDMST') {
      tokenTypeHelp = this.i18n('core:enums.TokenTypeEnum.helpBlock.CIDMST');
    } else if (detail.entity.tokenType === 'SYSTEM') {
      tokenTypeHelp = this.i18n('core:enums.TokenTypeEnum.helpBlock.SYSTEM');
    }
    //
    return (
      <Basic.Div>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ this.getManager() }
          className={ className }
          rowClass={ ({rowIndex, data}) => {
            const entity = data[rowIndex];
            entity.validFrom = entity.issuedAt;
            entity.validTill = entity.expiration;
            //
            return !entity.secretVerified ? 'disabled' : Utils.Ui.getRowClass(entity);
          }}
          showRowSelection
          forceSearchParameters={ forceSearchParameters }
          filterOpened={ filterOpened }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add-button"
                className="btn-xs"
                onClick={ this.showDetail.bind(this, { tokenType: 'SYSTEM' }) }
                rendered={ manager.canSave() && showAddButton }
                icon="fa:plus">
                { this.i18n('button.add.label') }
              </Basic.Button>
            ]
          }
          filter={
            <Filter
              ref="filterForm"
              onSubmit={ this.useFilter.bind(this) }
              onCancel={ this.cancelFilter.bind(this) }/>
          }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            width={ 16 }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                entity.validFrom = entity.issuedAt;
                entity.validTill = entity.expiration;
                const disabled = Utils.Entity.isDisabled(entity) || !Utils.Entity.isValid(entity) || !entity.secretVerified;
                //
                return (
                  <Basic.Div style={{ marginTop: 7, width: 10, height: 10, backgroundColor: disabled ? '#d9534f' : '#28a946', borderRadius: 5 }} />
                );
              }
            }/>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ this.showDetail.bind(this, data[rowIndex]) } />
                );
              }
            }/>
          <Advanced.Column
            property="issuedAt"
            width={ 150 }
            sort
            face="datetime"
            rendered={ _.includes(columns, 'issuedAt') }/>
          <Advanced.Column
            property="expiration"
            width={ 150 }
            sort
            face="datetime"
            rendered={ _.includes(columns, 'expiration') }/>
          <Advanced.Column
            property="ownerId"
            header={ this.i18n('entity.Token.owner.label') }
            title={ this.i18n('entity.Token.owner.title') }
            rendered={ _.includes(columns, 'ownerId') }
            cell={
              ({ rowIndex, data, property }) => {
                //
                if (!data[rowIndex]._embedded || !data[rowIndex]._embedded[property]) {
                  return (
                    <Advanced.UuidInfo value={ data[rowIndex][property] } />
                  );
                }
                //
                return (
                  <Advanced.EntityInfo
                    entityType={ Utils.Ui.getSimpleJavaType(data[rowIndex].ownerType) }
                    entityIdentifier={ data[rowIndex][property] }
                    entity={ data[rowIndex]._embedded[property] }
                    face="popover"
                    showEntityType={ false }
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            property="tokenType"
            width={ 150 }
            sort
            rendered={ _.includes(columns, 'tokenType') }
            cell={
              ({ rowIndex, data, property }) => {
                const tokenType = data[rowIndex][property];
                if (tokenType !== 'CIDMST' && tokenType !== 'SYSTEM') {
                  return <Basic.Label value={ tokenType } />;
                }
                //
                return (
                  <Basic.EnumValue
                    value={ tokenType }
                    enum={ TokenTypeEnum }
                    title={ this.i18n(`core:enums.TokenTypeEnum.helpBlock.${ tokenType }`) }/>
                );
              }
            }/>
        </Advanced.Table>
        <Basic.Modal
          bsSize="large"
          show={ detail.show }
          onHide={ this.closeDetail.bind(this) }
          backdrop="static"
          keyboard={ !_showLoading }>

          <form onSubmit={ this.save.bind(this) }>
            <Basic.Modal.Header
              closeButton={ !_showLoading }
              text={ this.i18n('create.header') }
              rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              closeButton={ !_showLoading }
              text={ this.i18n('edit.header') }
              rendered={ !Utils.Entity.isNew(detail.entity) }
              buttons={[
                <Advanced.AuditableInfo entity={ detail.entity }/>
              ]}/>
            <Basic.Modal.Body>
              <Basic.Alert level="info" rendered={ token !== null } text={ this.i18n('create.success', { escape: false })}/>
              <Basic.Alert
                title="Token"
                level="success"
                rendered={ token !== null }
                buttons={[
                  <Basic.Button
                    icon="fa:clipboard"
                    onClick={ () => {
                      // ~ctrl+c
                      this.refs['created-token'].select();
                      document.execCommand('copy');
                      this.addMessage({ level: 'success', message: this.i18n('button.copy.success') });
                    }}>
                    { this.i18n('button.copy.label') }
                  </Basic.Button>
                ]}>
                <textarea
                  ref="created-token"
                  style={{ wordBreak: 'break-all', width: '100%', border: 'none', background: 'transparent', resize: 'none' }}
                  readOnly
                  rows={ 6 }
                  value={ token }/>
              </Basic.Alert>

              <Basic.AbstractForm
                ref="form"
                readOnly={ !manager.canSave(detail.entity, _permissions) }
                showLoading={ _showLoading }>

                <Basic.Div rendered={ Utils.Entity.isNew(detail.entity) }>
                  <Basic.Alert level="info" text={ this.i18n('create.help', { escape: false }) }/>

                  <Advanced.IdentitySelect
                    ref="identityId"
                    label={ this.i18n('entity.Token.owner.label') }
                    helpBlock={ this.i18n('entity.Token.owner.help') }
                    required
                    readOnly={ Utils.Ui.isNotEmpty(detail.entity.identityId) }/>

                  <Basic.DateTimePicker
                    mode="datetime"
                    ref="expiration"
                    label={ this.i18n('entity.Token.expiration.label') }
                    helpBlock={ this.i18n('entity.Token.expiration.help') }/>
                </Basic.Div>

                <Basic.Div rendered={ !Utils.Entity.isNew(detail.entity) }>
                  <Basic.Row>
                    <Basic.Col lg={ 6 }>
                      <Basic.ContentHeader
                        icon="component:basic-tab"
                        text={ this.i18n('content.scheduler.all-tasks.tabs.basic') }
                        style={{ paddingTop: 0 }} />
                      <Basic.AbstractForm
                        ref="form"
                        data={ detail.entity }
                        readOnly>
                        <Basic.DateTimePicker
                          mode="datetime"
                          ref="issuedAt"
                          label={ this.i18n('entity.Token.issuedAt.label') }
                          helpBlock={ this.i18n('entity.Token.issuedAt.help') }/>
                        <Basic.DateTimePicker
                          mode="datetime"
                          ref="expiration"
                          label={ this.i18n('entity.Token.expiration.label') }
                          helpBlock={ this.i18n('entity.Token.expiration.help') }/>
                        <Basic.LabelWrapper
                          label={ this.i18n('entity.Token.owner.label') }
                          helpBlock={ this.i18n('entity.Token.owner.help') }>
                          <Advanced.EntityInfo
                            entityType={ Utils.Ui.getSimpleJavaType(detail.entity.ownerType) }
                            entityIdentifier={ detail.entity.ownerId }
                            entity={ detail.entity._embedded ? detail.entity._embedded.ownerId : null }
                            face="popover"
                            showEntityType={ false }
                            showIcon/>
                        </Basic.LabelWrapper>
                        <Basic.TextField
                          ref="tokenType"
                          label={ this.i18n('entity.Token.tokenType.label') }
                          helpBlock={ tokenTypeHelp }/>
                        <Basic.TextField
                          ref="moduleId"
                          label={ this.i18n('entity.Token.moduleId.label') }
                          helpBlock={ this.i18n('entity.Token.moduleId.help') }/>
                        <Basic.Checkbox
                          ref="disabled"
                          label={ this.i18n('entity.Token.disabled.label') }
                          helpBlock={ this.i18n('entity.Token.disabled.help') }/>
                        <Basic.Checkbox
                          ref="secretVerified"
                          label={ this.i18n('entity.Token.secretVerified.label') }
                          helpBlock={ this.i18n('entity.Token.secretVerified.help') }/>
                      </Basic.AbstractForm>
                    </Basic.Col>
                    <Basic.Col lg={ 6 }>
                      <Basic.ContentHeader
                        icon="fa:shield-alt"
                        text={ this.i18n('content.identity.authorities.label') }
                        help={ this.getHelp() }
                        style={{ paddingTop: 0 }}/>
                      {
                        !detail.entity.properties
                        ||
                        <AuthoritiesPanel authorities={ detail.entity.properties.authorities }/>
                      }
                    </Basic.Col>
                  </Basic.Row>
                  <Basic.TextField
                    value={ detail.entity.token }
                    label={ this.i18n('entity.Token.token.label') }
                    helpBlock={ this.i18n('entity.Token.token.help') }
                    readOnly/>
                </Basic.Div>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ _showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                rendered={ Utils.Entity.isNew(detail.entity) && this.getManager().canSave(detail.entity, _permissions) }
                showLoading={ _showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }>
                { this.i18n('button.generate.label') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}

TokenTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  /**
   * Rendered columns - see table columns above
   *
   * TODO: move to advanced table and add column sorting
   */
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Filter is opened / closed by default.
   */
  filterOpened: PropTypes.bool,
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};

TokenTable.defaultProps = {
  manager,
  columns: ['issuedAt', 'expiration', 'ownerType', 'ownerId', 'tokenType'],
  filterOpened: true,
  _showLoading: false,
  _permissions: null
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { forwardRef: true })(TokenTable);

/**
 * Table filter component.
 *
 * @author Radek Tomiška
 */
class Filter extends Advanced.Filter {

  focus() {
    this.refs.text.focus();
  }

  render() {
    const { onSubmit, onCancel } = this.props;
    //
    return (
      <Advanced.Filter onSubmit={ onSubmit }>
        <Basic.AbstractForm ref="filterForm">
          <Basic.Row>
            <Basic.Col lg={ 8 }>
              <Advanced.Filter.FilterDate
                ref="fromTill"
                fromProperty="createdFrom"
                tillProperty="createdTill"/>
            </Basic.Col>
            <Basic.Col lg={ 4 } className="text-right">
              <Advanced.Filter.FilterButtons cancelFilter={ onCancel }/>
            </Basic.Col>
          </Basic.Row>
          <Basic.Row className="last">
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.CreatableSelectBox
                ref="tokenType"
                emptyOptionLabel={ false }
                placeholder={ this.i18n('entity.Token.tokenType.title') }
                options={[
                  {
                    niceLabel: TokenTypeEnum.getNiceLabel(
                      TokenTypeEnum.findKeyBySymbol(TokenTypeEnum.CIDMST)
                    ),
                    _iconKey: 'component:identity',
                    value: TokenTypeEnum.findKeyBySymbol(TokenTypeEnum.CIDMST),
                    description: this.i18n('core:enums.TokenTypeEnum.helpBlock.CIDMST')
                  },
                  {
                    niceLabel: TokenTypeEnum.getNiceLabel(
                      TokenTypeEnum.findKeyBySymbol(TokenTypeEnum.SYSTEM)
                    ),
                    _iconKey: TokenTypeEnum.getIcon(TokenTypeEnum.findKeyBySymbol(TokenTypeEnum.SYSTEM)),
                    value: TokenTypeEnum.findKeyBySymbol(TokenTypeEnum.SYSTEM),
                    description: this.i18n('core:enums.TokenTypeEnum.helpBlock.SYSTEM')
                  },
                ]}
                useSymbol={ false }
                optionComponent={ EnumOptionDecorator }
                valueComponent={ EnumValueDecorator }/>
            </Basic.Col>
            <Basic.Col lg={ 8 }>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }
}
