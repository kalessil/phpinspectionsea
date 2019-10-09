<?php

namespace {
    use function uniqid;

    <error descr="Please provide 'more_entropy' parameter in order to increase likelihood of uniqueness.">uniqid()</error>;
    <error descr="Please provide 'more_entropy' parameter in order to increase likelihood of uniqueness.">uniqid('q')</error>;

    call_user_func(<error descr="Please provide 'more_entropy' parameter in order to increase likelihood of uniqueness.">'uniqid'</error>, '');
    call_user_func(<error descr="Please provide 'more_entropy' parameter in order to increase likelihood of uniqueness.">'\uniqid'</error>, '');

    call_user_func('uniqid');
    uniqid('', true);
}