# AGENTS.md - CobblePassive Multi-Loader 1.21.1 Architecture & Standards

This document is the authoritative architectural guide for development in this repository.

## 1. Multi-Loader Philosophy (DRY & Common-First)
This project targets **Minecraft 1.21.1 (Java 21)** and **Cobblemon** across both **NeoForge** and **Fabric** using a single-repository layout with **Official Mojang Mappings (Mojmap)**:
- **`:common` (95% of Code)**: Pure Vanilla Minecraft (Mojmap), Cobblemon API, SpongePowered Mixin, and FastUtil. Zero loader-specific imports (`net.neoforged.*` or `net.fabricmc.*` are strictly forbidden).
- **`:neoforge` (Adapter Layer)**: Implements SPI interfaces, `@Mod` lifecycle, and bridges `NeoForge.EVENT_BUS` to `:common`.
- **`:fabric` (Adapter Layer)**: Implements SPI interfaces, `ModInitializer` / `ClientModInitializer`, and bridges Fabric API events to `:common`.

### Mandatory Loader Synchronization Rule
**NEVER implement a feature in `:neoforge` without implementing its counterpart in `:fabric` (and vice versa).**
Update `:common`, `:neoforge`, and `:fabric` in the exact same turn/commit.

## 2. Software Engineering Paradigms
- **100% English Code Policy**: All code elements, comments, Javadocs, and logs MUST be strictly in English.
- **Dependency Inversion via Java SPI**: Common code accesses platform services via `java.util.ServiceLoader` registered in `META-INF/services/`.
- **Composition via Mixin**: Common mixins live in `:common/src/main/java/com/cobblepassive/mixin/` and are listed in `cobblepassive.mixins.json`.
