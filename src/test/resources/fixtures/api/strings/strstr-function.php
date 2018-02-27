<?php

    if (<warning descr="'false !== strpos('where', 'what')' should be used instead (saves memory).">strstr('where', 'what')</warning>)  ;
    if (<warning descr="'false === strpos('where', 'what')' should be used instead (saves memory).">!strstr('where', 'what')</warning>) ;

    if ('where' || <warning descr="'false !== strpos('where', 'what')' should be used instead (saves memory).">strstr('where', 'what')</warning>) ;
    if ('where' && <warning descr="'false !== strpos('where', 'what')' should be used instead (saves memory).">strstr('where', 'what')</warning>) ;
    if ('where' or <warning descr="'false !== strpos('where', 'what')' should be used instead (saves memory).">strstr('where', 'what')</warning>) ;
    if ('where' and <warning descr="'false !== strpos('where', 'what')' should be used instead (saves memory).">strstr('where', 'what')</warning>) ;

    if (<warning descr="'false === strpos('where', 'what')' should be used instead (saves memory).">strstr('where', 'what') === false</warning>) ;
    if (<warning descr="'false !== strpos('where', 'what')' should be used instead (saves memory).">strstr('where', 'what') !== false</warning>) ;
    if (<warning descr="'false === strpos('where', 'what')' should be used instead (saves memory).">strstr('where', 'what') == false</warning>)  ;
    if (<warning descr="'false !== strpos('where', 'what')' should be used instead (saves memory).">strstr('where', 'what') != false</warning>)  ;

    if (<warning descr="'false === strpos('where', 'what')' should be used instead (saves memory).">false === strstr('where', 'what')</warning>) ;
    if (<warning descr="'false !== strpos('where', 'what')' should be used instead (saves memory).">false !== strstr('where', 'what')</warning>) ;
    if (<warning descr="'false === strpos('where', 'what')' should be used instead (saves memory).">false == strstr('where', 'what')</warning>) ;
    if (<warning descr="'false !== strpos('where', 'what')' should be used instead (saves memory).">false != strstr('where', 'what')</warning>) ;

    if (<warning descr="'false === stripos('where', 'what')' should be used instead (saves memory).">false === stristr('where', 'what')</warning>) ;
    if (<warning descr="'false !== stripos('where', 'what')' should be used instead (saves memory).">false !== stristr('where', 'what')</warning>) ;
    if (<warning descr="'false === stripos('where', 'what')' should be used instead (saves memory).">false == stristr('where', 'what')</warning>) ;
    if (<warning descr="'false !== stripos('where', 'what')' should be used instead (saves memory).">false != stristr('where', 'what')</warning>) ;

    /* false-positives: weak types magic */
    if (true == strstr('where', 'what')) ;
    if (true != strstr('where', 'what')) ;