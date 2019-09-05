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