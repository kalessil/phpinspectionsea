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

    <warning descr="[EA] 'is_file($file)' would be more performing here (uses builtin caches).">file_exists($file)</warning>;
    <warning descr="[EA] 'is_file($file = '...')' would be more performing here (uses builtin caches).">file_exists($file = '...')</warning>;
    <warning descr="[EA] 'is_dir($directory)' would be more performing here (uses builtin caches).">file_exists($directory)</warning>;
    <warning descr="[EA] 'is_file($array['file'])' would be more performing here (uses builtin caches).">file_exists($array['file'])</warning>;
    <warning descr="[EA] 'is_file($object->file)' would be more performing here (uses builtin caches).">file_exists($object->file)</warning>;
    <warning descr="[EA] 'is_dir(dirname('...'))' would be more performing here (uses builtin caches).">file_exists(dirname('...'))</warning>;

    file_exists($_file);
    file_exists($_directory);
    file_exists($array['_file']);
    file_exists($object->_file);

    function cases_holder() {
        if (<warning descr="[EA] 'is_dir('directory')' would be more performing here (uses builtin caches).">file_exists('directory')</warning>) {
            rmdir('directory');
        }
        if (<warning descr="[EA] 'is_file('file')' would be more performing here (uses builtin caches).">file_exists('file')</warning>) {
            unlink('file');
        }
        if (file_exists('link')) {
            unlink(realpath('link'));
        }
    }
