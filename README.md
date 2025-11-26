# Bounty Plugin

A Minecraft plugin for version 1.21.10 that allows players to set bounties on other players using custom items.

## Features

- **Set bounties with custom items** - Players can choose any items from their inventory to use as bounty rewards
- **GUI-based item selection** - Easy-to-use inventory GUI for selecting bounty items
- **Bounties GUI** - View all active bounties with items in an interactive GUI (`/bounties`)
- **Bounty detail view** - Click on any bounty in the GUI to see all reward items
- **Leaderboard system** - Track and display top bounty claimers with rankings
- **Automatic item removal** - Items are removed from your inventory when you set a bounty
- **Non-reversible bounties** - Once set, bounties cannot be cancelled (admin can remove)
- **Smart reward distribution** - If killer's inventory is full, items are given automatically when space becomes available
- **Multiple bounties per player** - Multiple players can set bounties on the same target
- **Broadcast notifications** - Server-wide announcements when bounties are claimed

## Commands

### Player Commands
- `/setbounty <player>` - Open GUI to set a bounty on a player
- `/bounties` or `/bounty` - Open GUI to view all active bounties with items
- `/bounty list` - List all players with active bounties (text)
- `/bounty view <player>` - View details of bounties on a specific player (text)
- `/leaderboard` or `/bounty leaderboard` - View leaderboard of top bounty claimers

### Admin Commands
- `/bounty remove <player> <index>` - Remove a specific bounty (by index number)

## How It Works

1. **Setting a Bounty:**
   - Use `/setbounty <player>` to open the bounty GUI
   - Place items you want to use as reward in the GUI
   - Click the green wool button to confirm
   - Items are removed from your inventory immediately
   - Bounty is set and cannot be reversed

2. **Claiming a Bounty:**
   - When a player with bounties is killed by another player
   - The killer automatically receives all reward items from all bounties
   - If inventory is full, items are queued and given when space becomes available
   - A message is broadcast to the entire server

3. **Item Handling:**
   - Items are checked to ensure you have them in your inventory before setting
   - Items are removed immediately when bounty is confirmed
   - Reward items are given to the killer when they claim the bounty
   - Full inventory handling: items wait until space is available

## Permissions

- `bounty.use` - Use bounty commands (default: true)
- `bounty.set` - Set bounties on players (default: true)
- `bounty.view` - View bounties (default: true)
- `bounty.remove` - Remove bounties (default: op)
- `bounty.*` - All permissions (default: true)

## Configuration

The plugin creates a `config.yml` file with the following options:

- `max-bounties-per-player`: Maximum number of bounties a player can have (default: 10)
- `min-bounty-items`: Minimum items required to set a bounty (default: 1)
- `max-bounty-items`: Maximum items allowed in a single bounty (default: 36)
- `broadcast-bounty-claim`: Whether to broadcast when bounties are claimed (default: true)
- `bounty-claim-message`: Message format for bounty claim broadcast

## Building

1. Make sure you have Maven installed
2. Run `mvn clean package` in the project directory
3. The compiled JAR will be in the `target` folder

## Installation

1. Download or build the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. The plugin will create a `bounties.yml` file in the plugin's data folder to store bounty data

## Technical Details

- **Solid Logic**: All item operations are validated before execution
- **Inventory Safety**: Items are only removed after confirmation
- **Full Inventory Handling**: Automatic distribution when space becomes available
- **No Item Loss**: Items are queued if inventory is full, never lost
- **Persistent Storage**: Bounties are saved to file and persist across restarts

## Notes

- Bounties are only claimable when killed by another player (not mobs/environment)
- Multiple bounties on the same player stack - all rewards are given at once
- Items in bounties are stored and given exactly as placed in the GUI
- The plugin checks inventory space every second for pending rewards

