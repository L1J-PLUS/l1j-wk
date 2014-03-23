-- to start the event
update spawnlist set `count`=2000 where id=801500439 and npc_templateid=97076;
update spawnlist_npc set `count`=1 where npc_templateid=97077;

-- to end the event
update spawnlist set `count`=0 where id=801500439 and npc_templateid=97076;
update spawnlist_npc set `count`=0 where npc_templateid=97077;
