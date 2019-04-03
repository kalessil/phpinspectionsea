<?php

    <error descr="'scandir(...)' sorts results by default, please specify the second argument.">scandir('...')</error>;
    scandir('...', SCANDIR_SORT_ASCENDING);

    <error descr="'glob(...)' sorts results by default, please specify the second argument.">glob('...')</error>;
    glob('...', null);