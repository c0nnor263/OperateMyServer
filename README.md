[![](https://cf.way2muchnoise.eu/full_1341025_downloads.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/operate-my-server)
[![](https://cf.way2muchnoise.eu/versions/1341025.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/operate-my-server)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/J3J61TEZDO)

[![OMS Banner](./assets/banner.png)](https://conboi.gitbook.io/oms-wiki)

# Operate My Server

**Operate My Server (OMS)** is a server-side utility mod for Forge that handles automated restarts and server lifecycle control.

Use OMS if you run a modded Minecraft server and want scheduled restarts, player warnings, safer shutdown handling, and expandable server automation without installing a bunch of unrelated utility mods.

Lightweight, modular, and built to be extended through addons.

> **Server-side only**
>
> Players do not need to install OMS on their clients.

---

## What is OMS?

OMS is not just a restart mod.

It is a small server automation platform where behavior is built from independent features and addons. Features can be enabled, disabled, configured, or extended without affecting the rest of the system.

The core mod provides restart and lifecycle handling. Addons can build on top of it to add monitoring, server condition checks, commands, configs, and custom automation logic.

---

## Features

OMS core provides:

* Scheduled server restarts with player warnings
* Centralized restart & shutdown handling
* Modular feature system
* Runtime feature control
* Server-side configuration
* Addon support for extra server automation and monitoring features

Condition-based triggers such as low TPS detection and empty server detection are provided through addons like [**Watchdog Essentials**](https://conboi.gitbook.io/oms-wiki/addons/watchdog-essentials).

---

## Requirements

* Minecraft 1.20.1
* Forge 47.4.0+
* KotlinForForge 4.11.0

> Support for newer Minecraft and Forge versions is planned.

---

## Installation

Download:

1. **OMS**
   * [CurseForge](https://www.curseforge.com/minecraft/mc-mods/operate-my-server)
   * [Modrinth](https://modrinth.com/mod/operate-my-server)
   * [OMS Maven](https://c0nnor263.github.io/OperateMyServer/maven/)

2. **Kotlin For Forge 4.11.0**
   * [CurseForge](https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge)
   * [Modrinth](https://modrinth.com/mod/kotlin-for-forge)

Required files:

* `oms-x.y.z.jar`
* `kotlinforforge-4.11.0.jar`

Place both `.jar` files into your `mods/` folder.

Start the server normally - OMS will initialize automatically.

---

## Basic usage

Restart the server manually:

```text
/oms restart
```

View available OMS features:

```text
/oms feature
```

Enable or disable a feature:

```text
/oms feature <feature> enable
/oms feature <feature> disable
```

Use `/oms feature` to control features without removing the mod from your server.

---

## Config

Main OMS config:

```text
world/serverconfig/oms-server.toml
```

Each addon may create its own config file.

---

## Addons

OMS supports addons - separate mods that extend the core system with new features.

Each addon can provide features, commands, configuration, server condition checks, monitoring logic, and automation behavior.

Install addons like any other mod: drop the `.jar` into `mods/`.

### Existing addons
*   **Watchdog Essentials** - server monitoring and automatic restart triggers.
    * [CurseForge](https://www.curseforge.com/minecraft/mc-mods/watchdog-essentials)
    * [Modrinth](https://modrinth.com/mod/watchdog-essentials)
    * [OMS Maven](https://c0nnor263.github.io/OperateMyServer/maven/)
    * [Wiki](https://conboi.gitbook.io/oms-wiki/addons/watchdog-essentials)
        
> More addons coming soon…

---

## Documentation
Full documentation and guides are available on the [OMS Wiki](https://conboi.gitbook.io/oms-wiki)

Want to build your own addon? See the [Development Guide](https://conboi.gitbook.io/oms-wiki/developer-guide) on the same wiki
