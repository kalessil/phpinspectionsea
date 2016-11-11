<?php

class Level1Class {
}

class Level2Class extends Level1Class {
}

class Level3Class extends Level2Class {
}

class <weak_warning descr="Class has 3 or more parent classes, consider using appropriate design patterns.">Level4Class</weak_warning> extends Level3Class {
}