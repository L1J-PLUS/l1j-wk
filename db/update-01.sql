-- insert shop value for Shiren (the npc which sells elemental enchant scroll and accessory enchant scroll)
INSERT INTO `shop` (`npc_id`, `NPC_Name`, `item_id`, `Item_Name`, `order_id`, `selling_price`, `pack_count`, `purchasing_price`) VALUES
(71264, NULL, 40074, NULL, 0, 31000, 0, 15500),
(71264, NULL, 40087, NULL, 1, 75000, 0, 37500),
(71264, NULL, 41429, NULL, 2, 300000, 0, 100000),
(71264, NULL, 41430, NULL, 3, 300000, 0, 100000),
(71264, NULL, 41431, NULL, 4, 300000, 0, 100000),
(71264, NULL, 41432, NULL, 5, 300000, 0, 100000),
(71264, NULL, 49148, NULL, 6, 300000, 0, 100000);

-- move Consul to his usual Underground passage 1f spot
update spawnlist_npc set locx=32754, locy=32822, mapid=307 where npc_templateid=50127;

-- update access_level for various gm coomands
update commands set access_level=100 where `name` in ('account', 'ipcheck', 'drop', 'who', 'findinvis');

-- increased CB drop rate from Varlok back to 0.5%
update droplist set chance=5000 where itemid=41148 and mobid=45753;

-- slightly buff the drop table of new Antharas and Fafurion
update droplist set chance=300000 where chance=200000 and mobid in (97008, 97046);

-- insert Fiery Coal into Lava Golem's droptable at a low rate 0.1% 
INSERT INTO `droplist` (`mobId`, `mob_name`, `itemId`, `item_name`, `min`, `max`, `chance`) VALUES (45365, 'Lava Golem', 90009, 'Fiery Coal', 1, 1, 1000);

-- make tax rate of all castle 10% (default value)
update castle set tax_rate=10;

-- make all player current in a bp being able to use bp chat
update characters set clanrank=2 where clanid!=0 and clanrank=1;

-- update gold dragon's skills
delete from mobskill where mobid=46046 and actno=0;
INSERT INTO `mobskill` (`mobid`, `actNo`, `mobname`, `Type`, `TriRnd`, `TriHp`, `TriCompanionHp`, `TriRange`, `TriCount`, `ChangeTarget`, `Range`, `AreaWidth`, `AreaHeight`, `Leverage`, `SkillId`, `Gfxid`, `ActId`, `SummonId`, `SummonMin`, `SummonMax`, `PolyId`) VALUES 
(46046, 0, 'Gold Dragon', 2, 50, 0, 0, -8, 0, 0, 0, 0, 0, 20, 8820004, 5214, 19, 0, 0, 0, 0),
(46046, 1, 'Gold Dragon', 2, 50, 0, 0, -2, 0, 0, 2, 1, 2, 10, 8820005, 6398, 18, 0, 0, 0, 0);

-- update town names
update town set `name`='Talking Island' where town_id=1;
update town set `name`='Silver Knight Town' where town_id=2;
update town set `name`='Gludio' where town_id=3;
update town set `name`='Orc Town' where town_id=4;
update town set `name`='Windawood' where town_id=5;
update town set `name`='Kent' where town_id=6;
update town set `name`='Giran' where town_id=7;
update town set `name`='Heine' where town_id=8;
update town set `name`='Weldern' where town_id=9;
update town set `name`='Oren' where town_id=10;

-- update shop list for fishing old man
delete from shop where npc_id=80080;
INSERT INTO `shop` (`npc_id`, `item_id`, `order_id`, `selling_price`, `pack_count`, `purchasing_price`) VALUES
(80080, 41293, 0, 10000, 1, -1),
(80080, 47103, 1, 20000, 400, -1),
(80080, 41295, 2, -1, 1, 25),
(80080, 41296, 3, -1, 0, 5),
(80080, 41297, 4, -1, 0, 5),
(80080, 41298, 5, -1, 0, 20),
(80080, 41299, 6, -1, 0, 30),
(80080, 41300, 7, -1, 0, 40),
(80080, 41301, 8, -1, 0, 50),
(80080, 41302, 9, -1, 0, 50),
(80080, 41303, 10, -1, 0, 50),
(80080, 41304, 11, -1, 0, 50);

