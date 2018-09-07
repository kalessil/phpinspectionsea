<?php

    <warning descr="It's not recommended to rely on $secure and $httponly defaults (apply QF to see how to harden the call).">setcookie('')</warning>;
    <warning descr="It's not recommended to rely on $secure and $httponly defaults (apply QF to see how to harden the call).">setcookie('', '', 3600)</warning>;
    <warning descr="It's not recommended to rely on $secure and $httponly defaults (apply QF to see how to harden the call).">session_set_cookie_params(0)</warning>;
    <warning descr="It's not recommended to rely on $secure and $httponly defaults (apply QF to see how to harden the call).">session_set_cookie_params(0, '/')</warning>;

    setcookie();
    session_set_cookie_params();