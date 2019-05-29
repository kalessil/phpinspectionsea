<?php

    preg_match(<error descr="'e' modifier is deprecated, please use 'preg_replace_callback()' instead.">'/deprecated\s+modifier/e'</error>, '');
    preg_match(<error descr="Unknown modifier 'Z'.">'/non-existing\s+modifier/Z'</error>, '');

    preg_match('/z/i')
    preg_quote('..Z', '/');