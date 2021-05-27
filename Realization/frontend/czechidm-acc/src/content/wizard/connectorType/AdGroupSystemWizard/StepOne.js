import {Basic, Domain} from 'czechidm-core';
import React from 'react';
import Joi from 'joi';
import AbstractWizardStep from '../../AbstractWizardStep';
import {SystemMappingManager} from "../../../../redux";

const systemMappingManager = new SystemMappingManager();


/**
 * First step of MS AD connector.
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
export default class StepOne extends AbstractWizardStep {

    constructor(props, context) {
        super(props, context);
        const wizardContext = context.wizardContext;
        this.state.sslSwitch = true;
        this.state.identityMappingSearchParameters = new Domain.SearchParameters()
            .setFilter('operationType', 'PROVISIONING')
            .setFilter("entityType", "IDENTITY")
        const metadata = this.state.connectorType.metadata;
        if (metadata && metadata.sslSwitch !== undefined) {
            this.state.sslSwitch = metadata.sslSwitch === 'true';
        }
        if (metadata && metadata.memberSystemMappingId !== undefined) {
            this.state.memberSystemMappingId = metadata.memberSystemMappingId;
        }
        // If context contains connectorType, then we will used it.
        if (wizardContext && wizardContext.connectorType) {
            if (!wizardContext.connectorType.reopened) {
                //
            }
        }
    }

    /**
     * Prepare metadata for next action (send to the BE).
     */
    compileMetadata(_connectorType, formData, system) {
        const metadata = _connectorType.metadata;
        metadata.system = system ? system.id : null;
        metadata.name = formData.name;
        metadata.port = formData.port;
        metadata.host = formData.host;
        metadata.user = formData.user;
        metadata.password = formData.password;
        metadata.database = formData.database;
        metadata.table = formData.table;
        metadata.keyColumn = formData.keyColumn;
        metadata.sslSwitch = formData.sslSwitch;
        metadata.memberSystemMappingId = formData.memberSystemMappingId;
    }

    /**
     * Is call after execution of the step on backend.
     * Good place for set result to the wizard context.
     */
    afterNextAction(wizardContext, json) {
        wizardContext.entity = json._embedded.system;
        wizardContext.connectorType = json;
    }

    _toggleSslSwitch() {
        const {connectorType} = this.props;
        const value = this.refs.sslSwitch.getValue();
        const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
        if (_connectorType && _connectorType.metadata) {
            _connectorType.metadata.port = !value ? '636' : '389';
        }
        this.setState({
            sslSwitch: !value
        });
    }

    _onChangeMemberSystem(systemMapping) {
        const systemMappingId = systemMapping ? systemMapping.id : null;
        this.setState({
            memberSystemMappingId: systemMappingId
        });
    }

    render() {
        const {connectorType} = this.props;
        const {showLoading, sslSwitch, identityMappingSearchParameters, memberSystemMappingId} = this.state;

        const _connectorType = this.state.connectorType ? this.state.connectorType : connectorType;
        const formData = {};
        if (_connectorType && _connectorType.metadata) {
            const metadata = _connectorType.metadata;
            formData.name = metadata.name;
            formData.port = metadata.port;
            formData.host = metadata.host;
            formData.user = metadata.user;
            formData.sslSwitch = sslSwitch;
            formData.memberSystemMappingId = memberSystemMappingId;
            // Only for a help during testing. 
            // formData.host = 'WIN-E021NJ7TCDQ.kyblicek.piskoviste.bcv';
            // formData.user = 'cn=Administrator,cn=Users,dc=kyblicek,dc=piskoviste,dc=bcv';
            // formData.password = '';
            //
            if (_connectorType.reopened) {
                // We expecting the password was already filled for reopened system.
                formData.password = '********';
            }
        }
        const locKey = this.getLocKey();

        return (
            <Basic.Div showLoading={showLoading}>
                <Basic.AbstractForm
                    ref="form"
                    onSubmit={(event) => {
                        this.wizardNext(event);
                    }}
                    data={formData}>
                    <Basic.TextField
                        ref="name"
                        label={this.i18n(`${locKey}.systemName`)}
                        required
                        max={255}/>
                    <Basic.SelectBox
                        ref="memberSystemMappingId"
                        manager={systemMappingManager}
                        niceLabel={(mapping) => mapping._embedded.objectClass._embedded.system.name}
                        forceSearchParameters={identityMappingSearchParameters}
                        label={this.i18n(`${locKey}.memberSystemMapping.label`)}
                        onChange={this._onChangeMemberSystem.bind(this)}
                        helpBlock={this.i18n(`${locKey}.memberSystemMapping.help`)}/>
                    <Basic.Div rendered={!memberSystemMappingId}>
                        <Basic.ToggleSwitch
                            ref="sslSwitch"
                            label={this.i18n(`${locKey}.sslSwitch.label`)}
                            helpBlock={this.i18n(`${locKey}.sslSwitch.help`)}
                            onChange={this._toggleSslSwitch.bind(this)}
                        />
                        <Basic.Alert
                            title={this.i18n(`${locKey}.sslOffAlert.title`)}
                            text={this.i18n(`${locKey}.sslOffAlert.text`)}
                            showHtmlText
                            rendered={!formData.sslSwitch}
                            level="warning"
                        />
                        <Basic.Div style={{display: 'flex'}}>
                            <Basic.Div style={{flex: 3}}>
                                <Basic.TextField
                                    ref="host"
                                    label={this.i18n(`${locKey}.host.label`)}
                                    helpBlock={this.i18n(`${locKey}.host.help`)}
                                    required
                                    max={128}/>
                            </Basic.Div>
                            <Basic.Div style={{flex: 1, marginLeft: 15}}>
                                <Basic.TextField
                                    ref="port"
                                    validation={Joi.number().integer().min(0).max(65535)}
                                    label={this.i18n(`${locKey}.port.label`)}
                                    helpBlock={this.i18n(`${locKey}.port.help`)}
                                    required/>
                            </Basic.Div>
                        </Basic.Div>
                        <Basic.Div style={{display: 'flex'}}>
                            <Basic.Div style={{flex: 1, marginRight: 15}}>
                                <Basic.TextField
                                    ref="user"
                                    label={this.i18n(`${locKey}.user.label`)}
                                    helpBlock={this.i18n(`${locKey}.user.help`)}
                                    required
                                    max={128}/>
                            </Basic.Div>
                            <Basic.Div style={{flex: 1}}>
                                <Basic.TextField
                                    ref="password"
                                    pwdAutocomplete={false}
                                    required={_connectorType ? !_connectorType.reopened : true}
                                    type="password"
                                    label={this.i18n(`${locKey}.password.label`)}
                                    max={255}/>
                            </Basic.Div>
                        </Basic.Div>
                    </Basic.Div>
                </Basic.AbstractForm>
            </Basic.Div>
        );
    }
}
