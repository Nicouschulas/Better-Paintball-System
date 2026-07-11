# **Better Paintball System: The Next Level Paintball plugin!**

badges & icon wip

## **❓ What is BPS?**
**Better Paintball System** is an action-packed minigame plugin where two teams face off in an intense snowball fight! Both teams start with a set amount of lives. Every time a player gets hit and defeated, their team loses a life. The first team to hit 0 lives loses the game.
This plugin is **fully configurable**, easy to set up, and packed with gameplay features like KillStreaks, unique Hats, and permanent Perks to keep your players hooked!

## **🌟 Key Features:**
💣 **KillStreaks, Hats & Perks**:
<details>
<summary>8 different KillStreaks</summary>
- More Snowballs (You will receive extra snowballs)
- Strong Arm (You will throw snowballs at high speed)
- Triple Shoot (You will throw 3 snowballs at a time)
- +3 Lives (Regain 3 Lives for your team)
- Teleport (Teleports you to your death location)
- Lightning Strike (Kills your sorrounding enemies in a radius)
- Nuke (Kills all of your enemies)
- Fury Mode (You will be immune to snowballs, and you won't consume them)
</details>

<details>
<summary>9 unique Hats that grant special abilities during the game</summary>
- Speed Hat (Gives you permanent Speed I)
- Jump Hat (Gives you Jump I when sneaking for X seconds)
- Protector Hat (Gives you a small chance of dodging snowballs hits)
- Time Hat (All of your killstreaks last X more seconds)
- Guardian Hat (Gives you Invulnerability and Slowness III when sneaking for X seconds)
- Present Hat (At the start of the game one of your teammates will receive 3 killcoins)
- Assassin Hat (Killing the user who last killed you will give you an extra killcoin)
- Chicken Hat (You will shoot eggs instead of snowballs)
- Explosive Hat (Gives you a small chance of exploding when you are killed, killing nearby enemies)
</details>
 
<details>
<summary>3 permanent Perks with upgradable levels</summary>
- Extra Lives (Allows you to start the game with additional lives)
- Initial Killcoins (Allows you to start the game with additional killcoins)
- Extra Killcoins (Allows you to receive additional killcoins when doing a kill)
</details>

📊 **Top Leaderboard Holograms**: Display global, monthly, or weekly leaderboards for top wins and kills using DecentHolograms integration

💰 **Flexible Economy & Storage**: Supports 3 types of economies (Internal Paintball Coins, Vault, or TokenManager) and comes with MySQL database support to save player statistics (wins, losses, ties, kills)

💬 **Customizable Messages**: Tailor nearly all plugin messages to match your server's style

⚙️ **Fully Configurable**: Customize cooldowns, game timers, and team lives. Personalize team names, colors, and display items to fit your server's theme

🧩 **Even more feuatures!**: Features built-in PlaceholderAPI support for custom scoreboards/chat, a per-arena chat system, play-again items, a command whitelist and an API

**So what are you waiting for? Get Better Painbtall System now and bring a new layer of excitement to your server!**

## **📥 Downloads:**
Modrinth: https://modrinth.com/plugin/better-paintball-system/

Curseforge: https://www.curseforge.com/minecraft/bukkit-plugins/bps-better-paintball-system

Spigot: https://www.spigotmc.org/resources/bps-better-paintball-system.136911/

## 🛠️ **How to Create an Arena:**
 1. Set the main minigame lobby using  ```/paintball setmainlobby```.
 2. Create your arena with ```/paintball create <arena_name>```.
 3. Use ```/paintball edit <arena_name>``` to open the interactive **GUI Setup Menu**.
 4. Set the **Arena Lobby** (where players wait for the game to start) by clicking the first item.
 5. Set the **Spawnpoints** for Team 1 and Team 2 using the second and third items.
 6. Use the remaining GUI items to tweak game time, starting lives, player limits, and team colors.
 7. Enable the arena using ```/paintball enable <arena_name>```.
 8. Optional you can **create a Join Sign** using this format:
```
[Paintball]
arena_name
```
*(Note: If you ever want to edit the arena properties again, remember to disable it first via ```/paintball disable <arena_name>```)*

## ️**🛡️ Commands & Permissions:**
### **👑 Admin Commands:**
```/paintball setmainlobby``` | ```paintball.admin```

```/paintball create <arena>``` | ```paintball.admin```

```/paintball delete <arena>``` | ```paintball.admin```

```/paintball enable <arena>``` | ```paintball.admin```

```/paintball disable <arena>``` | ```paintball.admin```

```/paintball edit <arena>``` | ```paintball.admin```

```/paintball reload``` | ```paintball.admin```

```/paintball givecoins <player> <amount>``` | ```paintball.admin```

```/paintball createtophologram <name> <kills/wins> <global/monthly/weekly>``` | ```paintball.admin```

```/paintball removetophologram <name>``` | ```paintball.admin```

```customhitcommand.update``` (for in-game update notifications)

### **👤 Player Commands:**
```/paintball join <arena>``` | ```currently no permission```

```/paintball joinrandom``` | ```currently no permission```

```/paintball leave``` | ```currently no permission```

```/paintball shop``` | ```currently no permission```

## **🧩 Placeholders:**
You can use these placeholders in any other plugin with PlaceholderAPI support.

```%paintball_wins%```

```%paintball_loses%```

```%paintball_ties%```

```%paintball_kills%```

```%paintball_coins%```

```%paintball_arenaplayers_count_<arena>%```

```%paintball_arena_status_<arena>%```

## **💻 API:**
For this to work you need to add my plugin to your plugin external jars dependencies and to set softdepends: BetterPaintballSystem on your plugin.yml file.

<details>
<summary>Code (Java):</summary>
```
//Returns some player data
int wins = PaintballAPI.getWins(String player);
int loses = PaintballAPI.getLoses(String player);
int ties = PaintballAPI.getTies(String player);
int kills = PaintballAPI.getKills(String player);
```

```
//Paintball Coins methods (not KillCoins)
int coins = PaintballAPI.getCoins(Player player);
void addCoins(Player player, int coins);
void removeCoins(Player player, int coins);
```


```
//Hat methods
boolean hasHat = PaintballAPI.hasHat(Player player,String hat);
boolean hasHatSelected = PaintballAPI.hasHatSelected(Player player,String hat)
ArrayList<Hat> hats = PaintballAPI.getHats(Player player)
```

```
//Perks methods
int perkLevel = PaintballAPI.getPerkLevel(Player player,String perk)
ArrayList<Perk> perks = PaintballAPI.getPerks(Player player)
```
</details>


## **📆 Planned features:**
- optimise Msyl; Comando; PaintballBattle; InventarioAdmin; InventarioShop; PartidaManager
- refactor everything to english
- consistent prefix
- fix deprecations / modernize codes
- add comments to config files
- make paper plugin
- optmise version support
- drop old versions and unused stuff
- add bstats
- more permissions
- config updater
- folia support
- velocity support / multiple servers
- More KillStreaks (Suggest your ideas!)
- More Hats & Abilities (Suggest your ideas!)

## **❓ Q&A:**
**I can't create an arena! What am I doing wrong?**

Before creating your very first arena, you must define the global minigame main lobby using the ```/paintball setmainlobby``` command.

**I hit someone with a snowball, but they don't lose health and neither do I. How do I fix this?**

Ensure that PvP is explicitly enabled in the world settings where your Paintball arena is built.

## 🎖️ **Credits:**
This plugin is an official continuation of the original **[Paintball Battle](https://www.spigotmc.org/resources/paintball-battle-team-minigame-1-8-1-21-1.76676/)** plugin by **Ajneb97**. Since the original project is no longer maintained, Ajneb97 has officially granted me permission to take over, modernize, and continue publishing the project! Thank you, Ajneb97, for the amazing work!

## **🆘 Need Help?**
If you encounter any problems, bugs, or have questions about the plugin, please don't hesitate to contact me directly. I'll be happy to take a look and provide support! Your positive experience is our priority.
