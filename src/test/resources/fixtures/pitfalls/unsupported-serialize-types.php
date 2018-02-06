<?php

function cases_holder() {
    /* @var \SimpleXMLElement $simpleXmlElement */
    /* @var \HashContext      $hashContext      */
    /* @var \Closure          $closure          */
    /* @var \SplFileInfo      $splFileInfo      */
    /* @var \Generator        $generator        */
    /* @var \stdClass         $stdClass         */

    return [
        <error descr="'\SimpleXMLElement' doesn't support serialization (caused serialization is not allowed error).">serialize($simpleXmlElement)</error>,
        <error descr="'\HashContext' doesn't support serialization (caused serialization is not allowed error).">serialize($hashContext)</error>,
        <error descr="'\Closure' doesn't support serialization (caused serialization is not allowed error).">serialize($closure)</error>,
        <error descr="'\SplFileInfo' doesn't support serialization (caused serialization is not allowed error).">serialize($splFileInfo)</error>,
        <error descr="'\Generator' doesn't support serialization (caused serialization is not allowed error).">serialize($generator)</error>,
        serialize($stdClass),
    ];
}