<?php

function cases_holder()
{
    switch (<warning descr="[EA] Might not work properly with child classes. Consider using instanceof construct instead.">get_class($object)</warning>) {
        case '...':
        default:
            break;
    }
}