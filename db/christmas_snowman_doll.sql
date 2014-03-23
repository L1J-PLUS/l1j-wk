-- to start the event
INSERT INTO `shop` (`npc_id`, `NPC_Name`, `item_id`, `Item_Name`, `order_id`, `selling_price`, `pack_count`, `purchasing_price`) VALUES 
(70030, 'Giran Pot Shop', 349318, 'Snowman Doll', 30, 300, 0, -1),
(70030, 'Giran Pot Shop', 349319, 'Snowman Doll', 31, 300, 0, -1),
(70030, 'Giran Pot Shop', 349320, 'Snowman Doll', 32, 300, 0, -1);

-- to end the event
delete from shop where npc_id=70030 and item_id in (349318, 349319, 349320);
delete from character_items where item_id in (349318, 349319, 349320);


