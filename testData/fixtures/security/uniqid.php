<?php

namespace {
    use function uniqid;

    <error descr="[EA] Please provide 'more_entropy' parameter in order to increase likelihood of uniqueness.">uniqid()</error>;
    <error descr="[EA] Please provide 'more_entropy' parameter in order to increase likelihood of uniqueness.">uniqid('q')</error>;

    call_user_func(<error descr="[EA] Please provide 'more_entropy' parameter in order to increase likelihood of uniqueness.">'uniqid'</error>, '');
    call_user_func(<error descr="[EA] Please provide 'more_entropy' parameter in order to increase likelihood of uniqueness.">'\uniqid'</error>, '');

    call_user_func('uniqid');
    uniqid('', true);
    uniqid(more_entropy: true);
}