-- giran dm update remove kurtz from 2nd group
update spawnlist_ub set npc_templateid=45286 where npc_templateid=45600 and id=279;

-- remove exp from training dummy to avoid possible exp exploit
update npc set `exp`=0 where npcid in (45001, 45002, 45003, 45004);

-- insert some custom drops back into the drop list
INSERT INTO `droplist` (`mobId`, `mob_name`, `itemId`, `item_name`, `min`, `max`, `chance`) VALUES 
(45642, 'Great Spirit of Earth', 41430, 'Scroll of Enchant Weapon: Earth', 1, 1, 75000),
(45592, 'Abyss Earth Spirit', 41430, 'Scroll of Enchant Weapon: Earth', 1, 1, 9000),
(45508, 'Earth Spirit Master', 41430, 'Scroll of Enchant Weapon: Earth', 1, 1, 7000),
(45619, 'Great Spirit of Earth', 41430, 'Scroll of Enchant Weapon: Earth', 1, 1, 18000),
(45645, 'Great Spirit of Fire', 41432, 'Scroll of Enchant Weapon: Fire', 1, 1, 75000),
(45511, 'Fire Spirit Master', 41432, 'Scroll of Enchant Weapon: Fire', 1, 1, 7000),
(45622, 'Great Spirit of Fire', 41432, 'Scroll of Enchant Weapon: Fire', 1, 1, 18000),
(45594, 'Abyss Fire Spirit', 41432, 'Scroll of Enchant Weapon: Fire', 1, 1, 9000),
(45500, 'Water Spirit Master', 41431, 'Scroll of Enchant Weapon: Water', 1, 1, 7000),
(45620, 'Great Spirit of Water', 41431, 'Scroll of Enchant Weapon: Water', 1, 1, 18001),
(45591, 'Abyss Water Spirit', 41431, 'Scroll of Enchant Weapon: Water', 1, 1, 9000),
(45643, 'Great Spirit of Water', 41431, 'Scroll of Enchant Weapon: Water', 1, 1, 75000),
(45644, 'Great Spirit of Wind', 41429, 'Scroll of Enchant Weapon: Wind', 1, 1, 75000),
(45621, 'Great Spirit of Wind', 41429, 'Scroll of Enchant Weapon: Wind', 1, 1, 18000),
(45510, 'Wind Spirit Master', 41429, 'Scroll of Enchant Weapon: Wind', 1, 1, 7000),
(45593, 'Abyss Wind Spirit', 41429, 'Scroll of Enchant Weapon: Wind', 1, 1, 9000),
(45601, 'Death Knight', 66, 'Dragon Slayer', 1, 1, 1200),
(45680, 'Ken Rauhel', 66, 'Dragon Slayer', 1, 1, 1200),
(45649, 'Demon', 66, 'Dragon Slayer', 1, 1, 1200);

-- made summon/pets usable in tikal
update mapids set take_pets=1, recall_pets=1 where mapid in (783, 784);

-- fix joy of pain
update skills set buffduration=0, `type`=64 where skill_id=218;

-- fix panic
update skills set probability_value=30, probability_dice=30 where skill_id=217;

-- add gatekeeper for orc fort
INSERT INTO `spawnlist_npc` (`id`, `location`, `count`, `npc_templateid`, `locx`, `locy`, `randomx`, `randomy`, `heading`, `respawn_delay`, `mapid`, `movement_distance`) VALUES (1900020, 'Oricish Gatekeeper', 1, 70600, 32788, 32324, 0, 0, 4, 0, 4, 100);

-- reduce the respawn delay of DK lvl15 quest mobs
update spawnlist set min_respawn_delay=300, max_respawn_delay=600 where npc_templateid in (46157, 46158, 46159);

-- reduce the physical attack skill trigger rate for lesser demon in toi
update mobskill set trirnd=40 where mobid=45481 and actno=1;

