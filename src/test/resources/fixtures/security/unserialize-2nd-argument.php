<?php

    <error descr="Please specify classes allowed for unserialization in 2nd argument.">unserialize('...')</error>;
    <error descr="Please specify classes allowed for unserialization in 2nd argument.">unserialize('...', true)</error>;

    unserialize();
    unserialize('string', false);