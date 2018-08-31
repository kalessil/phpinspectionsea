<?php

    strncmp('wrong-length-specified', $string, 22);
    strncmp($string, 'wrong-length-specified', 22);
    strncmp($string, 'correct-length-specified', 24);
    strncmp($string, 'correct-\\-escaping', 18);

    strncasecmp('wrong-length-specified', $string, 22);
    strncasecmp($string, 'wrong-length-specified', 22);
    strncasecmp($string, 'correct-length-specified', 24);
    strncasecmp($string, 'correct-\\-escaping', 18);