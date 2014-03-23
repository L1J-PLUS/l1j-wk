-- delete old mobskills
delete from mobskill where mobid in (45581,45586,45604,45589,45619,45620,45621,45622,45595,45616,45664);

-- insert mobskills of normal mobs from toi 81f+ with 20% nerf in leverage to compensate for no fast running poly
INSERT INTO `mobskill` (`mobid`, `actNo`, `mobname`, `Type`, `mpConsume`, `TriRnd`, `TriHp`, `TriCompanionHp`, `TriRange`, `TriCount`, `ChangeTarget`, `Range`, `AreaWidth`, `AreaHeight`, `Leverage`, `SkillId`, `SkillArea`, `Gfxid`, `ActId`, `SummonId`, `SummonMin`, `SummonMax`, `PolyId`) VALUES 
(45581, 0, 'Insolent Zenith Queen', 2, 0, 40, 0, 0, -6, 0, 0, 0, 0, 0, 8, 8810004, 0, 0, 0, 0, 0, 0, 0),
(45581, 1, 'Insolent Zenith Queen', 1, 0, 100, 0, 0, -2, 0, 0, 2, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0),
(45586, 0, 'Lesser Seer', 2, 0, 40, 0, 0, -6, 0, 0, 0, 0, 0, 12, 8810001, 0, 0, 0, 0, 0, 0, 0),
(45586, 1, 'Lesser Seer', 1, 0, 100, 0, 0, -1, 0, 0, 1, 0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0),
(45589, 0, 'Zombie Lord of Fear', 2, 0, 30, 0, 0, -6, 0, 0, 0, 0, 0, 16, 8810000, 0, 0, 0, 0, 0, 0, 0),
(45589, 1, 'Zombie Lord of Fear', 1, 0, 100, 0, 0, -2, 0, 0, 2, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0),
(45595, 0, 'Iris', 2, 0, 30, 0, 0, -4, 0, 0, 0, 0, 0, 64, 53, 0, 0, 0, 0, 0, 0, 0),
(45595, 1, 'Iris', 1, 0, 100, 0, 0, -3, 0, 0, 3, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0),
(45604, 0, 'Marquise Vampire', 2, 0, 40, 0, 0, -6, 0, 0, 0, 0, 0, 8, 8810006, 0, 0, 0, 0, 0, 0, 0),
(45604, 1, 'Marquise Vampire', 2, 0, 40, 0, 0, -3, 0, 0, 0, 0, 0, 48, 28, 0, 0, 0, 0, 0, 0, 0),
(45604, 2, 'Marquise Vampire', 1, 0, 100, 0, 0, -1, 0, 0, 1, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0),
(45616, 0, 'Mummy Lord', 1, 0, 100, 0, 0, -1, 0, 0, 1, 0, 0, 32, 0, 0, 0, 0, 0, 0, 0, 0),
(45619, 0, 'Great Spirit of Earth', 2, 0, 30, 0, 0, -8, 0, 0, 0, 0, 0, 16, 8810020, 0, 0, 0, 0, 0, 0, 0),
(45619, 1, 'Great Spirit of Earth', 1, 0, 100, 0, 0, -2, 0, 0, 2, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0),
(45620, 0, 'Great Spirit of Water', 2, 0, 30, 0, 0, -8, 0, 0, 0, 0, 0, 16, 8810022, 0, 0, 0, 0, 0, 0, 0),
(45620, 1, 'Great Spirit of Water', 1, 0, 100, 0, 0, -2, 0, 0, 2, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0),
(45621, 0, 'Great Spirit of Wind', 2, 0, 30, 0, 0, -4, 0, 0, 0, 0, 0, 40, 53, 0, 0, 0, 0, 0, 0, 0),
(45621, 1, 'Great Spirit of Wind', 1, 0, 100, 0, 0, -2, 0, 0, 2, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0),
(45622, 0, 'Great Spirit of Fire', 2, 0, 30, 0, 0, -8, 0, 0, 0, 0, 0, 16, 8810021, 0, 0, 0, 0, 0, 0, 0),
(45622, 1, 'Great Spirit of Fire', 1, 0, 100, 0, 0, -2, 0, 0, 2, 0, 0, 24, 0, 0, 0, 18, 0, 0, 0, 0),
(45664, 0, 'Knight Vald', 1, 0, 100, 0, 0, -2, 0, 0, 2, 0, 0, 36, 0, 0, 0, 0, 0, 0, 0, 0);


