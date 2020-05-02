<?php

    use function scandir;
    use function glob;

    scandir('...', SCANDIR_SORT_NONE);
    scandir('...', SCANDIR_SORT_ASCENDING);

    glob('...', GLOB_NOSORT);
    glob('...', null);

    glob('*', GLOB_ONLYDIR);
    glob('*', GLOB_ONLYDIR);
    glob('*', GLOB_NOSORT | GLOB_ONLYDIR);
    glob('*', GLOB_ONLYDIR);
    glob('*', GLOB_NOSORT | GLOB_ONLYDIR);

    is_file($file);
    is_file($file = '...');
    is_dir($directory);
    is_file($array['file']);
    is_file($object->file);
    is_dir(dirname('...'));

    file_exists($_file);
    file_exists($_directory);
    file_exists($array['_file']);
    file_exists($object->_file);

    function cases_holder() {
        if (is_dir('directory')) {
            rmdir('directory');
        }
        if (is_file('file')) {
            unlink('file');
        }
        if (file_exists('link')) {
            unlink(realpath('link'));
        }
    }
