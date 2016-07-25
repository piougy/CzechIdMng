
import React, { Component, PropTypes } from 'react';
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import merge from 'object-assign';
import ApiOperationTypeEnum from '../../../modules/core/enums/ApiOperationTypeEnum';
import Loading from '../Loading/Loading'


class AbstractForm extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.findFormComponentsKeys = this.findFormComponentsKeys.bind(this);
    let componentsKeys = [];
    //We need find our form components keys (only ReactElement) in form
    this.findFormComponentsKeys(this.props.children, componentsKeys, this);
    let mode = this.props.mode ? this.props.mode : ApiOperationTypeEnum.UPDATE;
    const { showLoading } = props;
    this.state = {mode: mode, showLoading:(showLoading != null ? showLoading : true),componentsKeys:componentsKeys};
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.readOnly != null) {
      this.setReadOnly(nextProps.readOnly);
    }
    if (nextProps.disabled != null) {
      this.setDisabled(nextProps.disabled);
    }
    if (nextProps.showLoading && nextProps.showLoading !== this.props.showLoading){
      this.setState({showLoading: nextProps.showLoading});
    }
    if (nextProps.data != null) {
      this.setData(nextProps.data);
    }
  }

  componentDidMount(){
    if (this.props.readOnly != null) {
      this.setReadOnly(this.props.readOnly);
    }
  }

  getFormChildren(){
    return newChildren;
  }

  /**
  * Find all form-components keys and add them to componentsKey array;
  */
  findFormComponentsKeys(children, keys, basicForm){
   React.Children.map(children, function(child) {
     if (!child){
       return null;
     }
     //We will add only AbstractFormComponent
     if (child.type && child.type.prototype instanceof AbstractFormComponent){
       if (child.ref){
         keys.push(child.ref);
       }
       return;
     }else {
       if (child.props && child.props.children && !(child.type && child.type === AbstractForm)){
         basicForm.findFormComponentsKeys(child.props.children, keys, basicForm);
         return;
       }else {
         return;
       }
     }
   });
   return;
 }

  getFooter(){
    //some footer elements. Is override in childe (etc. BasicForm)
  }

  //method for handle json to state
  setData(json, error, operationType){
    if (error){
      this.processEnded(error, operationType);
      return;
    }
    this.setState({allData: merge({},json)});
    if (json){
      for (let componentRef in this.state.componentsKeys) {
        let key = this.state.componentsKeys[componentRef];
        let component = this.getComponent(key);
        if (json.hasOwnProperty(key)) {
          let value = json[key];
          //set new value to component
          component.setValue(value);
        }else {
          component.setValue(null)
        }
      }
    }
    this.processEnded(null, operationType);
  }

  isFormValid(){
      for (let componentRef in this.state.componentsKeys) {
          let component = this.getComponent(this.state.componentsKeys[componentRef]);
          if (!component.isValid()){
              this.showValidationError(true);
              //focus invalid component
              component.focus();
              return false;
          }
      }
      return true;
  }

  showValidationError(show){
    for (let componentRef in this.state.componentsKeys) {
        let component = this.getComponent(this.state.componentsKeys[componentRef]);
        component.setState({showValidationError:show})
    }
  }


  //method for compile data from all component
  getData(){
    let result = merge({},this.state.allData);
    for (let componentRef in this.state.componentsKeys) {
      let key = this.state.componentsKeys[componentRef];
      let component = this.getComponent(this.state.componentsKeys[componentRef]);
      merge(result, {[key]: component.getValue()})
    }
    return result;
  }

  /**
   * Return form component by key
   */
  getComponent(key) {
    if (this.state.componentsKeys){
      let hasKey = false;
      for (let componentRef in this.state.componentsKeys) {
        let k = this.state.componentsKeys[componentRef];
        if (k === key){
          hasKey = true;
        }
      }
      if (!hasKey){
        return null;
      }
      //work around ... I need get instance of react component
      let ownerRefs = this._reactInternalInstance._currentElement._owner._instance.refs;
      return ownerRefs[key];
    }
    return null;
  }

  /**
   * Operation called after asynchrone operation with form is ended.
   * @param  {object} error
   * @param  {Symbol} operationType
   * @return void
   */
  processEnded(error, operationType){
    if (error){
        this.addError(error);
    } else if (ApiOperationTypeEnum.UPDATE === operationType) {
        this.addMessage({title: this.i18n('message.success.update'), level:'success'});
    } else if (ApiOperationTypeEnum.CREATE === operationType) {
        this.addMessage({title: this.i18n('message.success.create'), level:'success'});
    } else if (ApiOperationTypeEnum.DELETE === operationType) {
        this.addMessage({title: this.i18n('message.success.delete'), level:'success'});
    }
    this.setState({showLoading:false});
    this.disableComponents(false);
  }

  processStarted(operationType){
    this.setState({showLoading:true});
    this.disableComponents(true);
  }

  disableComponents(disabled){
    for (let componentRef in this.state.componentsKeys) {
      let component = this.getComponent(this.state.componentsKeys[componentRef]);
      if (component.props.disabled == null || component.props.disabled === false){
        component.setState({disabled: disabled, formDisabled: disabled});
      }else {
        component.setState({formDisabled: disabled});
      }
    }
  }

  setReadOnly(readOnly){
    this.readOnlyComponents(readOnly);
  }

  setDisabled(disabled){
    this.disableComponents(disabled);
  }

  readOnlyComponents(readOnly){
    for (let componentRef in this.state.componentsKeys) {
      let component = this.getComponent(this.state.componentsKeys[componentRef]);
        if (component.props.readOnly == null || component.props.readOnly === false){
          component.setState({readOnly:readOnly, formReadOnly:readOnly});
        }else {
          component.setState({formReadOnly:readOnly});
        }
    }
  }

  render() {
    return (
      <Loading showLoading={this.props.showLoading || this.state.showLoading}>{/* props.showLoading has higher priority */}
        {/* TODO: remove className defs? Somethimes different className e.g. form-inline is needed */}
        <div className={'abstract-form clearfix ' + this.props.className} style={this.props.style}>
          {this.props.children}
        </div>
        {this.getFooter()}
      </Loading>
    );
  }
};

AbstractForm.contextTypes = {
  store: React.PropTypes.object.isRequired,
  router: React.PropTypes.object.isRequired
};

AbstractForm.propTypes = {
  showLoading: React.PropTypes.bool,
  readOnly: React.PropTypes.bool,
  data: React.PropTypes.object
};

export default AbstractForm;
