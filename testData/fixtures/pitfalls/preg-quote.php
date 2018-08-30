<?php

    echo <error descr="Please provide regex delimiter as the second argument for proper escaping.">preg_quote</error> ('');
    echo '#'.<error descr="Please provide regex delimiter as the second argument for proper escaping.">preg_quote</error> ('');
    echo '/'.<error descr="Please provide regex delimiter as the second argument for proper escaping.">preg_quote</error> ('').'/';
    echo "/".<error descr="Please provide regex delimiter as the second argument for proper escaping.">preg_quote</error> ('')."/";

    /* false -positives */
    echo preg_quote('', '');
    echo '#'.preg_quote('', '');
    echo '/'.preg_quote('', '').'/';
