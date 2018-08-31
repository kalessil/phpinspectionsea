<?php

namespace {
    use function uniqid;

    <error descr="Please provide both prefix and more entropy parameters.">uniqid()</error>;
    <error descr="Please provide both prefix and more entropy parameters.">uniqid('q')</error>;

    call_user_func(<error descr="Please provide both prefix and more entropy parameters.">'uniqid'</error>, '');
    call_user_func(<error descr="Please provide both prefix and more entropy parameters.">'\uniqid'</error>, '');

    call_user_func('uniqid');
    uniqid('', true);
}