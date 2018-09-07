<?php

    preg_match(<warning descr="'(a|b)' is 'greedy'. Please use '([ab])' instead.">'/(a|b)/'</warning>, '');
    preg_match(<warning descr="'(^|\.|\*|\]|\\)' is 'greedy'. Please use '([\^\.\*\]\\])' instead.">'/(^|\.|\*|\]|\\)/'</warning>, '');
    preg_match('/(a)/', '');
    preg_match('/(a|aa)/', '');