-- buff some bosses 
-- increase the trigger rate of summons for kv, chaos and death
update mobskill set trirnd=15 where `type`=3 and mobid in (45618, 45625, 45674);

-- increase the trigger rate of cancel for chaos, death and gaq
update mobskill set trirnd=30 where skillid=44 and mobid in (45614, 45625, 45674);

-- buff skill dmg of gaq and increase her mr to 150
update mobskill set leverage=leverage*1.5 where mobid=45614;
update npc set mr=150 where npcid=45614;

-- reduce drop rate of junks from skeleton fighters, archers etc from dv
update droplist set chance=5000 where itemid in (100098, 120043) and mobid=45269;
update droplist set chance=5000 where itemid in (172, 100172) and mobid=45270;
update droplist set chance=5000 where itemid in (100143, 120242) and mobid=45286;

-- respawn royal lvl45 quest mob on it8f
INSERT INTO `spawnlist` (`id`, `location`, `count`, `npc_templateid`, `group_id`, `locx`, `locy`, `randomx`, `randomy`, `locx1`, `locy1`, `locx2`, `locy2`, `heading`, `min_respawn_delay`, `max_respawn_delay`, `mapid`, `respawn_screen`, `movement_distance`, `rest`, `near_spawn`) VALUES (82007, '魂的捕食者', 1, 46041, 0, 32703, 32831, 0, 0, 32640, 32768, 32767, 32895, 0, 100, 100, 82, 0, 0, 0, 0);

-- fix the spawn of great spirits bosses in DI
update spawnlist_boss set `count`=1 where id in (50,51,52,53) and `count`=3;
INSERT INTO `spawnlist_boss` (`id`, `location`, `cycle_type`, `count`, `npc_id`, `group_id`, `locx`, `locy`, `randomx`, `randomy`, `locx1`, `locy1`, `locx2`, `locy2`, `heading`, `mapid`, `respawn_screen`, `movement_distance`, `rest`, `spawn_type`, `percentage`) VALUES
(133, '土精靈王', 'Caspa', 1, 45642, 0, 32727, 32749, 0, 0, 32685, 32701, 32770, 32798, 4, 303, 1, 0, 0, 0, 100),
(134, '土精靈王', 'Caspa', 1, 45642, 0, 32727, 32749, 0, 0, 32685, 32701, 32770, 32798, 4, 303, 1, 0, 0, 0, 100),
(135, '水精靈王', 'Caspa', 1, 45643, 0, 32762, 32616, 0, 0, 32717, 32593, 32808, 32640, 4, 303, 1, 0, 0, 0, 100),
(136, '水精靈王', 'Caspa', 1, 45643, 0, 32762, 32616, 0, 0, 32717, 32593, 32808, 32640, 4, 303, 1, 0, 0, 0, 100),
(137, '風精靈王', 'Caspa', 1, 45644, 0, 32624, 32807, 0, 0, 32588, 32739, 32660, 32876, 4, 303, 1, 0, 0, 0, 100),
(138, '風精靈王', 'Caspa', 1, 45644, 0, 32624, 32807, 0, 0, 32588, 32739, 32660, 32876, 4, 303, 1, 0, 0, 0, 100),
(139, '火精靈王', 'Caspa', 1, 45645, 0, 32833, 32778, 0, 0, 32798, 32738, 32869, 32818, 4, 303, 1, 0, 0, 0, 100),
(140, '火精靈王', 'Caspa', 1, 45645, 0, 32833, 32778, 0, 0, 32798, 32738, 32869, 32818, 4, 303, 1, 0, 0, 0, 100);

-- return cancel rate of gaq to 10%
update mobskill set trirnd=10 where skillid=44 and mobid=45614;

-- insert .dm command
INSERT INTO `commands` (`name`, `access_level`, `class_name`) VALUES ('dm', 100, 'L1StartDm');

-- reduce the movement distance of Fairy Queen
update spawnlist_npc set movement_distance=50 where npc_templateid=70852;

-- reinsert boss island tele scroll
delete from etcitem where item_id=42099;
INSERT INTO `etcitem` (`item_id`, `name`, `unidentified_name_id`, `identified_name_id`, `item_type`, `use_type`, `material`, `weight`, `invgfx`, `grdgfx`, `itemdesc_id`, `stackable`, `max_charge_count`, `dmg_small`, `dmg_large`, `min_lvl`, `max_lvl`, `locx`, `locy`, `mapid`, `bless`, `trade`, `cant_delete`, `can_seal`, `delay_id`, `delay_time`, `delay_effect`, `food_volume`, `save_at_once`) VALUES (42099, 'Scroll of Teleportation - Boss Event', '$230 - Boss Event', '$230 - Boss Event', 'scroll', 'normal', 'paper', 630, 551, 22, 0, 1, 0, 0, 0, 0, 0, 32691, 32735, 303, 1, 0, 0, 0, 4, 1500, 0, 0, 0);

