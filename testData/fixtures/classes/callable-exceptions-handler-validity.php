<?php

function invalid_handler(\Exception $e) {}
class Invalid {
    static public function handler(\Exception $e) {}
}

set_exception_handler(<warning descr="[EA] \Throwable instead of \Exception should be used in the handler (BC break introduced in PHP 7).">['Invalid', 'handler']</warning>);
set_exception_handler(<warning descr="[EA] \Throwable instead of \Exception should be used in the handler (BC break introduced in PHP 7).">'invalid_handler'</warning>);
set_exception_handler(function (<warning descr="[EA] \Throwable instead of \Exception should be used in the handler (BC break introduced in PHP 7).">\Exception $e</warning>) {});
set_exception_handler(function (<warning descr="[EA] \Throwable instead of \Exception should be used in the handler (BC break introduced in PHP 7).">Exception $e</warning>) {});

set_exception_handler(function (\Throwable $e) {});
set_exception_handler(function ($e) {});