-- add elemental enchant scrolls back to dragons' drop table
INSERT INTO `droplist` (`mobId`, `mob_name`, `itemId`, `item_name`, `min`, `max`, `chance`) VALUES 
(45684, 'Valakas', 41432, 'Scroll of Enchant Weapon: Fire', 3, 6, 300000),
(45681, 'Lindvior', 41429, 'Scroll of Enchant Weapon: Wind', 3, 6, 300000),
(97008, 'Antharas', 41430, 'Scroll of Enchant Weapon: Earth', 3, 6, 300000),
(97046, 'Fafurion', 41431, 'Scroll of Enchant Weapon: Water', 3, 6, 300000);

-- add more physical attack skills to Antharas and Fafurion
INSERT INTO `mobskill` (`mobid`, `actNo`, `mobname`, `Type`, `mpConsume`, `TriRnd`, `TriHp`, `TriCompanionHp`, `TriRange`, `TriCount`, `ChangeTarget`, `Range`, `AreaWidth`, `AreaHeight`, `Leverage`, `SkillId`, `SkillArea`, `Gfxid`, `ActId`, `SummonId`, `SummonMin`, `SummonMax`, `PolyId`) VALUES 
(97008, 10, 'Antharas (3) Bite', 1, 0, 100, 60, 0, -3, 0, 3, 3, 3, 4, 200, 0, 0, 0, 1, 0, 0, 0, 0),
(97046, 16, 'Fafurion (3) Stand', 1, 0, 100, 60, 0, -3, 0, 3, 3, 3, 4, 200, 0, 0, 0, 47, 0, 0, 0, 0);

-- skill adjustment to Hellbound
update mobskill set trirnd=100, leverage=30 where mobid=45512 and actno=0;

-- reduce drop rate of junks from Black knights from SKT dungeon
update droplist set chance=100 where mobid=81319 and itemid in (100095, 20043, 20162, 93, 94, 100103, 20213);
update droplist set chance=100 where mobid=81320 and itemid in (20043, 100095, 100103, 93, 94);

-- insert .mob back into gm command
INSERT INTO `commands` (`name`, `access_level`, `class_name`) VALUES ('mob', 100, 'L1MobDrops');

-- fix the learning of Strikers Gale
update skills set `name`='Strikers Gale' where skill_id=174;

-- change Varlok's attack range to 1
update npc set ranged=1 where npcid in (45752, 45753);

-- correct the hp of Yahee fly type and Demon of Varlok
update npc set hp=30000 where npcid=81082;
update npc set hp=1000 where npcid=45647;

-- add possible missing dummy item from fishing
INSERT INTO `armor` (`item_id`, `name`, `unidentified_name_id`, `identified_name_id`, `type`, `material`, `weight`, `invgfx`, `grdgfx`, `itemdesc_id`, `ac`, `safenchant`, `use_royal`, `use_knight`, `use_mage`, `use_elf`, `use_darkelf`, `use_dragonknight`, `use_illusionist`, `add_str`, `add_con`, `add_dex`, `add_int`, `add_wis`, `add_cha`, `add_hp`, `add_mp`, `add_hpr`, `add_mpr`, `add_sp`, `min_lvl`, `max_lvl`, `m_def`, `haste_item`, `damage_reduction`, `weight_reduction`, `hit_modifier`, `dmg_modifier`, `bow_hit_modifier`, `bow_dmg_modifier`, `bless`, `trade`, `cant_delete`, `max_use_time`, `defense_water`, `defense_wind`, `defense_fire`, `defense_earth`, `regist_stun`, `regist_stone`, `regist_sleep`, `regist_freeze`, `regist_sustain`, `regist_blind`, `grade`) VALUES 
(21140, 'Wet Hat', '$7507', '$7507', 'helm', 'leather', 20000, 3609, 7519, 0, -1, 4, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1),
 (21141, 'Wet Hood', '$7508', '$7508', 'helm', 'leather', 20000, 3610, 7520, 0, -1, 4, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1);

-- changed jindo puppy/dog's attack range to 2 (according to live)
update npc set ranged=2 where npcid in (45711, 45712);

