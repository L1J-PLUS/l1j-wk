-- to start the event
INSERT INTO `shop` (`npc_id`, `NPC_Name`, `item_id`, `Item_Name`, `order_id`, `selling_price`, `pack_count`, `purchasing_price`) VALUES (70073, NULL, 20380, NULL, 32, 1000, 0, 2);

-- to end the event
delete from shop where npc_id=70073 and order_id=32 and item_id=20380;
delete from character_items where item_id=20380;
