<?php

function cases_holder() {
    /* @var \SimpleXMLElement $simpleXmlElement */
    /* @var \HashContext      $hashContext      */
    /* @var \Closure          $closure          */
    /* @var \SplFileInfo      $splFileInfo      */
    /* @var \Generator        $generator        */
    /* @var \stdClass         $stdClass         */

    return [
        serialize(<error descr="[EA] '\SimpleXMLElement' doesn't support serialization (causes serialization is not allowed error).">$simpleXmlElement</error>),
        serialize(<error descr="[EA] '\HashContext' doesn't support serialization (causes serialization is not allowed error).">$hashContext</error>),
        serialize(<error descr="[EA] '\Closure' doesn't support serialization (causes serialization is not allowed error).">$closure</error>),
        serialize(<error descr="[EA] '\SplFileInfo' doesn't support serialization (causes serialization is not allowed error).">$splFileInfo</error>),
        serialize(<error descr="[EA] '\Generator' doesn't support serialization (causes serialization is not allowed error).">$generator</error>),
        serialize($stdClass),
    ];
}