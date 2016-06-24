# AbstractContent Component

Super class for all contents (pages) in application.

## Usage

```javascript
import * as Basic from '../../../components/basic';
...
export default class MyPage extends Basic.AbstractContent {
  ...
  // AbstractContextComponent automatically injects redux store to component context.
  // functions from AbstractContextComponent can be used (e.q i18n(..), addMessage(...) ...)
  ...

  /**
   * Return content identifier, with can be used in localization etc.
   *
   * @return {string} content identifier
   */
  getContentKey() {
    return 'content.MyPage';
  }
}
...
```

## Content skeleton

```javascript
'use strict';

import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';

/**
 * Organizations list
 */
class Organizations extends Basic.AbstractContent {

  constructor(props, context) {
     super(props, context);
  }

  getContentKey() {
    return 'content.organizations';
  }

  componentDidMount() {
    this.selectNavigationItem('organizations');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        TODO
      </div>
    );
  }
}

Organizations.propTypes = {
}
Organizations.defaultProps = {
}

function select(state) {
  return {};
}

export default connect(select)(Organizations);
```
