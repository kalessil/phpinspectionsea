<?php

    echo (new \DateTime())->setTime(1, 0, 0, <error descr="The call will return false ('microseconds' parameter is available in PHP 7.1+).">1</error>);
    echo (new \DateTime())->setTime(1, 0, 0, <error descr="The call will return false ('microseconds' parameter is available in PHP 7.1+).">null</error>);
    echo (new \DateTime())->setTime(1, 0, 0);