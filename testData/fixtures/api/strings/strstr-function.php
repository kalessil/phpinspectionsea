<?php

    if (<warning descr="'strpos('where', 'what') !== false' should be used instead (saves memory).">strstr('where', 'what')</warning>)  ;
    if (<warning descr="'strpos('where', 'what') === false' should be used instead (saves memory).">!strstr('where', 'what')</warning>) ;

    if ('where' || <warning descr="'strpos('where', 'what') !== false' should be used instead (saves memory).">strstr('where', 'what')</warning>) ;
    if ('where' && <warning descr="'strpos('where', 'what') !== false' should be used instead (saves memory).">strstr('where', 'what')</warning>) ;
    if ('where' or <warning descr="'strpos('where', 'what') !== false' should be used instead (saves memory).">strstr('where', 'what')</warning>) ;
    if ('where' and <warning descr="'strpos('where', 'what') !== false' should be used instead (saves memory).">strstr('where', 'what')</warning>) ;

    if (<warning descr="'strpos('where', 'what') === false' should be used instead (saves memory).">strstr('where', 'what') === false</warning>) ;
    if (<warning descr="'strpos('where', 'what') !== false' should be used instead (saves memory).">strstr('where', 'what') !== false</warning>) ;
    if (<warning descr="'strpos('where', 'what') === false' should be used instead (saves memory).">strstr('where', 'what') == false</warning>)  ;
    if (<warning descr="'strpos('where', 'what') !== false' should be used instead (saves memory).">strstr('where', 'what') != false</warning>)  ;

    if (<warning descr="'strpos('where', 'what') === false' should be used instead (saves memory).">false === strstr('where', 'what')</warning>) ;
    if (<warning descr="'strpos('where', 'what') !== false' should be used instead (saves memory).">false !== strstr('where', 'what')</warning>) ;
    if (<warning descr="'strpos('where', 'what') === false' should be used instead (saves memory).">false == strstr('where', 'what')</warning>) ;
    if (<warning descr="'strpos('where', 'what') !== false' should be used instead (saves memory).">false != strstr('where', 'what')</warning>) ;

    if (<warning descr="'stripos('where', 'what') === false' should be used instead (saves memory).">false === stristr('where', 'what')</warning>) ;
    if (<warning descr="'stripos('where', 'what') !== false' should be used instead (saves memory).">false !== stristr('where', 'what')</warning>) ;
    if (<warning descr="'stripos('where', 'what') === false' should be used instead (saves memory).">false == stristr('where', 'what')</warning>) ;
    if (<warning descr="'stripos('where', 'what') !== false' should be used instead (saves memory).">false != stristr('where', 'what')</warning>) ;

    /* false-positives: weak types magic */
    if (true == strstr('where', 'what')) ;
    if (true != strstr('where', 'what')) ;