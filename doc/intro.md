# Data Structures
Play area is a vector of 9 spaces. Each space is a vector which itself holds all the cards
associate with that region.

These are represented in the following ways:
Character card: `[player character]`. Example: `[:p1 :ryu]`
Attack card: `[player facing character card-name]`. Example: `[:p1 :face-up :ryu :hadoken]` For normals the character is :normal
