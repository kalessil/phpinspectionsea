<?php

    echo <error descr="[EA] Please provide regex delimiter as the second argument for proper escaping.">preg_quote('')</error>;
    echo '#'.<error descr="[EA] Please provide regex delimiter as the second argument for proper escaping.">preg_quote('')</error>;
    echo '/'.<error descr="[EA] Please provide regex delimiter as the second argument for proper escaping.">preg_quote('')</error>.'/';
    echo "/".<error descr="[EA] Please provide regex delimiter as the second argument for proper escaping.">preg_quote('')</error>."/";

    echo preg_quote('', <error descr="[EA] The separator value is platform-dependent, consider using '/' instead.">DIRECTORY_SEPARATOR</error>);

    /* false -positives */
    echo preg_quote('', '');
    echo '#'.preg_quote('', '');
    echo '/'.preg_quote('', '').'/';
