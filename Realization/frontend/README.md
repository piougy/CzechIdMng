# CzechIdM frontend


## Usage

In project folder:

### Install Node.js

**Node.js version 4.x or higher is required.** Download and install Node.js by your OS.

For linux (fedora):

`sudo dnf install nodejs`

Check nodejs version:

`node -v`

or

`nodejs -v`

For update nodejs from 0.x versions:
* https://nodejs.org/en/download/package-manager/#enterprise-linux-and-fedora
* http://tecadmin.net/upgrade-nodejs-via-npm/#

### Install gulp

Globally as root:

`sudo npm install -g gulp`

or locally:

`npm install gulp`

## Install the dependencies

`npm install`

## Test

`npm run test`
or better
`npm run test-watch`

__Test via gulp (for profile "default" and stage "test". Profile and stage arguments are supported. Profiles could be defined in [configuration](./config)):__

`gulp test -p default -s test`

or for livereload (check src and test dir)

`gulp test -w`  (profile and stage arguments are not supported)

### Development mode with livereload

`gulp`

__For run with specific profile and stage (default value for profile is `default`  and for stage argument is `development`):__

`gulp -p default -s test`

### Build

When you are done, a production ready version of the JS bundle can be created:

`gulp build -p default -s test`

Builded application will be located in `dist` folder. Application could be deployed to any http server (e.g. Apache).

### [Docs](./docs/README.md)


### npm link for components development (draft)
* create module: https://docs.npmjs.com/getting-started/creating-node-modules
* publish module https://docs.npmjs.com/getting-started/scoped-packages
* Nexus: https://books.sonatype.com/nexus-book/reference/npm-deploying-packages.html
* npm link:
  * https://docs.npmjs.com/cli/link
  * http://justjs.com/posts/npm-link-developing-your-own-npm-modules-without-tears
