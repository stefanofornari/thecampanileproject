<?php defined("SYSPATH") or die("No direct script access.");

require_once("java/Java.inc");

/**
 * Gallery - a web based photo album viewer and editor
 * Copyright (C) 2000-2011 Bharat Mediratta
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street - Fifth Floor, Boston, MA  02110-1301, USA.
 */
class info_theme_Core {
    
   
  static function thumb_info($theme, $item) {
      
      $i = java_closure($item, null, array(new Java("ste.campanile.Item")));

      $req = java_context()->getHttpServletRequest();
      $req->setAttribute("item", $i);
      $req->setAttribute("class", get_class($item->owner));

      return java_virtual("/modules/info/stars.bsh", true);
  }

  /**
  static function thumb_info($theme, $item) {
    $view = new View('info_thumb_bottom.html');
    $view->set('stars', 3);
    $view->bind('theme', $theme);
    $view->bind('item', $item);
    
    return $view->render();
  }
   * 
   */
}