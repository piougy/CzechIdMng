import React, { Component, PropTypes } from 'react';
import AbstractForm from '../AbstractForm/AbstractForm';
import TextField from '../TextField/TextField';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Button from '../Button/Button';
import ApiOperationTypeEnum from '../../../modules/core/enums/ApiOperationTypeEnum';
import merge from 'object-assign';


class BasicForm extends AbstractForm {

  constructor(props) {
     super(props);
     this.save = this.save.bind(this);
     let mode = this.props.mode ? this.props.mode : ApiOperationTypeEnum.UPDATE;
     this.state = merge(this.state, {disableBtns: false, mode: mode, loading:true});
     this.processEnded = this.processEnded.bind(this);
   }


  //operation called after asynchrone operation with form is ended.
  processEnded(error, operationType){
      super.processEnded(error, operationType)
      this.setState({disableBtns: false});
  }

  processStarted(operationType){
    super.processStarted(operationType);
    this.setState({disableBtns: true});
  }

  save(e){
    if (!this.isFormValid()){
      return;
    }
    this.processStarted();
    this.props.saveFunc(this.getData());
  }


  getFooter(){
    let modifyBtn =  this.state.mode === ApiOperationTypeEnum.UPDATE ?
    <Button type="button" level="success" disabled = {this.state.disableBtns} onClick={this.save}>{this.i18n('button.save')}</Button> :
    <Button type="button" level="success" disabled = {this.state.disableBtns} onClick={this.create}>{this.i18n('button.create')}</Button>
    return <div className="panel-footer">
      <Button type="button" level="link" onClick={this.context.router.goBack}>
        {this.i18n('button.back')}
      </Button>
      {modifyBtn}
    </div>
  }

}
export default BasicForm;

BasicForm.propTypes = {
  name: React.PropTypes.string.isRequired,
  saveFunc: React.PropTypes.func
  //mode: React.PropTypes.symbol
};
