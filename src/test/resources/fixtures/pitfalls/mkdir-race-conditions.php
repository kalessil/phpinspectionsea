<?php

    <error descr="Following construct shall be used: 'if (!@mkdir(...) && !is_dir(...)) { throw ...; }'">mkdir('test');</error>

    if (!is_dir('test')) {
        <error descr="Following construct shall be used: 'if (!@mkdir(...) && !is_dir(...)) { throw ...; }'">mkdir('test');</error>
    }

    <error descr="Following construct shall be used: 'if (!@mkdir(...) && !is_dir(...)) { throw ...; }'">if</error> ((!mkdir('test'))) {
       echo 'not created';
    }

    if (!is_dir('test') &&
        <error descr="Condition needs to be corrected (invert if needed): '!@mkdir(...) && !is_dir(...)'">!mkdir('test')</error>
    ) {
        echo 'not created';
    }