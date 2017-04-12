<?php

class Level1Class                     {}
class Level2Class extends Level1Class {}
class Level3Class extends Level2Class {}

/** @deprecated */
class Level4Class extends Level3Class {}

class L4Exception extends Level3Class {}

abstract class Level3Abstract extends Level2Class {}
class L4AExtendsAbstract extends Level3Abstract   {}