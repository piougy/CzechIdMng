import fs from 'fs';
import gulp from 'gulp';
import path from 'path';
import browserify from 'browserify';
import watchify from 'watchify';
import source from 'vinyl-source-stream';
import buffer from 'vinyl-buffer';
import eslint from 'gulp-eslint';
import bootlint from 'gulp-bootlint';
import babelify from 'babelify';
import uglify from 'gulp-uglify';
import rimraf from 'rimraf';
import notify from 'gulp-notify';
import browserSync, { reload } from 'browser-sync';
import sourcemaps from 'gulp-sourcemaps';
import htmlReplace from 'gulp-html-replace';
import image from 'gulp-image';
import runSequence from 'run-sequence';
import less from 'gulp-less';
import minifyCSS from 'gulp-minify-css';
import autoprefixer from 'gulp-autoprefixer';
import mocha from 'gulp-mocha';
import babelRegister from 'babel/register';
import stringify from 'stringify';
import yargs from 'yargs';
import util from 'gulp-util';
import pathmodify from 'pathmodify';

const paths = {
  bundle: 'app.js',
  srcJs: ['node_modules/bootstrap-less/js/bootstrap.min.js', 'node_modules/jquery/dist/jquery.min.js', 'node_modules/metismenu/dist/metisMenu.min.js'],
  srcFont: ['node_modules/bootstrap-less/fonts/*', 'src/fonts/*', 'node_modules/font-awesome/fonts/*'],
  srcJsx: 'src/Index.js',
  srcCss: ['src/css/**/*.less', 'src/css/google.fonts.css', 'src/components/**/*.less', 'src/themes/**/*.less'],
  srcImg: 'src/images/**',
  srcLocale: 'src/modules/**/locales/**',
  srcThemes: 'src/modules/**/themes/**/images/**', // only images for now
  testSrc: 'test/**/*.js',
  dist: 'dist',
  distJs: 'dist/js',
  distImg: 'dist/images',
  distCss: 'dist/css',
  distFont: 'dist/fonts',
  distLocale: 'dist/locales',
  distThemes: 'dist/modules',
  src: 'src/**/*.js'
};

const pathmodifyOptions = {
  mods: [
    pathmodify.mod.dir('app', path.join(__dirname, 'src')),
    pathmodify.mod.dir('core', path.join(__dirname, 'src/modules/core'))
  ]
};

/**
 * Returns configuration for requestet environment
 * @param  {string} env environment
 * @return {object}     config json
 */
function getConfigByEnvironment(env = 'development', profile = 'default') {
  return require('./config/' + profile + '/' + env + '.json');
}

/**
 * Select environment stage and profile by input arguments.
 */
function selectStageAndProfile() {
  const argv = yargs.alias('p', 'profile').alias('s', 'stage').usage('Usage: $0 --profile [name of profile] --stage [development/test/production]')
  .choices('stage', ['development', 'test', 'production']).help('help').alias('h', 'help').argv;
  let profile = argv.profile;
  if (!profile) {
    profile = 'default';
    util.log('No profile argument present. Profile "' + profile + '" will be used for build!');
  } else {
    util.log('Profile "' + profile + '" will be used for build.');
  }
  let stage = argv.stage;
  if (!stage) {
    stage = 'development';
    util.log('No stage argument present. Stage "' + stage + '" will be used for build!');
  } else {
    util.log('Stage "' + stage + '" will be used for build.');
  }
  process.env.NODE_ENV = stage;
  process.env.NODE_PROFILE = profile;
}

gulp.task('clean', cb => {
  rimraf('dist', cb);
});

gulp.task('browserSync', () => {
  browserSync({
    server: {
      baseDir: './'
    }
  });
});

gulp.task('watchify', () => {
  const bundler = watchify(
    browserify(paths.srcJsx, watchify.args)
      .plugin(pathmodify, pathmodifyOptions)
      .transform(stringify)
  );

  function rebundle() {
    return bundler
      .bundle()
      .on('error', notify.onError())
      .pipe(source(paths.bundle))
      .pipe(gulp.dest(paths.distJs))
      .pipe(reload({stream: true}));
  }

  bundler.transform(babelify)
  .on('update', rebundle);
  return rebundle();
});

gulp.task('browserify', () => {
  browserify(paths.srcJsx)
  .plugin(pathmodify, pathmodifyOptions)
  .transform(stringify)
  .transform(babelify)
  .bundle()
  .pipe(source(paths.bundle))
  .pipe(buffer())
  .pipe(sourcemaps.init())
  .pipe(
    uglify({
      compress: {
        global_defs: {
          DEBUG: false
        }
      }
    })
  )
  .pipe(sourcemaps.write('.'))
  .pipe(gulp.dest(paths.distJs));
});