-- make fish of aura usable
update etcitem set item_type='treasure_box' where item_id in (41301, 41302, 41303, 41304);

-- buff some mobs in lasta 3f and 4f
-- Spiritologist of Blood 3f
delete from mobskill where mobid=45517;
INSERT INTO `mobskill` (`mobid`, `actNo`, `mobname`, `Type`, `mpConsume`, `TriRnd`, `TriHp`, `TriCompanionHp`, `TriRange`, `TriCount`, `ChangeTarget`, `Range`, `AreaWidth`, `AreaHeight`, `Leverage`, `SkillId`, `SkillArea`, `Gfxid`, `ActId`, `SummonId`, `SummonMin`, `SummonMax`, `PolyId`) VALUES 
(45517, 0, 'Spiritologist of Blood', 2, 0, 70, 0, 0, -8, 0, 0, 0, 0, 0, 0, 8810086, 0, 0, 0, 0, 0, 0, 0),
(45517, 1, 'Spiritologist of Blood', 2, 0, 30, 0, 0, -7, 0, 0, 0, 0, 0, 0, 8810087, 0, 0, 0, 0, 0, 0, 0);

-- Elder Attendant 4f
update mobskill set `range`=8, trirange=-8, leverage=40 where mobname='長老隨從-光球:闇';

-- Spiritologist of Blood 4f
update mobskill set leverage=40 where mobname='血色術師-寒冰氣息';

-- buff Cerenis
update mobskill set leverage=120 where mobid=45678 and actno=0;

-- DK skill adjustment
update skills set hpConsume=20, buffDuration=32 where name="GuardBreak";
update skills set hpConsume=12, buffDuration=1800 where name="DragonSkin";
update skills set hpConsume=6 where name="BurningSlash";
update skills set buffDuration=300 where name="Bloodlust";
update skills set hpConsume=18 where name="ShockSkin";
update skills set hpConsume=16 where name="FoeSlayer";
update skills set hpConsume=12, buffDuration=16 where name="ResistFear";
update skills set hpConsume=35 where name="ThunderGrab";
update skills set hpConsume=16 where name="FreezingBreath";
update skills set hpConsume=20, buffDuration=32 where name="HorrorOfDeath";
update skills set hpConsume=50 where name="mortalbody";

-- update stat of Hidden Demon Chain sword
update weapon set dmg_large=28, hitmodifier=8, dmgmodifier=1 where item_id=308;

-- fix lesser dragon aoe skill
delete from mobskill where mobid=45496;
INSERT INTO `mobskill` (`mobid`, `actNo`, `mobname`, `Type`, `TriRnd`, `TriHp`, `TriCompanionHp`, `TriRange`, `TriCount`, `ChangeTarget`, `Range`, `AreaWidth`, `AreaHeight`, `Leverage`, `SkillId`, `Gfxid`, `ActId`, `SummonId`, `SummonMin`, `SummonMax`, `PolyId`) VALUES (45496, 0, 'Lesser Dragon', 2, 100, 0, 0, -2, 0, 0, 2, 1, 2, 0, 8810008, 2510, 18, 0, 0, 0, 0);

-- add in missing exp for some summoned mobs
update npc set `exp`=325 where npcid in (46038, 46039, 46040);

-- fixed the skeleton from bapho's summon
update mobskill set summonid=45107, summonmin=3 where mobid=46190 and summonid=91266;

-- adjust summon amount by Beleth
update mobskill set summonmin=3 where mobid=46187 and summonid=81088;

-- demon's shadow shouldn't be able to cancel
delete from mobskill where mobid=46195 and actno=1;
update mobskill set actno=actno-1 where mobid=46195 and actno in (2,3);

-- reduce the monster trap on ivory tower 6 7 8f by 30%
update spawnlist_trap set count=count*0.7 where mapid in (80,81,82);

