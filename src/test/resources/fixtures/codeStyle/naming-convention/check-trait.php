<?php

trait <weak_warning descr="Class name does not follow naming convention">a</weak_warning>{}
trait <weak_warning descr="Class name does not follow naming convention">BTrait</weak_warning>{}
trait <weak_warning descr="Class name does not follow naming convention">TraitA</weak_warning>{}
trait <weak_warning descr="Class name does not follow naming convention">My_trait_Name</weak_warning>{}


/** false-positive */
trait MyTraitCustom{}
trait MyValidTraitName{}
interface MyInterfaceCustom{}
class MyTraitClass{}