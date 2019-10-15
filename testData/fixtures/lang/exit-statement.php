<?php

    <error descr="[EA] Exit statuses should be in the range 0 to 254, -1 is given.">exit (-1)</error>;
    <error descr="[EA] Exit statuses should be in the range 0 to 254, 256 is given.">exit (256)</error>;

    exit (0);
    exit (1);
    exit (254);