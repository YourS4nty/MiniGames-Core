# MinigamesCore (v1.0.0)

A lightweight, multilingual **multi-minigame core** for Spigot/Paper **1.20+**.  
This repository includes ready-to-play arenas for several classics (TNT Run, TNT Tag, Spleef, OITC, Paintball, Sumo, Build Battle) plus a generic arena system, join GUI, SQLite storage, and a simple localization layer.

> Last updated: 2025-08-15 (First Version *BASIC*)

---

## ✨ Features

- **Arenas system (SQLite)** — create, edit, persist arenas (`ArenaStorage.db`) with spawn, lobby and playzone (cuboid) for each arena.
- **Join GUI** — `/mg join` opens a dynamic GUI showing all arenas with icon and display name.
- **Leave item** — players receive a barrier item in slot 8 to leave an arena quickly.
- **Multilingual** — `messages.yml` ships strings in: es, en, pt, fr; switch with `language` in `config.yml`.
- **Minigames included**
  - **TNT Run** — falling floor, breakable blocks list, **power-ups** (Speed, Jump, Second Chance) with randomized spawning.
  - **TNT Tag** — tag players by hitting; spectators are kept in the playzone during rounds.
  - **Spleef** — break blocks (and via snowballs); fall detection via playzone checks.
  - **One in the Chamber (OITC)** — one-hit arrow kills, controlled damage processing.
  - **Paintball** — snowball gun with cooldown, jump feather with limited uses.
  - **Sumo** — knockback-focused PvP with controlled damage and ring-out detection.
  - **Build Battle** — build phase, voting phase, theme handling, configurable times.
- **BossBars & Titles** — state feedback like waiting/running/winner (per game messages).

---

## 📦 Installation (Users)

1. **Requirements**
   - Minecraft server **Spigot/Paper 1.20+** (Java 17+ recommended).
   - No external dependencies.
2. **Install**
   - Drop the plugin JAR into `/plugins` and start the server once to generate files.
3. **Configure**
   - Edit `/plugins/MinigamesCore/config.yml` (see section below).
   - Optional: set `language: en | es | pt | fr`.
4. **Create arenas**
   - Use the wand (a **Blaze Rod**) to set **pos1** (left click) and **pos2** (right click) of your playzone.
   - Commands:
     - `/mg create <name> <type> <icon> <display name...>`
     - `/mg setspawn <name>`
     - `/mg setlobby <name>`
     - `/mg setplayzone <name>` (uses your pos1/pos2)
     - `/mg list`
     - `/mg delete <name>`
   - Types available: create, delete, list, setspawn, setlobby, setplayzone, join, reload.
5. **Play**
   - `/mg join` → opens the **Join GUI**; click an arena to enter.
   - Receive a **Barrier** in slot 8 to leave the arena.
6. **Reload**
   - `/mg reload` → reloads configuration and arenas from disk.
   - `/mg reload players` → **force-kicks** and resets all running arenas safely.

> **Note on permissions**: The current code does **not** implement permission checks for `/mg` subcommands; server operators should gate command access via OP or a command wrapper. See “Permissions” below for suggestions.

---

## ⚙️ Configuration (`config.yml`)

### Global
- `language`: `en | es | pt | fr` — changes the active language for all messages.

### `tntrun`
- `breakable-blocks`: list of materials to break under players (e.g., `RED_WOOL`, `BLUE_WOOL`).
- `powerup-chances`: integer weights that **sum to 100** for `SPEED`, `JUMP`, `SECOND_CHANCE`.
- `powerup-spawn-interval`: spawn **period in ticks** (20 ticks = 1 s).
- `powerup-spawn-chance`: percent chance to spawn a power-up each interval.

### `buildbattle`
- `build-time`: seconds of building.
- `vote-time`: seconds for each voting period.
- `plot-size`: size of each player’s plot.
- `themes`: default list of possible themes.

### Other sections
- `colorfloor`, `juggernaut`: reserved scaffolding for future minigames (no code in this tree yet).

---

## 🗣 Localization (`messages.yml`)

- Bundled languages: es, en, pt, fr.
- The **LanguageManager** loads the `messages.yml` section matching `config.yml: language` and exposes:
  - `get(key)` → `String`
  - `get(key, placeholders)` → replaces `{placeholder}` tokens
  - `list(key, placeholders)` → returns `List<String>` (used for multi-line help, etc.)

