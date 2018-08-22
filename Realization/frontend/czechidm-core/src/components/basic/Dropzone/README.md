# Dropzone component

Component for select files.

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| onDrop | func.isRequired | Function call after droped or selected any files | |
| readOnly | bool | read only | false |
| multiple | bool | Can be select multiple files | true |
| accept | string | Define accepted file extension | |
| style  | object   | Define styles in object | default style (/styles.js) |
| styleActive  | object   | Object with styles for active state (when are files accepted) | default style (/styles.js) |
| styleReject  | object   | Object with styles for reject state (when are files rejected) | default style (/styles.js) ||

## Usage
```javascript

<Basic.Dropzone ref="dropzone"
  multiple={true}
  accept="text/xml"
  onDrop={this.onDrop.bind(this)}>
</Basic.Dropzone>

onDrop(files) {
  if (this.refs.dropzone.state.isDragReject){
    this.addMessage({
      message: this.i18n('filesRejected'),
      level: 'warning'
    });
    return;
  }
  files.forEach((file)=> {
    this.upload(file);
  });
}
```
