# LabelWrapper component

Component for wrap any content. This content will be add to form with label and correct dimension (in form). Extended from AbstractFormComponent.

## Parameters

All parameters from AbstractFormComponent are supported.

## Usage

```html
<Basic.LabelWrapper readOnly ref="applicant" label={this.i18n('applicant')} componentSpan="col-sm-5">
  <Advanced.IdentityInfo entity={applicant} showLoading={!applicant} className="no-margin"/>
</Basic.LabelWrapper>
 />
```