Important keys used by the code include (non-exhaustive): `alreadyin`, `leaveitemname`, `pos1`, `pos2`, `mgreloaded`, `forcedr`, `force-reload`, `mgcreate*`, `mgspawn*`, `mglobby*`, and per-game sets like `tntrun.*`, `tnttag.*`, `spleef.*`, `oitc.*`, `sumo.*`, `buildbattle.*`.

---

## ⛏ Commands (Users/Admins)

Primary command: `/mg`

Subcommands implemented in code:
```
create, setspawn, setlobby, setplayzone, delete, list, join, reload
```

Typical flow:
- **Create** `/mg create arena1 TNT_RUN RED_WOOL "Fast Floor"`
- **Define** `/mg setspawn arena1`, `/mg setlobby arena1`  
  Select pos1/pos2 with the wand (Wooden Axe), then `/mg setplayzone arena1`.
- **List/Delete** `/mg list`, `/mg delete arena1`
- **Join** `/mg join` (opens the GUI)
- **Reload** `/mg reload` (or `/mg reload players` to clear arenas)

---

## 🧠 How the Arena System Works (Developers)

- **Storage**: SQLite file `MinigamesCore/ArenaStorage.db` with table:
  ```sql
  CREATE TABLE IF NOT EXISTS arenas (
    name TEXT PRIMARY KEY,
    type TEXT,
    spawn TEXT,
    lobby TEXT,
    playzone1 TEXT,
    playzone2 TEXT,
    icon TEXT NOT NULL DEFAULT 'GRASS_BLOCK',
    display_name TEXT NOT NULL DEFAULT '§aArena'
  );
  ```
  Locations are serialized as `world,x,y,z` (no yaw/pitch).

- **Core managers**:
  - `ArenaManager` — CRUD + load/save of `Arena` records. Routes join/leave to the right minigame manager and gives/removes the **leave item**.
  - `PlayerArenaManager` — tracks which arena a player is in and applies a brief cooldown to avoid spam.
  - `SelectionManager` — stores per-player pos1/pos2 for playzone selection.
  - `InventoryManager` — hands out the barrier **leave item** (slot 8).
  - `LanguageManager` — loads and resolves localized strings.

- **Per-minigame** (pattern):
  - `<Game>Game` holds a `<GameManager>`, registers listeners.
  - `<Game>Manager` keeps `Map<String, <Game>Arena>` and exposes `getArenaByPlayer`.
  - `<Game>Arena` tracks state, players, timers, and rules.
  - `<Game>Listener` enforces gameplay (hits, movement, blocks).
  - `<Game>Task` runs periodic logic when needed (e.g., TNTRun power-ups).
  - `<Game>State` enum describes phases (WAITING/COUNTDOWN/RUNNING/ENDING).

