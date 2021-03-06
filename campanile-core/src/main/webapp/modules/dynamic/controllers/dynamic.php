<?php defined("SYSPATH") or die("No direct script access.");/**
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
class Dynamic_Controller extends Controller {
  public function updates() {
    print $this->_show("updates");
  }

  public function popular() {
    print $this->_show("popular");
  }
  
  public function random() {
    print $this->_show_random();
  }
  
  public function _show_random() {
    $page_size = module::get_var("gallery", "page_size", 9);
    $page = Input::instance()->get("page", "1");
    
    $items = array();
    $children = array();
    $i = 0;
    
    $children_count = ORM::factory("item")
                    ->viewable()
                    ->where("type", "!=", "album")
                    ->count_all();
    
    $limit = min($children_count, $page_size);
    
    while ($i < $limit) {
      $items = item::random_query()->where("type", "!=", "album")->find_all(9, 0);
      foreach ($items as $item) {
        $children[$i] = $item;
        ++$i;
        if ($i >= $limit) {
            break;
        }
      }
    }
    
    $template = new Theme_View("page.html", "collection", "dynamic");
    $template->set_global("page", $page);
    $template->set_global("page_size", $page_size);
    $template->set_global("page_title", t("The Campanile Project"));
    $template->set_global("max_pages", 1);
    $template->set_global("children", $children);
    $template->set_global("children_count", $children_count);
    $template->content = new View("dynamic.html");
    $template->content->title = t($album_defn->title);

    print $template;
  }

  private function _show($album) {
    $page_size = module::get_var("gallery", "page_size", 9);
    $page = Input::instance()->get("page", "1");

    $album_defn = unserialize(module::get_var("dynamic", $album));
    $display_limit = $album_defn->limit;
    
    $children_count = ORM::factory("item")
                    ->viewable()
                    ->where("type", "!=", "album")
                    ->count_all();
    if (!empty($display_limit)) {
      $children_count = min($children_count, $display_limit);
    }

    $offset = ($page - 1) * $page_size;
    $max_pages = max(ceil($children_count / $page_size), 1);

    // Make sure that the page references a valid offset
    if ($page < 1 || ($children_count && $page > ceil($children_count / $page_size))) {
      throw new Kohana_404_Exception();
    }
    
    $image_count = module::get_var("image_block", "image_count");
    
    $children = ORM::factory("item")
                   ->viewable()
                   ->where("type", "!=", "album")
                   ->order_by($album_defn->key_field, "DESC")
                   ->find_all($page_size, $offset);

    $template = new Theme_View("page.html", "collection", "dynamic");
    $template->set_global("page", $page);
    $template->set_global("page_size", $page_size);
    $template->set_global("max_pages", $max_pages);
    $template->set_global("children", $children);
    $template->set_global("children_count", $children_count);
    $template->content = new View("dynamic.html");
    $template->content->title = t($album_defn->title);
    $template->set_global("page_title", t($album_defn->title));

    print $template;
  }

}