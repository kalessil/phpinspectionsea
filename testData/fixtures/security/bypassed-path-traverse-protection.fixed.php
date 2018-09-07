<?php

function cases_holder() {
    preg_replace('/\.+[\/\\]+/', '', '...');
    preg_replace('/\.+[\/\\]+/', '', '...');
    preg_replace('/\.+[\/\\]+/', '', '...');

    str_replace(['\\', '../'], ['/', ''], '...');
    str_replace(['..\\', '../'], '', '...');

    str_replace('..', '', '...');
}