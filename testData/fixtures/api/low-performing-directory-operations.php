<?php

    use function scandir;
    use function glob;

    <error descr="'scandir(...)' sorts results by default, please provide second argument for specifying the intention.">scandir('...')</error>;
    scandir('...', SCANDIR_SORT_ASCENDING);

    <error descr="'glob(...)' sorts results by default, please provide second argument for specifying the intention.">glob('...')</error>;
    glob('...', null);

    <error descr="'glob('*', GLOB_ONLYDIR)' would be more performing here (reduces amount of file-system interactions).">array_filter(glob('*'), 'is_dir')</error>;
    <error descr="'glob('*', GLOB_ONLYDIR)' would be more performing here (reduces amount of file-system interactions).">array_filter(glob('*'), '\is_dir')</error>;
    <error descr="'glob('*', GLOB_NOSORT | GLOB_ONLYDIR)' would be more performing here (reduces amount of file-system interactions).">array_filter(glob('*', GLOB_NOSORT), 'is_dir')</error>;
    <error descr="'glob('*', GLOB_ONLYDIR)' would be more performing here (reduces amount of file-system interactions).">array_filter(glob('*', GLOB_ONLYDIR), 'is_dir')</error>;
    <error descr="'glob('*', GLOB_NOSORT | GLOB_ONLYDIR)' would be more performing here (reduces amount of file-system interactions).">array_filter(glob('*', GLOB_NOSORT | GLOB_ONLYDIR), 'is_dir')</error>;