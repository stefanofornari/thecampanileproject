<?php require_once("http://localhost:8080/campanile/java/Java.inc");

defined("SYSPATH") or die("No direct script access.");

class star_installer {

  static function install() {
    module::set_var("star", "test", 1);
    module::set_version("star", 1);
  }

  static function upgrade($version) {
    module::set_var("star", "test", java("java.lang.System")->currentTimeMillis());
    //module::set_var("star", "test", 3);
    if ($version == 1) {
    }
  }
}
