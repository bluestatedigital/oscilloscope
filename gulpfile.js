var gulp = require('gulp');
var source = require('vinyl-source-stream'); // Used to stream bundle for further handling
var browserify = require('browserify');
var watchify = require('watchify');
var reactify = require('reactify');
var concat = require('gulp-concat');
var sass = require('gulp-sass');
var gutil = require('gulp-util');

var buildDestination = 'src/main/resources/app/build';

gulp.task('browserify', function() {
   return browserify({
        entries: ['app/js/main.js'], // Only need initial file, browserify finds the deps
        transform: [reactify], // We want to convert JSX to normal javascript
        debug: false, // Gives us sourcemapping
        cache: {}, packageCache: {}, fullPaths: true // Requirement of watchify
    })
   .bundle()
   .pipe(source('app.js'))
   .pipe(gulp.dest(buildDestination));
});

gulp.task('browserify:dev', function() {
    var bundler = browserify({
        entries: ['app/js/main.js'], // Only need initial file, browserify finds the deps
        transform: [reactify], // We want to convert JSX to normal javascript
        debug: true, // Gives us sourcemapping
        cache: {}, packageCache: {}, fullPaths: true // Requirement of watchify
    });
    var watcher  = watchify(bundler);

    return watcher
        .on('update', function (files) { // When any files update
            var updateStart = Date.now();
            watcher.bundle() // Create new bundle that uses the cache for high performance
                .pipe(source('app.js'))
                .pipe(gulp.dest(buildDestination));
            gutil.log('Updated', files.length, 'file(s) in', gutil.colors.magenta((Date.now() - updateStart) + ' ms'));
        })
        .bundle() // Create the initial bundle when starting the task
        .pipe(source('app.js'))
        .pipe(gulp.dest(buildDestination));
});

gulp.task('sass', function () {
    return gulp.src(['app/css/**/*.scss'])
        .pipe(sass().on('error', sass.logError))
        .pipe(gulp.dest(buildDestination));
});

gulp.task('sass:watch', function () {
    gulp.watch(['app/css/**/*.scss'], ['sass']);
});

gulp.task('css', function () {
    return gulp.src(['app/css/normalize.css', 'app/css/**/*.css'])
        .pipe(concat('app.css'))
        .pipe(gulp.dest(buildDestination));
});

gulp.task('css:watch', function () {
    gulp.watch(['app/css/**/*.css'], ['css']);
});

gulp.task('default', ['browserify', 'sass', 'css']);
gulp.task('dev', ['browserify:dev', 'sass', 'css', 'sass:watch', 'css:watch']);
