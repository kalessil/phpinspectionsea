<?php

    preg_match(<error descr="Did you mean [...A-Za-z...] instead of [...a-Z...]?">'/[a-Z]/'</error>, '');
    preg_match(<error descr="Did you mean [...A-Za-z...] instead of [...a-Z...]?">'/[^a-Z]/'</error>, '');
    preg_match(<error descr="Did you mean [...A-Za-z...] instead of [...A-z...]?">'/[A-z]/'</error>, '');
    preg_match(<error descr="Did you mean [...A-Za-z...] instead of [...A-z...]?">'/[^A-z]/'</error>, '');

    preg_match('/.+a-Z.+/', '');
    preg_match('/.+A-z.+/', '');

    preg_match('/[a-z]/', '');
    preg_match('/[A-Z]/', '');
    preg_match('/[A-Za-z]/', '');
    preg_match('/[a-zA-Z]/', '');
