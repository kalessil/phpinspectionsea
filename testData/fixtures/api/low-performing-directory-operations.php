<?php

use function scandir;
use function glob;

<error descr="'scandir(...)' sorts results by default, please provide second argument for specifying the intention.">scandir('...')</error>;
scandir('...', SCANDIR_SORT_ASCENDING);

<error descr="'glob(...)' sorts results by default, please provide second argument for specifying the intention.">glob('...')</error>;
glob('...', null);