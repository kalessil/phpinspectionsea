<?php

use function scandir;
use function glob;

scandir('...', SCANDIR_SORT_NONE);
scandir('...', SCANDIR_SORT_ASCENDING);

glob('...', GLOB_NOSORT);
glob('...', null);