<?php

    <error descr="Exit statuses should be in the range 0 to 254, -1 is given.">exit (-1)</error>;
    <error descr="Exit statuses should be in the range 0 to 254, 255 is given.">exit (255)</error>;

    exit (0);
    exit (1);
    exit (254);