gulp.task('styles', () => {
  const config = getConfigByEnvironment(process.env.NODE_ENV, process.env.NODE_PROFILE);
  //
  return gulp.src(paths.srcCss)
    .pipe(sourcemaps.init())
    .pipe(less({
      compress: true,
      globalVars: {
        ENV: config.env,
        version: 10,
        theme: '\"' + config.theme + '\"' // wrap to quotes - less engine needs it to skip formating slash characters
      }
    }))
    .pipe(autoprefixer('last 10 versions', 'ie 9'))
    .pipe(minifyCSS({keepBreaks: false}))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.distCss))
    .pipe(reload({stream: true}));
});

gulp.task('htmlReplace', () => {
  const config = getConfigByEnvironment(process.env.NODE_ENV, process.env.NODE_PROFILE);
  //
  return gulp.src(['index.html'])
  .pipe(htmlReplace(
    {
      icon: {
        src: config.theme + '/images/favicon.ico',
        tpl: '<link rel="shortcut icon" href="%s" type="image/x-icon" />'
      },
      favicon: {
        src: config.theme + '/images/favicon.gif',
        tpl: '<link rel="icon" href="%s" type="image/gif" />'
      },
      css: ['css/main.css', 'css/google.fonts.css'],
      js: ['js/jquery.min.js', 'js/bootstrap.min.js', 'js/metisMenu.min.js', 'js/app.js']
    })
  )
  .pipe(gulp.dest(paths.dist));
});

gulp.task('images', () => {
  return gulp.src(paths.srcImg)
  .pipe(image())
  .pipe(gulp.dest(paths.distImg));
});

gulp.task('themes', () => {
  return gulp.src(paths.srcThemes)
    .pipe(gulp.dest(paths.distThemes));
});

gulp.task('js', () => {
  return gulp.src(paths.srcJs)
    .pipe(gulp.dest(paths.distJs));
});

gulp.task('fonts', () => {
  return gulp.src(paths.srcFont)
    .pipe(gulp.dest(paths.distFont));
});

gulp.task('locales', () => {
  return gulp.src(paths.srcLocale)
    .pipe(gulp.dest(paths.distLocale))
    .pipe(reload({stream: true}));
});

gulp.task('lint', () => {
  return gulp.src(paths.srcJsx)
    .pipe(eslint())
    .pipe(eslint.format())
    .pipe(bootlint());
});

gulp.task('config', (cb) => {
  fs.writeFile(path.join(__dirname, '/config.json'), JSON.stringify(getConfigByEnvironment(process.env.NODE_ENV, process.env.NODE_PROFILE)), cb);
});

gulp.task('test', () => {
  const argv = yargs.alias('w', 'watch').help('help').alias('h', 'help')
  .usage('Usage (for only one run test): gulp test --profile [name of profile] --stage [development/test/production]\nUsage (for permanent watch on src and test changes): gulp test --watch').argv;
  const watchArg = argv.watch;
  if (watchArg) {
    gulp.watch([paths.src, paths.testSrc], ['runTest']);
  } else {
    selectStageAndProfile();
    runSequence('clean', ['runTest']);
  }
});

gulp.task('runTest', () => {
  // https://www.npmjs.com/package/gulp-mocha#require
  return gulp.src(paths.testSrc, { read: false })
    .pipe(mocha({
      reporter: 'nyan',
      recursive: true,
      compilers: ['js:babel/register'],
      require: ['./test/setup.js'],
      ignoreLeaks: false
    }));
});

gulp.task('watchTask', () => {
  gulp.watch(paths.srcCss, ['styles']);
  gulp.watch(paths.srcJsx, ['lint']);
  gulp.watch(paths.srcLocale, ['locales']);
});

gulp.task('watch', cb => {
  selectStageAndProfile();
  runSequence('clean', 'runTest', ['browserSync', 'watchTask', 'watchify', 'config', 'styles', 'lint', 'images', 'themes', 'js', 'fonts', 'locales'], cb);
});

gulp.task('build', cb => {
  selectStageAndProfile();
  runSequence('clean', ['browserify', 'config', 'styles', 'htmlReplace', 'images', 'themes', 'js', 'fonts', 'locales'], cb);
});

gulp.task('default', ['watch']);
