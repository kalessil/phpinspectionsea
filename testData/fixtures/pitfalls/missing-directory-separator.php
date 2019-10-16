<?php

class CasesHolder
{

    public function directoryConstant() {
        return [
            __DIR__ . <warning descr="[EA] Looks like a directory separator is missing here.">'vendor'</warning>,
            '' . __DIR__ . <warning descr="[EA] Looks like a directory separator is missing here.">'vendor'</warning>,
            __DIR__ . <warning descr="[EA] Looks like a directory separator is missing here.">'vendor'</warning> . '',
            __DIR__ . <warning descr="[EA] Looks like a directory separator is missing here.">'vendor'</warning> . '..',

            /* valid cases */
            __DIR__ . DIRECTORY_SEPARATOR,
            __DIR__ . '/',
            __DIR__ . '\\',
            __DIR__ . ' ...',
        ];
    }

    public function dirnameFunction() {
        return [
            dirname(__DIR__) . <warning descr="[EA] Looks like a directory separator is missing here.">'vendor'</warning>,
            '' . dirname(__DIR__) . <warning descr="[EA] Looks like a directory separator is missing here.">'vendor'</warning>,
            dirname(__DIR__) . <warning descr="[EA] Looks like a directory separator is missing here.">'vendor'</warning> . '',

            /* valid cases */
            dirname(__DIR__) . DIRECTORY_SEPARATOR,
            dirname(__DIR__) . '/',
            dirname(__DIR__) . '\\',
            dirname(__DIR__) . ' ...',
        ];
    }

}