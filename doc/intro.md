# Data Structures
Play area is a vector of 9 spaces. Each space is a vector which itself holds all the cards
associate with that region.

These are represented in the following ways:
Character card: `[player character]`. Example: `[:p1 :ryu]`
Attack card: `[player facing character card-name]`. Example: `[:p1 :face-up :ryu :hadoken]` For normals the character is :normal

Player input is handled right in the game structure. The `:input-required` keyword stores this state. When calling a function,
if it returns nothing, it ran successfully. If it returns :p1 or :p2, we are awaiting input from them on this. `:response`
is when the input is provided, and contains both the response, and the function to call again to update.

Currently, for change cards action, the format to be returned should be:
[force-amount, spent-cards]
where force-amount is how much it is worth, and spent-cards is a collection of lists of the form [location card]
so that the corresponding cards can be removed

[:force] should return `[[card location] [card location]]`
