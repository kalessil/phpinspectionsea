<?php

    if (<warning descr="'false !== strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr</warning>($strToTest, 'smth.'))  ;
    if (!<warning descr="'false === strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr</warning>($strToTest, 'smth.')) ;

    if ($strToTest || <warning descr="'false !== strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr</warning>($strToTest, 'smth.')) ;
    if ($strToTest && <warning descr="'false !== strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr</warning>($strToTest, 'smth.')) ;
    if ($strToTest or <warning descr="'false !== strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr</warning>($strToTest, 'smth.')) ;
    if ($strToTest and <warning descr="'false !== strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr</warning>($strToTest, 'smth.')) ;

    if (<warning descr="'false === strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr($strToTest, 'smth.') === false</warning>) ;
    if (<warning descr="'false !== strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr($strToTest, 'smth.') !== false</warning>) ;
    if (<warning descr="'false === strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr($strToTest, 'smth.') == false</warning>)  ;
    if (<warning descr="'false !== strpos($strToTest, 'smth.')' should be used instead (saves memory)">strstr($strToTest, 'smth.') != false</warning>)  ;

    if (<warning descr="'false === strpos($strToTest, 'smth.')' should be used instead (saves memory)">false === strstr($strToTest, 'smth.')</warning>) ;
    if (<warning descr="'false !== strpos($strToTest, 'smth.')' should be used instead (saves memory)">false !== strstr($strToTest, 'smth.')</warning>) ;
    if (<warning descr="'false === strpos($strToTest, 'smth.')' should be used instead (saves memory)">false == strstr($strToTest, 'smth.')</warning>) ;
    if (<warning descr="'false !== strpos($strToTest, 'smth.')' should be used instead (saves memory)">false != strstr($strToTest, 'smth.')</warning>) ;

    if (<warning descr="'false === stripos($strToTest, 'smth.')' should be used instead (saves memory)">false === stristr($strToTest, 'smth.')</warning>) ;
    if (<warning descr="'false !== stripos($strToTest, 'smth.')' should be used instead (saves memory)">false !== stristr($strToTest, 'smth.')</warning>) ;
    if (<warning descr="'false === stripos($strToTest, 'smth.')' should be used instead (saves memory)">false == stristr($strToTest, 'smth.')</warning>) ;
    if (<warning descr="'false !== stripos($strToTest, 'smth.')' should be used instead (saves memory)">false != stristr($strToTest, 'smth.')</warning>) ;
