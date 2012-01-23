<?php defined("SYSPATH") or die("No direct script access.") ?>

<? if ($item->owner) { ?>
<li>
  <? if ($item->owner->url) { ?>
        <?= t("By: <a href=\"%owner_url\">%owner_name</a>",
                      array("owner_name" => $item->owner->display_name(),
                            "owner_url" => $item->owner->url)); ?>
  <? } else { ?>
        <?= t("By: %owner_name", array("owner_name" => $item->owner->display_name())); ?>
  <? } ?>
</li>
<? } ?>

<? for ($i=1; $i<=$stars; $i++) { ?>
<img src="<?= $theme->url('images/star-color-16x16.png'); ?>"/>
<? } ?>
<? for ($i=$stars; $i<5; $i++) { ?>
<img src="<?= $theme->url('images/star-gray-16x16.png'); ?>"/>
<? } ?>