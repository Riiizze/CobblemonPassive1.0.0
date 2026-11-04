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

## 3. Build & Mixin Refmap Prevention Rules (CRITICAL - Zero Crash Policy)
To prevent `InvalidInjectionException: ... No refMap loaded` or Gradle build failures in future builds, strictly enforce these rules:

1. **Fabric Mixin Refmap Requirement (Mojmap -> Intermediary)**:
   - `:common` mixins are written against **Official Mojang Mappings (Mojmap)** (e.g., `makeStuckInBlock`, `canStandOnFluid`).
   - At runtime, **NeoForge** runs Mojmap natively (no refmap needed), whereas **Fabric Loader** runs **Intermediary** (`class_1297`, `method_5844`, `class_1309`, `method_26319`).
   - Therefore, the Fabric JAR **MUST** contain both `CobblePassive-refmap.json` AND `"refmap": "CobblePassive-refmap.json"` inside `cobblepassive.mixins.json`.
   - In `fabric/build.gradle`:
     - Always configure `loom { mixin { defaultRefmapName = "CobblePassive-refmap.json" } }`.
     - Wire `:common` into `:fabric` via `sourceSets.main.java.srcDir project(':common').file('src/main/java')` and `sourceSets.main.resources.srcDir project(':common').file('src/main/resources')` so Fabric Loom's Mixin annotation processor recognizes `cobblepassive.mixins.json` and automatically injects `"refmap": "CobblePassive-refmap.json"`.

2. **Subproject Source Wiring in `:neoforge` and `:fabric`**:
   - Never attach `:common` sources using `tasks.withType(JavaCompile).configureEach`, as that leaks `main` sources into `compileTestJava`. Always use `tasks.named('compileJava', JavaCompile)` or `sourceSets.main.java.srcDir`.

3. **Gradle 8.12 Plugin Compatibility**:
   - With Gradle 8.12 (`C:\gradle-8.12\bin\gradle.bat`) and JDK 21 (`C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot`), always use:
     - `fabric-loom` version `1.9-SNAPSHOT` (`1.8.x` crashes on Gradle 8.12 due to removed `Problems.forNamespace` API).
     - `net.neoforged.moddev` version `2.0.78` for `:common` and `:neoforge`.

4. **Cobblemon Entity Method Remapping & Cross-Version Binary Compatibility (1.6.x - 1.8.x)**:
   - Never invoke Vanilla `Mob` / `LivingEntity` / `Entity` methods (such as `getNavigation()`, `teleportTo()`, `setDeltaMovement()`) through a receiver variable typed as `com.cobblemon.mod.common.entity.pokemon.PokemonEntity`.
   - In Cobblemon 1.6.1, `PokemonEntity.getNavigation()` has a covariant return type (`PokemonNavigation`), which prevents Fabric Loom's TinyRemapper from remapping it to Intermediary `method_5942()` and causes a `NoSuchMethodError` / `NoClassDefFoundError` at runtime on Cobblemon 1.8.x.
   - Always store or cast `PokemonEntity` to Vanilla `net.minecraft.world.entity.Mob` before calling Vanilla entity methods, and iterate `PlayerPartyStore` via `for (Pokemon pokemon : party)`.

5. **Mandatory Post-Build Verification Checklist**:
   - Before declaring any build complete, inspect `fabric/build/libs/CobblePassive-*.jar` to verify:
     - `CobblePassive-refmap.json` exists in the root of the JAR and maps all `@Inject` targets.
     - `cobblepassive.mixins.json` inside the Fabric JAR contains `"refmap": "CobblePassive-refmap.json"`.
     - Every class listed in `cobblepassive.mixins.json` exists in `com/cobblepassive/mixin/` inside both the Fabric and NeoForge JARs.
   - Copy both verified JARs (`CobblePassive-1.0.1.jar` and `CobblePassive-NeoForge-1.0.1.jar`) to `output/`.
