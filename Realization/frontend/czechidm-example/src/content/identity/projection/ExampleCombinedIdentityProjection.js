import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Utils, Managers, Content } from 'czechidm-core';

const identityManager = new Managers.IdentityManager();
const identityProjectionManager = new Managers.IdentityProjectionManager();


/**
 * Example identity form projection - combined different projections for create and edit identity:
 * - create by projection
 * - edit by default product form
 *
 * @author Radek Tomiška
 * @since 10.3.0
 */
class ExampleCombinedIdentityProjection extends Content.AbstractIdentityProjection {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    const { location } = this.props;
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    //
    // redirect to custom new / default edit identity details
    if (!isNew) {
      this.context.history.replace(`/identity/${ encodeURIComponent(entityId) }/profile`);
    } else {
      super.componentDidMount();
    }
  }

  render() {
    const { location } = this.props;
    const { identityProjection } = this.state;
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    //
    return (
      <Basic.Div>
        <Basic.Row>
          <Basic.Div className="col-lg-offset-2 col-lg-8">

            { this.renderHeader(identityProjection, isNew) }

            <form onSubmit={ this.save.bind(this) }>

              {
                !identityProjection
                ?
                <Basic.Panel rendered={ identityProjection === null || identityProjection === undefined }>
                  <Basic.Loading isStatic show/>
                </Basic.Panel>
                :
                <Basic.Panel className="last">

                  <Basic.PanelBody>
                    <Basic.AbstractForm
                      ref="form"
                      data={ identityProjection }
                      readOnly={ !identityProjectionManager.canSave(isNew ? null : identityProjection) }
                      style={{ padding: 0 }}>

                      <Basic.TextField
                        ref="username"
                        label={ this.i18n('identity.username.label') }
                        max={ 255 }
                        readOnly={
                          !identityProjectionManager.canSave(isNew ? null : identityProjection)
                          ||
                          (!isNew && identityProjection && !Utils.Permission.hasPermission(identityProjection._permissions, 'CHANGEUSERNAME'))
                        }
                        required={ !isNew }/>

                    </Basic.AbstractForm>
                  </Basic.PanelBody>
                  <Basic.PanelFooter>
                    { this.renderBackButton() }
                    { this.renderSaveButton() }
                  </Basic.PanelFooter>
                </Basic.Panel>
              }
            </form>
          </Basic.Div>
        </Basic.Row>
      </Basic.Div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.match.params;
  const profileUiKey = identityManager.resolveProfileUiKey(entityId);
  const profile = Managers.DataManager.getData(state, profileUiKey);
  const identityProjection = identityProjectionManager.getEntity(state, entityId);
  //
  return {
    identityProjection,
    showLoading: identityProjectionManager.isShowLoading(state, null, !identityProjection ? entityId : identityProjection.id),
    _imageUrl: profile ? profile.imageUrl : null
  };
}

export default connect(select)(ExampleCombinedIdentityProjection);
