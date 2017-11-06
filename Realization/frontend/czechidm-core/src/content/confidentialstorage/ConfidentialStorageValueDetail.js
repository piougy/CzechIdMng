import React, { PropTypes } from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { ConfidentialStorageValueManager } from '../../redux';

/**
 * Detail for confidential storage values
 * * owner id
 * * owner type class
 * * key
 * * value
 *
 */
export default class ConfidentialStorageValueDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.confidentialStorageValueManager = new ConfidentialStorageValueManager();
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'content.confidentialStorage';
  }

  componentDidMount() {
    const { entity } = this.props;
    this.selectNavigationItem('confidential-storage');
    this._initForm(entity);
  }

  /**
   * Method check if props in this component is'nt different from new props.
   */
  componentWillReceiveProps(nextProps) {
    // check id of old and new entity
    if (nextProps.entity.id !== this.props.entity.id) {
      this._initForm(nextProps.entity);
    }
  }

  /**
   * Method for basic initial form
   */
  _initForm(entity) {
    if (entity !== undefined) {
      this.refs.form.setData(entity);
    }
  }

  closeDetail() {
  }

  render() {
    const { uiKey} = this.props;
    const { showLoading } = this.state;
    return (
      <div>
          <Basic.AbstractForm
            ref="form"
            uiKey={uiKey}
            readOnly
            style={{ padding: '15px 15px 0 15px' }}>
          <Basic.Row>
            <div className="col-lg-6">
              <Basic.TextField
                ref="ownerId"
                label={this.i18n('entity.ConfidentialStorageValue.ownerId')}
                max={255}/>
            </div>
            <div className="col-lg-6">
              <Basic.TextField
                ref="ownerType"
                label={this.i18n('entity.ConfidentialStorageValue.ownerType')}/>
            </div>
          </Basic.Row>
          <Basic.TextField ref="key" label={this.i18n('entity.ConfidentialStorageValue.key')} />
          <Basic.TextField ref="serializableValue" label={this.i18n('entity.ConfidentialStorageValue.serializableValue')} />
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
          </Basic.PanelFooter>
          {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
          <input type="submit" className="hidden"/>
      </div>
    );
  }
}

ConfidentialStorageValueDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
ConfidentialStorageValueDetail.defaultProps = {
};
