<?php
namespace {
    class BaseController
    {
    }

    class <weak_warning descr="Class name does not follow naming convention">EditUser</weak_warning> extends BaseController {

    }
    /** false-positive: class is correct */
    class EditUserController extends BaseController
    {

    }
    /** false-positive: class does not extend target class. So we do not validate it */
    class EditBalance
    {

    }
}

namespace B {
    class BaseController {

    }
    /** false-positive: class extends \B\BaseController so we do not validate it*/
    class EditBalance extends BaseController
    {

    }
}