<?php

class CasesHolderMissingArgument {
    public function <error descr="[EA] Has been deprecated in favour of 'spl_autoload_register(...)' as of PHP 7.2.0."><error descr="[EA] __autoload accepts exactly 1 arguments.">__autoload</error></error>() {}
}
class CasesHolderReturnValue {
    public function <error descr="[EA] Has been deprecated in favour of 'spl_autoload_register(...)' as of PHP 7.2.0.">__autoload</error>($parameter) {
        <error descr="[EA] __autoload cannot return a value.">return $parameter;</error>
    }
}
class CasesHolderReturnVoid {
    public function <error descr="[EA] Has been deprecated in favour of 'spl_autoload_register(...)' as of PHP 7.2.0.">__autoload</error>($parameter) {
        return;
    }
}