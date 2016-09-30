# Basic form
Form for wrap and control inner components.
- Extend AbstractForm component.
- Defined default inner footer with buttons for save and cancel form.


## Usage
```html
<BasicForm ref="userForm" name="firstBasicForm name">
    <TextField ref="name" label="Uživatelské jméno"/>
    <TextField ref="telephone" label="Telefon"/>
    <TextField ref="mobil" label="Mobil"/>
    <TextField ref="email" label="Email"/>

    <div className="row">
      <div className="col-sm-12">
        <div className="col-sm-5">
          <TextField ref="firstName" label="Jméno"/>
        </div>
        <div className="col-sm-7">
          <TextField ref="lastNameKey" label="Příjmení"/>
        </div>
      </div>
    </div>

    <div className="form-group">
      <div className="col-sm-offset-3 col-sm-8">
           <div className="checkbox">
              <label>
                <input type="checkbox" checked={!this.state.disabled}/> Aktivní
              </label>
            </div>
        </div>
    </div>
</BasicForm>
```
