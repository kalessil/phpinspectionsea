<?php

    use function scandir;
    use function glob;

    <warning descr="[EA] 'scandir(...)' sorts results by default, please provide second argument for specifying the intention.">scandir('...')</warning>;
    scandir('...', SCANDIR_SORT_ASCENDING);

    <warning descr="[EA] 'glob(...)' sorts results by default, please provide second argument for specifying the intention.">glob('...')</warning>;
    glob('...', null);

    <warning descr="[EA] 'glob('*', GLOB_ONLYDIR)' would be more performing here (reduces amount of file system interactions).">array_filter(glob('*'), 'is_dir')</warning>;
    <warning descr="[EA] 'glob('*', GLOB_ONLYDIR)' would be more performing here (reduces amount of file system interactions).">array_filter(glob('*'), '\is_dir')</warning>;
    <warning descr="[EA] 'glob('*', GLOB_NOSORT | GLOB_ONLYDIR)' would be more performing here (reduces amount of file system interactions).">array_filter(glob('*', GLOB_NOSORT), 'is_dir')</warning>;
    <warning descr="[EA] 'glob('*', GLOB_ONLYDIR)' would be more performing here (reduces amount of file system interactions).">array_filter(glob('*', GLOB_ONLYDIR), 'is_dir')</warning>;
    <warning descr="[EA] 'glob('*', GLOB_NOSORT | GLOB_ONLYDIR)' would be more performing here (reduces amount of file system interactions).">array_filter(glob('*', GLOB_NOSORT | GLOB_ONLYDIR), 'is_dir')</warning>;

    <warning descr="[EA] 'is_file($file)' (guessed) would be more performing here (uses builtin caches).">file_exists($file)</warning>;
    <warning descr="[EA] 'is_file($file = '...')' (guessed) would be more performing here (uses builtin caches).">file_exists($file = '...')</warning>;
    <warning descr="[EA] 'is_dir($directory)' (guessed) would be more performing here (uses builtin caches).">file_exists($directory)</warning>;
    <warning descr="[EA] 'is_file($array['file'])' (guessed) would be more performing here (uses builtin caches).">file_exists($array['file'])</warning>;
    <warning descr="[EA] 'is_file($object->file)' (guessed) would be more performing here (uses builtin caches).">file_exists($object->file)</warning>;
    <warning descr="[EA] 'is_dir(dirname('...'))' (guessed) would be more performing here (uses builtin caches).">file_exists(dirname('...'))</warning>;

    file_exists($_file);
    file_exists($_directory);
    file_exists($array['_file']);
    file_exists($object->_file);

    function cases_holder() {
        if (<warning descr="[EA] 'is_dir('/tmp')' (discovered from workflow) would be more performing here (uses builtin caches).">file_exists('/tmp')</warning>) {
            rmdir('/tmp');
        }
        if (<warning descr="[EA] 'is_file('/tmp/process.lock')' (discovered from workflow) would be more performing here (uses builtin caches).">file_exists('/tmp/process.lock')</warning>) {
            unlink('/tmp/process.lock');
        }
        if (file_exists('/tmp/symlink/system.log')) {
            unlink(realpath('/tmp/symlink/system.log'));
        }
        if (<warning descr="[EA] 'is_file('clazz.php')' (discovered from workflow) would be more performing here (uses builtin caches).">file_exists('clazz.php')</warning>) {
            include_once 'clazz.php';
        }
    }
