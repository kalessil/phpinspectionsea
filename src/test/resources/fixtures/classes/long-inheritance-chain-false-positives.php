<?php

class Level1Class {
}
class Level2Class extends Level1Class {
}
class Level3Class extends Level2Class {
}

/**
 * @deprecated
 */
class Level4Class extends Level3Class {
}

class Level4Exception extends Level3Class {
}