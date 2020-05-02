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

    file_exists($file);
    file_exists($file = '...');
    file_exists($directory);
    file_exists($array['file']);
    file_exists($object->file);

    file_exists($_file);
    file_exists($_directory);
    file_exists($array['_file']);
    file_exists($object->_file);
