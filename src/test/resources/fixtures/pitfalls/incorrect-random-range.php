<?php

    <error descr="The range is not defined properly.">rand(2, 1)</error>;
    rand(1, 2);

    <error descr="The range is not defined properly.">mt_rand(2, 1)</error>;
    mt_rand(1, 2);

    <error descr="The range is not defined properly.">random_int(2, 1)</error>;
    random_int(1, 2);