<?php

class Level1Class                     {}
class Level2Class extends Level1Class {}
class Level3Class extends Level2Class {}

/** @deprecated */
class Level4Class extends Level3Class {}

class L4Exception extends Level3Class {}

abstract class Level3Abstract extends Level2Class {}
class L4AExtendsAbstract extends Level3Abstract   {}

/* false-positives: not all exceptions named with exception-suffix */
class Level2Exception extends Exception {}
class Level3Exception extends Level2Exception {}
class Level4Exception extends Level3Exception {}
class ExceptionNamedAccordingToDDD extends Level4Exception {}