- **Wiring**:
  - `MinigamesCore#onEnable` creates all managers/games, registers listeners (`JoinGUIListener`, `WandListener`, `PlayerConnectionListener`, and each minigame's listener), sets the `/mg` executor and tab completer, and loads arenas/messages/config.
  - `ArenaManager#notifyGameManagers` pushes a newly created/loaded arena to the correct `<Game>Manager` based on `type` (e.g., `TNT_RUN`, `SPLEEF`, etc.).
  - `JoinGUI` reads all arenas and renders a center-balanced layout of clickable icons (using PDC key `arena_name`).

---

## ➕ Adding a New Minigame (Developers)

1. Create package `dev.yours4nty.minigames.<yourgame>` and implement:
   - `YourGameGame`, `YourGameManager`, `YourGameArena`, `YourGameListener`, `YourGameTask` (if needed), `YourGameState`.
2. In **`MinigamesCore`**:
   - Instantiate `YourGameGame` and expose a getter.
   - Register your listener(s) in `onEnable`.
3. In **`ArenaManager#notifyGameManagers`**:
   - Add a `case` for your game **type** to call `yourGame.getManager().registerArena(arena)`.
4. In **`MinigamesTabCompleter`**:
   - Add your type (e.g., `"YOUR_GAME"`) to the static types list.
5. In **`messages.yml`** and **`config.yml`**:
   - Add the necessary keys and settings (document them in this README).
6. (Optional) In **`JoinGUI`**:
   - If you need custom display logic per game, extend the item lore/icon mapping.

---

## 🔐 Permissions

Currently, the code does **not** check permissions for `/mg` subcommands or GUI interactions.  
**Recommendation**: introduce granular permissions, e.g.:

- `minigames.admin.*` (all admin actions)
- `minigames.admin.create`, `minigames.admin.delete`, `minigames.admin.setspawn`, `minigames.admin.setlobby`, `minigames.admin.setplayzone`, `minigames.admin.reload`
- `minigames.user.join` (GUI and join)
- `minigames.user.leave` (leave item)

Then gate each subcommand/feature via `Player#hasPermission(...)` and define them in `plugin.yml` under `permissions:`.

---

## 🧪 Known Issues / Review Notes (from a file-by-file scan)

- **No permissions** on admin subcommands (any player could create/delete arenas unless the command is op-only at the server level).
- **`/mg join <name>`**: The command opens the GUI; there is no direct join-by-name branch — consider adding `/mg join <arena>` for quick joins (help text should match behavior).
- **Location serialization** stores only `world,x,y,z` (no yaw/pitch) — good enough for teleports, but players will keep their existing yaw/pitch.
- **Build tooling**: No `pom.xml`/`build.gradle` present in this tree. Consider adding a build script (Maven or Gradle) to produce the JAR.
- **ColorFloor/Juggernaut** sections exist in `config.yml` but no Java packages are present — remove or mark as “planned” to avoid confusion.
- **Consistency**: Ensure all referenced message keys exist across all languages (e.g., `tntrun.pw.*`, `buildbattle.*`, etc.).
- **Damage handling**: Sumo/Paintball/OITC listeners cancel/override damage in specific ways; if you run other PvP-altering plugins, test for compatibility (consider event priority).

---

## 🗂 Project Structure (select files)

```
main/
  java/dev/yours4nty/
    MinigamesCore.java                # Main plugin class
    commands/
      MinigamesCommand.java           # /mg subcommands
      MinigamesTabCompleter.java      # tab completions + game types
    gui/
      JoinGUI.java                    # Arenas GUI
    listeners/
      JoinGUIListener.java            # GUI click/block interactions
      PlayerConnectionListener.java   # join/quit and damage guard in arenas
      WandListener.java               # Wooden Axe selection
    managers/
      Arena.java                      # arena POJO (name/type/spawn/lobby/playzone/icon/displayName)
      ArenaManager.java               # storage, join/leave routing, notify managers
      InventoryManager.java           # leave item (Barrier, slot 8)
      LanguageManager.java            # messages loader & placeholder support
      PlayerArenaManager.java         # tracks players in arenas + cooldown
      SelectionManager.java           # stores pos1/pos2 per player
    storage/
      SQLite.java                     # sqlite connection + schema
    minigames/
      tntrun/ ...                     # power-ups, breakable floor, state/task/listener
      tnttag/ ...                     # tag mechanic
      spleef/ ...                     # block breaking + snowball interactions
      oitc/ ...                       # one-hit arrow kill
      paintball/ ...                  # snowball gun, jump feather
      sumo/ ...                       # knockback, ring-out
      buildbattle/ ...                # build & voting phases, theme
  resources/
    plugin.yml                        # name/version/main/api-version/command
    config.yml                        # language + per-game settings
    messages.yml                      # multi-language strings
```

---

## 🏗 Build from Source

This project uses **Maven** for building.

**Requirements**:

* Java 17 or newer
* Maven 3.6+

**Steps**:

```bash
# 1. Go to the project folder
cd /path/to/project

# 2. Compile and package the plugin
mvn clean package

# 3. The compiled JAR will be in:
target/<plugin-name>-<version>.jar
```

---

## 🧩 API & Events (quick reference)

- **Join an arena (programmatic)**:
  ```java
  Arena arena = plugin.getArenaManager().getArena("arena1");
  plugin.getArenaManager().joinArena(player, arena);
  ```
- **Leave an arena**:
  ```java
  plugin.getArenaManager().leaveArena(player);
  ```
- **Listen for game events**: Hook into existing listeners or add your own under a new package; follow the per-minigame pattern shown in this codebase.

---

## 📄 License

_Under MIT License_

---

## 🙋 FAQ

- **Q:** “Players keep the same facing direction when teleported.”  
  **A:** Correct — yaw/pitch aren’t stored in the DB serialization. If needed, extend `ArenaManager` to save yaw/pitch.

- **Q:** “How do I add permissions?”  
  **A:** See the “Permissions” section; add checks in `MinigamesCommand` and define nodes in `plugin.yml`.

- **Q:** “I don’t see `ColorFloor/Juggernaut` in-game.”  
  **A:** Those sections are placeholders in `config.yml`; no implementation is present in this code snapshot.

---
