<?php
interface <weak_warning descr="Class name does not follow naming convention">My</weak_warning>{}
interface <weak_warning descr="Class name does not follow naming convention">InterfaceBase</weak_warning>{}
interface <weak_warning descr="Class name does not follow naming convention">myinterface</weak_warning>{}


/** false-positive */
interface My_Interface{}
interface my_custom_Interface{}
interface InterfaceCustom extends MyInterface {}
trait Traitname{}
class ClassName{}