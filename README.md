# Operate My Server

**OperateMyServer (OMS)** is a modular Minecraft Forge mod designed to give server owners fine-grained control over
server behavior, automated actions, and event-based lifecycle management.

Whether you're running a private server for friends or hosting a more complex public instance, OMS empowers you to build
your own logic around restarts, monitoring, scheduled events, and beyond - using a clean addon-based architecture.

### Why Use OMS?

* Automate server restarts with fully configurable schedules and in-game warnings
* React to server conditions like low TPS or empty player list
* Interact with every feature via powerful in-game `/oms` commands
* Modular system: each addon contains isolated features you can enable or disable
* Live config reloads and runtime-safe caching
* Lightweight, server-side only - no client required
* Easy to extend: write your own addons using a clean, documented API

### Who Is It For?

* **Server Owners** – manage restarts, shutdowns, and conditions like TPS drop or idle server, while using your own
  startup scripts
* **Modpack Developers** – bundle smart server logic like scheduling, idle detection, and watchdogs into your pack
* **Mod Developers** – build powerful lifecycle-based features and admin tools using the OMS addon API

### Requirements

* **Minecraft:** 1.20.1
* **Forge:** 47.4.0 or latest stable version for 1.20.1
* **Kotlin for Forge:** 4.11.0
