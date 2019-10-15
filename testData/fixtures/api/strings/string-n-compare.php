<?php

    strncmp('wrong-length-specified', $string, <error descr="[EA] The specified length doesn't match the string length.">1</error>);
    strncmp($string, 'wrong-length-specified', <error descr="[EA] The specified length doesn't match the string length.">1</error>);
    strncmp($string, 'correct-length-specified', 24);
    strncmp($string, 'correct-\\-escaping', 18);

    strncasecmp('wrong-length-specified', $string, <error descr="[EA] The specified length doesn't match the string length.">1</error>);
    strncasecmp($string, 'wrong-length-specified', <error descr="[EA] The specified length doesn't match the string length.">1</error>);
    strncasecmp($string, 'correct-length-specified', 24);
    strncasecmp($string, 'correct-\\-escaping', 18);