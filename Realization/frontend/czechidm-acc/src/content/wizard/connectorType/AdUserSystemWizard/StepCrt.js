import { Basic, Services } from 'czechidm-core';
import React from 'react';
import moment from 'moment';
import AbstractWizardStep from '../../AbstractWizardStep';

/**
 * Second step of MS AD connector.
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
export default class StepCrt extends AbstractWizardStep {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
    if (wizardContext && wizardContext.wizardForceUpdate) {
      // Workaround: Next button can be hidden and we need to call force update.
      wizardContext.wizardForceUpdate();
    }
  }

  wizardNext(event) {
    if (event) {
      event.preventDefault();
    }
    const wizardContext = this.context.wizardContext;
    if (wizardContext.callBackNext) {
      wizardContext.callBackNext();
    } else if (wizardContext.onClickNext) {
      wizardContext.onClickNext(false, true);
    }
  }

  /**
   * Is call after execution of the step on backend.
   * Good place for set result to the wizard context.
   */
  afterNextAction(wizardContext, json) {
    wizardContext.entity = json._embedded.system;
    wizardContext.connectorType = json;
  }

  /**
   * Download certificate
   *
   * @param  {string} attachmentId
   * @return {string} url
   */
  getDownloadCertificateUrl(attachmentId) {
    return Services.RestApiService.getUrl(
      `${this.props.apiPath}/${encodeURIComponent(attachmentId)}/download?cidmst=${Services.AuthenticateService.getTokenCIDMST()}`
    );
  }

  formatDateTime(date) {
    return moment(date).format(this.i18n('format.datetime'));
  }

  render() {
    const {connectorType} = this.props;
    const {showLoading} = this.state;

    const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
    const formData = {};
    if (_connectorType && _connectorType.metadata) {
      const metadata = _connectorType.metadata;
      formData.attachmentId = metadata.attachmentId;
      formData.subjectDN = metadata.subjectDN;
      formData.hasTrustedCa = metadata.hasTrustedCa === 'true';
      formData.fingerPrint = metadata.fingerPrint;
      formData.crtValidityFrom = this.formatDateTime(metadata.crtValidityFrom);
      formData.crtValidityTill = this.formatDateTime(metadata.crtValidityTill);
      formData.crtFilePath = metadata.crtFilePath;
      formData.serverAttachmentId = metadata.serverAttachmentId;
      formData.serverSubjectDN = metadata.serverSubjectDN;
      formData.serverFingerPrint = metadata.serverFingerPrint;
      formData.serverCrtValidityFrom = this.formatDateTime(metadata.serverCrtValidityFrom);
      formData.serverCrtValidityTill = this.formatDateTime(metadata.serverCrtValidityTill);
      formData.sslSwitch = metadata.sslSwitch === 'true';
    }
    const locKey = this.getLocKey();
    if (!formData.sslSwitch) {
      return (
        <Basic.Div>
          <Basic.Alert
            style={{marginLeft: 25, marginRight: 25}}
            title={this.i18n(`${locKey}.sslOffAlert.title`)}
            text={this.i18n(`${locKey}.sslOffAlert.text`)}
            showHtmlText
            level="warning"
          />
          <Basic.Alert
            style={{marginLeft: 25, marginRight: 25}}
            title={this.i18n(`${locKey}.youCanContinue.title`)}
            text=""
            showHtmlText
            level="info"
          />
        </Basic.Div>
      );
    }

    return (
      <Basic.Div showLoading={showLoading}>
        <Basic.Alert
          title={this.i18n(`${locKey}.noTrustedCertificate.title`)}
          text={this.i18n(`${locKey}.noTrustedCertificate.text`)}
          showHtmlText
          rendered={!formData.hasTrustedCa}
          level="danger"
        />
        <Basic.Alert
          title={this.i18n(`${locKey}.trustedCertificate.title`)}
          text={this.i18n(`${locKey}.trustedCertificate.text`)}
          showHtmlText
          rendered={!!formData.hasTrustedCa}
          level="info"
        />
        <Basic.Alert
          title={this.i18n(`${locKey}.certificate.title`)}
          text={this.i18n(`${locKey}.certificate.text`, {
            subjectDN: formData.subjectDN,
            fingerPrint: formData.fingerPrint,
            crtValidityFrom: formData.crtValidityFrom,
            crtValidityTill: formData.crtValidityTill,
            certificatePath: formData.crtFilePath
          })}
          showHtmlText
          level="success"
          buttons={
            [
              <a
                href={this.getDownloadCertificateUrl(formData.attachmentId)}
                title={this.i18n(`${locKey}.downloadCertificate.title`)}
                style={{ color: 'white' }}
                className="btn btn-success">
                <Basic.Icon value="fa:certificate"/>
                {' '}
                {this.i18n(this.i18n(`${locKey}.downloadCertificate.label`))}
              </a>
            ]
          }
        />
        <Basic.Alert
          title={this.i18n(`${locKey}.serverCertificate.title`)}
          text={this.i18n(`${locKey}.serverCertificate.text`, {
            subjectDN: formData.serverSubjectDN,
            fingerPrint: formData.serverFingerPrint,
            crtValidityFrom: formData.serverCrtValidityFrom,
            crtValidityTill: formData.serverCrtValidityTill
          })}
          showHtmlText
          level="info"
          buttons={
            [
              <a
                href={this.getDownloadCertificateUrl(formData.serverAttachmentId)}
                title={this.i18n(`${locKey}.downloadCertificate.title`)}
                style={{ color: 'white' }}
                className="btn btn-info">
                <Basic.Icon value="fa:certificate"/>
                {' '}
                {this.i18n(this.i18n(`${locKey}.downloadCertificate.label`))}
              </a>
            ]
          }
        />
      </Basic.Div>
    );
  }
}