-- snowman dolls for christmas event
INSERT INTO `npc` (`npcid`, `name`, `nameid`, `note`, `impl`, `gfxid`, `lvl`, `hp`, `mp`, `ac`, `str`, `con`, `dex`, `wis`, `intel`, `mr`, `exp`, `lawful`, `size`, `weakAttr`, `ranged`, `tamable`, `passispeed`, `atkspeed`, `alt_atk_speed`, `atk_magic_speed`, `sub_magic_speed`, `undead`, `poison_atk`, `paralysis_atk`, `agro`, `agrososc`, `agrocoi`, `family`, `agrofamily`, `agrogfxid1`, `agrogfxid2`, `picupitem`, `digestitem`, `bravespeed`, `hprinterval`, `hpr`, `mprinterval`, `mpr`, `teleport`, `randomlevel`, `randomhp`, `randommp`, `randomac`, `randomexp`, `randomlawful`, `damage_reduction`, `hard`, `doppel`, `IsTU`, `IsErase`, `bowActId`, `karma`, `transform_id`, `transform_gfxid`, `light_size`, `amount_fixed`, `change_head`, `cant_resurrect`, `hascastle`, `spawnlist_door`, `count_map`) VALUES (92114, 'Snowman Magic Doll', '$10149', '魔法娃娃', 'L1Doll', 8751, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 'small', 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '', 0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, NULL, NULL);

INSERT INTO `etcitem` (`item_id`, `name`, `unidentified_name_id`, `identified_name_id`, `item_type`, `use_type`, `material`, `weight`, `invgfx`, `grdgfx`, `itemdesc_id`, `stackable`, `max_charge_count`, `dmg_small`, `dmg_large`, `min_lvl`, `max_lvl`, `locx`, `locy`, `mapid`, `bless`, `trade`, `cant_delete`, `can_seal`, `delay_id`, `delay_time`, `delay_effect`, `food_volume`, `save_at_once`) VALUES 
(349318, 'Magic Doll: Snowman Magic', 'Magic Doll: Snowman Magic', 'Magic Doll: Snowman Magic', 'magic_doll', 'normal', 'wood', 1000, 4196, 6174, 2772, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1),
(349319, 'Magic Doll: Snowman Melee', 'Magic Doll: Snowman Melee', 'Magic Doll: Snowman Melee', 'magic_doll', 'normal', 'wood', 1000, 4196, 6174, 2772, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1),
(349320, 'Magic Doll: Snowman Range', 'Magic Doll: Snowman Range', 'Magic Doll: Snowman Range', 'magic_doll', 'normal', 'wood', 1000, 4196, 6174, 2772, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1);

INSERT INTO `magic_doll` (`item_id`, `note`, `doll_id`, `ac`, `hpr`, `hpr_time`, `mpr`, `mpr_time`, `hit`, `dmg`, `dmg_chance`, `bow_hit`, `bow_dmg`, `dmg_reduction`, `dmg_reduction_chance`, `dmg_evasion_chance`, `weight_reduction`, `regist_stun`, `regist_stone`, `regist_sleep`, `regist_freeze`, `regist_sustain`, `regist_blind`, `make_itemid`, `effect`, `effect_chance`) VALUES 
(349318, 'Magic Doll: Snowman Magic', 92114, 0, 0, 0, 30, 1, 0, 0, 0, 0, 0, 15, 2, 0, 0, 0, 0, 0, 0, 0, 0, 40318, 0, 0),
(349319, 'Magic Doll: Snowman Melee', 92114, 0, 80, 1, 0, 0, 0, 0, 0, 0, 0, 15, 2, 0, 0, 0, 0, 0, 0, 0, 0, 40012, 0, 0),
(349320, 'Magic Doll: Snowman Range', 92114, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 15, 2, 0, 0, 0, 0, 0, 0, 0, 0, 40319, 0, 0);

-- change spawn location of Saell
update spawnlist_npc set locx=32860, locy=32791 where npc_templateid=81292;