-- buff the skill of Elder attendent (queries above doesn't work on wk)
update mobskill set `range`=8, trirange=-8, leverage=40 where mobid in (45995,45999,46003,46004,46007) and actno=0;
update mobskill set `range`=8, trirange=-8, leverage=40 where mobid in (45994,45998,46002) and actno=1;

-- buff Spiritologist of Blood 4f
update mobskill set leverage=40 where mobid=46000 and actno=0;

-- insert boss items to ivory tower mob at very low rate (twice of elixer drop rate)
INSERT INTO `droplist` (`mobId`, `mob_name`, `itemId`, `item_name`, `min`, `max`, `chance`) VALUES 
(46181, 'Dark Elder', 20218, 'Sandals of Dark Elder', 1, 1, 20),
(46181, 'Dark Elder', 20160, 'Robe of Dark Elder', 1, 1, 200),
(46187, 'Beleth\'s Shadow', 20204, 'Boots of Beleth', 1, 1, 20),
(46187, 'Beleth\'s Shadow', 110, 'Staff of Beleth', 1, 1, 20),
(46190, 'Baphomet\'s Shadow', 109, 'Staff of Baphomet', 1, 1, 20),
(46190, 'Baphomet\'s Shadow', 20117, 'Armor of Baphomet', 1, 1, 20),
(46195, 'Demon\'s Shadow', 165, 'Devil\'s Claw', 1, 1, 80),
(46195, 'Demon\'s Shadow', 63, 'Devil\'s Knife', 1, 1, 80),
(46195, 'Demon\'s Shadow', 85, 'Devil\'s Edoryu', 1, 1, 80),
(46195, 'Demon\'s Shadow', 185, 'Devil\'s Crossbow', 1, 1, 80);

-- fix the boss spawn pattern of Dark Elder (was spawning like Caspa)
update spawnlist_boss set cycle_type='DarkElder' where npc_id=45545 and id in (31, 32);

-- reduce the drop rate of adv and ab from mimic
update droplist set chance=350 where mobid=46169 and itemid in (40224, 40223);

-- correct drop rate of monster breath on mimic and paper man
update droplist set chance=5000 where mobid in (46168, 46169) and itemid=50564;

-- insert Alligator Meat into more types of alligator's drop table
INSERT INTO `droplist` (`mobId`, `mob_name`, `itemId`, `item_name`, `min`, `max`, `chance`) VALUES
(45026, 'Mutated Alligator', 49041, 'Alligator Meat', 1, 1, 5000),
(45096, 'Island Crocodile', 49041, 'Alligator Meat', 1, 1, 5000),
(45101, 'Alligator', 49041, 'Alligator Meat', 1, 1, 5000),
(45196, 'Alligator', 49041, 'Alligator Meat', 1, 1, 5000),
(45338, 'Crocodile', 49041, 'Alligator Meat', 1, 1, 5000),
(45469, 'Crocodile', 49041, 'Alligator Meat', 1, 1, 5000),
(45814, 'Mutated Alligator', 49041, 'Alligator Meat', 1, 1, 5000);

-- make stat boots not tradable
update armor set trade=1 where item_id>=30015 and item_id<=30030;

-- evil monkey
INSERT INTO `npc` (`npcid`, `name`, `nameid`, `note`, `impl`, `gfxid`, `lvl`, `hp`, `mp`, `ac`, `str`, `con`, `dex`, `wis`, `intel`, `mr`, `exp`, `lawful`, `size`, `weakAttr`, `ranged`, `tamable`, `passispeed`, `atkspeed`, `alt_atk_speed`, `atk_magic_speed`, `sub_magic_speed`, `undead`, `poison_atk`, `paralysis_atk`, `agro`, `agrososc`, `agrocoi`, `family`, `agrofamily`, `agrogfxid1`, `agrogfxid2`, `picupitem`, `digestitem`, `bravespeed`, `hprinterval`, `hpr`, `mprinterval`, `mpr`, `teleport`, `randomlevel`, `randomhp`, `randommp`, `randomac`, `randomexp`, `randomlawful`, `damage_reduction`, `hard`, `doppel`, `IsTU`, `IsErase`, `bowActId`, `karma`, `transform_id`, `transform_gfxid`, `light_size`, `amount_fixed`, `change_head`, `cant_resurrect`, `hascastle`, `spawnlist_door`, `count_map`) VALUES (97076, 'Evil Monkey', 'Evil Monkey', 'boot event', 'L1Monster', 7285, 40, 1000, 10, -10, 40, 15, 15, 15, 15, 150, 400, -200, 'small', 0, 1, 0, 480, 960, 1160, 960, 960, 0, 0, 0, 0, 0, 0, '', 0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, NULL, NULL);

-- evil monkey's drop
INSERT INTO `droplist` (`mobId`, `mob_name`, `itemId`, `item_name`, `min`, `max`, `chance`) VALUES 
(97076, 'Evil Monkey', 90011, 'Evil Monkey\'s Bag', 1, 1, 5000),
(97076, 'Evil Monkey', 40308, 'Adena', 100, 150, 1000000);

-- event item
INSERT INTO `etcitem` (`item_id`, `name`, `unidentified_name_id`, `identified_name_id`, `item_type`, `use_type`, `material`, `weight`, `invgfx`, `grdgfx`, `itemdesc_id`, `stackable`, `max_charge_count`, `dmg_small`, `dmg_large`, `min_lvl`, `max_lvl`, `locx`, `locy`, `mapid`, `bless`, `trade`, `cant_delete`, `can_seal`, `delay_id`, `delay_time`, `delay_effect`, `food_volume`, `save_at_once`) VALUES 
(90011, 'Evil Monkey\'s Bag', 'Evil Monkey\'s Bag', 'Evil Monkey\'s Bag', 'treasure_box', 'normal', 'leather', 2000, 1396, 3461, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1),
(90012, 'Shining Banana', 'Shining Banana', 'Shining Banana', 'treasure_box', 'normal', 'vegetation', 2000, 127, 80, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

-- inserted into spawnlist with count 0
INSERT INTO `spawnlist` (`id`, `location`, `count`, `npc_templateid`, `group_id`, `locx`, `locy`, `randomx`, `randomy`, `locx1`, `locy1`, `locx2`, `locy2`, `heading`, `min_respawn_delay`, `max_respawn_delay`, `mapid`, `respawn_screen`, `movement_distance`, `rest`, `near_spawn`) VALUES (801500439, 'Evil Monkey', 0, 97076, 0, 33343, 32767, 959, 767, 32384, 32000, 34303, 33535, 7, 100, 200, 4, 0, 100, 0, 0);

-- buff the nutrition of b-banana
update etcitem set food_volume=1200 where item_id=140062;

-- reload bugs table to make -bugs in game work
CREATE TABLE IF NOT EXISTS `bugs` (
  `id` int(13) unsigned NOT NULL AUTO_INCREMENT,
  `bugtext` varchar(255) NOT NULL DEFAULT '',
  `charname` varchar(45) NOT NULL DEFAULT '',
  `mapID` int(6) NOT NULL DEFAULT '0',
  `mapX` int(6) NOT NULL DEFAULT '0',
  `mapY` int(6) NOT NULL DEFAULT '0',
  `resolved` int(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- more evil monkey's drop
INSERT INTO `droplist` (`mobId`, `mob_name`, `itemId`, `item_name`, `min`, `max`, `chance`) VALUES 
(97076, 'Evil Monkey', 40062, 'Banana', 1, 1, 100000),
(97076, 'Evil Monkey', 40068, 'Elven Wafer', 1, 1, 50000),
(97076, 'Evil Monkey', 49158, 'Forbidden Fruit', 1, 1, 50000),
(97076, 'Evil Monkey', 40031, 'Native`s Totem', 1, 1, 50000),
(97076, 'Evil Monkey', 40014, 'Potion of Bravery', 1, 1, 50000),
(97076, 'Evil Monkey', 40405, 'Fur', 1, 1, 100000),
(97076, 'Evil Monkey', 49138, 'Chocolate Cake', 1, 1, 10);

-- correct name of devil's blood
update etcitem set `name`='Devil`s Blood' where item_id=40031;
update droplist set item_name='Devil`s Blood' where itemid=40031;

-- restore spawn count in toi boss floors up to 70f (was originally reduced by 20%)
update spawnlist set `count`=`count`*1.25 where mapid in (110,120,130,140,150,160,170);

-- Disguised Monkey
INSERT INTO `npc` (`npcid`, `name`, `nameid`, `note`, `impl`, `gfxid`, `lvl`, `hp`, `mp`, `ac`, `str`, `con`, `dex`, `wis`, `intel`, `mr`, `exp`, `lawful`, `size`, `weakAttr`, `ranged`, `tamable`, `passispeed`, `atkspeed`, `alt_atk_speed`, `atk_magic_speed`, `sub_magic_speed`, `undead`, `poison_atk`, `paralysis_atk`, `agro`, `agrososc`, `agrocoi`, `family`, `agrofamily`, `agrogfxid1`, `agrogfxid2`, `picupitem`, `digestitem`, `bravespeed`, `hprinterval`, `hpr`, `mprinterval`, `mpr`, `teleport`, `randomlevel`, `randomhp`, `randommp`, `randomac`, `randomexp`, `randomlawful`, `damage_reduction`, `hard`, `doppel`, `IsTU`, `IsErase`, `bowActId`, `karma`, `transform_id`, `transform_gfxid`, `light_size`, `amount_fixed`, `change_head`, `cant_resurrect`, `hascastle`, `spawnlist_door`, `count_map`) VALUES 
(97077, 'Disguised Monkey', 'Disguised Monkey', 'NPC', 'L1Merchant', 8162, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '', 0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 1, 0, 0, NULL, NULL);

-- spawn them at skt and giran
INSERT INTO `spawnlist_npc` (`id`, `location`, `count`, `npc_templateid`, `locx`, `locy`, `randomx`, `randomy`, `heading`, `respawn_delay`, `mapid`, `movement_distance`) VALUES 
(89971, 'Disguised Monkey', 1, 97077, 33074, 33399, 0, 0, 6, 0, 4, 0),
(89972, 'Disguised Monkey', 1, 97077, 33438, 32812, 0, 0, 6, 0, 4, 0);

-- npcaction for disguided monkey
INSERT INTO `npcaction` (`npcid`, `normal_action`, `caotic_action`, `teleport_url`, `teleport_urla`) VALUES (97077, 'evilmonkey', '', '', '');

-- remove bag from droplist
delete from droplist where itemid=90011;

-- change the drop rate of shining banana to roughly 50%
update droplist set chance=166700 where itemid=90012;

-- fixed some bosses being resurrectable
update npc set cant_resurrect=1 where npcid in (81163,45753,45678,95018,95019,46123,46124,81082,45677,45641);

-- re-load character_pvp table
CREATE TABLE IF NOT EXISTS `character_pvp` (
  `killer_char_obj_id` int(11) DEFAULT NULL,
  `killer_char_name` varchar(20) DEFAULT NULL,
  `killer_lvl` int(11) DEFAULT NULL,
  `victim_char_obj_id` int(11) DEFAULT NULL,
  `victim_char_name` varchar(20) DEFAULT NULL,
  `victim_lvl` int(11) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `locx` int(11) DEFAULT NULL,
  `locy` int(11) DEFAULT NULL,
  `mapid` int(11) DEFAULT NULL,
  `penalty` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- make gold dragon lvl-up hp/mp gain same as before
update pettypes set hpupmin=5, hpupmax=8, mpupmin=3, mpupmax=5 where basenpcid=46046;

-- iq earring buff
update armor set defense_water=5, regist_freeze=5 where item_id=21081;
update armor set defense_water=5, regist_freeze=5, add_hp=20 where item_id=21082;
update armor set defense_water=5, regist_freeze=5, add_hp=20, add_mp=10 where item_id=21083;
update armor set defense_water=5, regist_freeze=5, add_hp=30, add_mp=10 where item_id=21084;
update armor set defense_water=5, regist_freeze=5, add_hp=30, add_mp=10, add_hpr=2 where item_id=21085;
update armor set defense_water=10, regist_freeze=5, add_hp=30, add_mp=20, add_hpr=2 where item_id=21086;
update armor set defense_water=10, regist_freeze=5, add_hp=40, add_mp=20, add_hpr=2, add_mpr=1 where item_id=21087;
update armor set defense_water=10, regist_freeze=5, add_hp=40, add_mp=20, add_hpr=4, add_mpr=2 where item_id=21088;
update armor set defense_water=10, regist_freeze=5, add_hp=50, add_mp=30, add_hpr=8, add_mpr=4 where item_id in (21089,21090,21091);

-- spawn tigers like before
INSERT INTO `spawnlist` (`id`, `location`, `count`, `npc_templateid`, `group_id`, `locx`, `locy`, `randomx`, `randomy`, `locx1`, `locy1`, `locx2`, `locy2`, `heading`, `min_respawn_delay`, `max_respawn_delay`, `mapid`, `respawn_screen`, `movement_distance`, `rest`, `near_spawn`) VALUES 
(801500440, 'Tiger', 1, 45313, 0, 32808, 32845, 12, 12, 0, 0, 0, 0, 4, 20, 20, 480, 0, 0, 0, 0),
(801500441, 'Tiger', 1, 45313, 0, 32807, 32837, 12, 12, 0, 0, 0, 0, 3, 20, 20, 480, 0, 0, 0, 0),
(801500442, 'Tiger', 1, 45313, 0, 32729, 32853, 12, 12, 0, 0, 0, 0, 4, 20, 20, 480, 0, 0, 0, 0);

-- reduce the the spawn count of tiger in boss spawnlist to 1
update spawnlist_boss set `count`=1 where npc_id=45313;

-- add restart to town for several boss dungeons
INSERT INTO `getback_restart` (`area`, `note`, `locx`, `locy`, `mapid`) VALUES (24, 'Windawood Castle Dungeon 2F', 32628, 33204, 4);
INSERT INTO `getback_restart` (`area`, `note`, `locx`, `locy`, `mapid`) VALUES (73, 'Ice Queens Castle 2F', 34061, 32276, 4);
INSERT INTO `getback_restart` (`area`, `note`, `locx`, `locy`, `mapid`) VALUES (74, 'Ice Queens Castle 3F', 34061, 32276, 4);
INSERT INTO `getback_restart` (`area`, `note`, `locx`, `locy`, `mapid`) VALUES (543, 'Lair of Giant Ant Queen', 32628, 33204, 4);
INSERT INTO `getback_restart` (`area`, `note`, `locx`, `locy`, `mapid`) VALUES (603, 'Varloks Hideout', 32628, 33204, 4);

-- changed the Heine guards in the spawnlist to the correct ones
update spawnlist_npc set npc_templateid=70857 where npc_templateid=60563;

-- return Yahee fly type to old strength
delete from mobskill where mobid=81082;
INSERT INTO `mobskill` (`mobid`, `actNo`, `mobname`, `Type`, `mpConsume`, `TriRnd`, `TriHp`, `TriCompanionHp`, `TriRange`, `TriCount`, `ChangeTarget`, `Range`, `AreaWidth`, `AreaHeight`, `Leverage`, `SkillId`, `SkillArea`, `Gfxid`, `ActId`, `SummonId`, `SummonMin`, `SummonMax`, `PolyId`) VALUES 
(81082, 0, 'Yahee', 1, 0, 100, 0, 0, -15, 1, 0, 15, 0, 0, 20, 0, 0, 0, 4, 0, 0, 0, 0),
(81082, 1, 'Yahee', 4, 0, 15, 0, 0, -10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3872),
(81082, 2, 'Yahee', 2, 0, 30, 0, 0, -10, 0, 0, 0, 0, 0, 35, 8810063, 0, 0, 0, 0, 0, 0, 0),
(81082, 3, 'Yahee', 2, 0, 100, 0, 0, -10, 0, 0, 0, 0, 0, 35, 8810064, 0, 0, 0, 0, 0, 0, 0);

-- made halloween blessing cap not tradable and usable by dk and illu
update armor set trade=1, use_dragonknight=1, use_illusionist=1 where item_id=20380;

-- modified mobskills of jack-o-laterns 
update mobskill set TriRnd=100 where mobid in (45166, 45167);

