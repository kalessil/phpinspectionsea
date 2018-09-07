<?php

function cases_holder() {
    <error descr="The call doesn't prevent path traversal, as can be bypassed with e.g. '....//'.">str_replace('../', '', '...')</error>;
    <error descr="The call doesn't prevent path traversal, as can be bypassed with e.g. '....//'.">str_replace('..'.DIRECTORY_SEPARATOR, '', '...')</error>;
    <error descr="The call doesn't prevent path traversal, as can be bypassed with e.g. '....//'.">str_replace('..\\', '', '...')</error>;

    <error descr="The call doesn't prevent path traversal, as can be bypassed with e.g. '....//'.">str_replace(['\\', '../'], ['/', ''], '...')</error>;
    <error descr="The call doesn't prevent path traversal, as can be bypassed with e.g. '....//'.">str_replace(['..\\', '../'], '', '...')</error>;

    str_replace('..', '', '...');
}