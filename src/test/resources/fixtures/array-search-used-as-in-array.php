<?php

if (!<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search</warning>('', array()))          {}
if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search</warning>('', array()))           {}

if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search</warning>('', array()) || false)  {}
if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search</warning>('', array()) or false)  {}
if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search</warning>('', array()) OR false)  {}

if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search</warning>('', array()) && true)   {}
if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search</warning>('', array()) and true)  {}
if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search</warning>('', array()) AND true)  {}

if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search('', array()) === false</warning>) {}
if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">false === array_search('', array())</warning>) {}

if (array_search('', array()) !== <error descr="This makes no sense, as array_search(...) never returns true">true</error>)   {}
if (<error descr="This makes no sense, as array_search(...) never returns true">true</error> !== array_search('', array()))   {}

if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">array_search('', array()) !== false</warning>) {}
if (<warning descr="'in_array(...)' shall be used instead (clearer intention)">false !== array_search('', array())</warning>) {}

if (array_search('', array()) === <error descr="This makes no sense, as array_search(...) never returns true">true</error>)   {}
if (<error descr="This makes no sense, as array_search(...) never returns true">true</error> === array_search('', array()))   {}
