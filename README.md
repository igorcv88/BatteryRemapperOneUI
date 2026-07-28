BatteryRemapper OneUI — Safe Fork

«[!IMPORTANT]
This is an independent safety-focused fork of "Dhangofa/BatteryRemapper" (https://github.com/Dhangofa/BatteryRemapper).

Unlike the upstream module, this fork does not shut down the device at 20% physical battery and does not control Battery Saver.

Its only function is to visually remap the battery percentage shown by Android SystemUI.»

BatteryRemapper OneUI is a lightweight, headless Xposed/LSPosed module intended for Samsung One UI devices using the native 80% Battery Protection limit.

When the physical battery reaches 80%, SystemUI displays 100%. A small visual reserve is retained below 20% physical battery, so the displayed percentage only reaches 0% when the physical battery also reaches 0%.

---

Differences from the upstream module

Behavior| Upstream BatteryRemapper| BatteryRemapper OneUI
Remaps the SystemUI battery percentage| Yes| Yes
Physical 80% is displayed as 100%| Yes| Yes
Physical 20% is displayed as 0%| Yes| No — displayed as 10%
Retains visibility below 20% physical| No| Yes
Forces shutdown at 20% physical| Yes| Removed
Shows a 30-second shutdown countdown| Yes| Removed
Automatically enables Battery Saver| Yes| Removed
Automatically disables Battery Saver| Yes| Removed
Interferes with Samsung Adaptive Power Saving| Possible through forced Battery Saver state| No Battery Saver control
Reads charger state| Yes| Removed
Calls "PowerManager" or "BatterySaverUtils"| Yes| Removed
Runs only inside SystemUI| Yes| Yes
Background service or polling loop| No| No

This fork was created for devices that must not be intentionally shut down, rebooted, or have their Battery Saver state overridden by the module.

---

Features

- Visual SystemUI remapping: Physical battery levels from 20% to 80% are displayed as 10% to 100%.
- Visible low-battery reserve: Physical battery levels from 0% to 20% are displayed as 0% to 10%.
- No forced shutdown: The module never requests a shutdown or reboot.
- No Battery Saver automation: Battery Saver remains controlled by Android and Samsung services.
- Samsung Adaptive Power Saving remains available: This module does not enable, disable, or override it.
- No charging control: Samsung Battery Protection remains responsible for stopping charging at the configured physical limit.
- SystemUI-only scope: The hook is loaded only inside "com.android.systemui".
- No permanent background process: The module does not use a service, polling loop, root shell, alarm, scheduled task, or wakelock.

---

LSPosed configuration

Enable the module only for:

- [x] System UI ("com.android.systemui")

Do not add any of the following to the module scope:

- Android System Framework
- Settings
- Device Care
- System Server
- Samsung battery services
- Third-party applications

«[!NOTE]
The module modifies only battery-level reads performed inside SystemUI. Android system services, Samsung power-management components, the battery HAL, the kernel battery interface, and the charging controller continue receiving the physical battery state.»

---

Battery mapping

The fork uses two linear ranges:

- Physical 0%–20% becomes displayed 0%–10%.
- Physical 20%–80% becomes displayed 10%–100%.
- Physical levels of 80% or more are displayed as 100%.

Physical battery| Displayed battery
100%| 100%
80%| 100%
75%| 93%
70%| 85%
65%| 78%
60%| 70%
50%| 55%
40%| 40%
30%| 25%
25%| 18%
20%| 10%
15%| 8%
10%| 5%
5%| 3%
0%| 0%

Because the physical 60-point range between 20% and 80% is expanded into a displayed 90-point range, some displayed numbers may be skipped. For example, the percentage may move directly from 78% to 76%. This is expected.

---

Samsung Battery Protection

This module does not limit charging by itself.

Configure the physical charging limit through Samsung One UI:

"Settings → Battery → Battery protection"

With Battery Protection configured to stop at 80%:

1. The physical battery reaches 80%.
2. Samsung Battery Protection stops or pauses charging.
3. SystemUI reads the physical 80% level.
4. BatteryRemapper OneUI displays it as 100%.

The displayed 100% is a SystemUI value. It does not replace the physical battery level used by Samsung's charging controller.

---

Samsung Adaptive Power Saving

BatteryRemapper OneUI does not control Power Saving Mode.

The following upstream components were removed from this fork:

- Automatic Battery Saver activation
- Automatic Battery Saver deactivation
- Battery Saver hysteresis
- Charger-state bypass
- "PowerManager.setPowerSaveModeEnabled()" calls
- "BatterySaverUtils.setPowerSaveMode()" calls

Samsung Adaptive Power Saving can therefore continue enabling or disabling Power Saving Mode according to the operating system's own rules.

---

Safety changes

The following shutdown-related code was completely removed:

- Physical 20% shutdown trigger
- 30-second countdown
- System-level shutdown dialog
- Charger cancellation logic
- Shutdown intent
- Immediate shutdown fallback after dialog errors

Reaching 20% physical battery now displays 10%. The percentage continues decreasing normally until physical 0% is displayed as 0%.

---

Scope of the hook

The module hooks:

Intent.getIntExtra(String, int)

inside:

com.android.systemui

When the requested key matches:

BatteryManager.EXTRA_LEVEL

the physical result is converted into the displayed percentage.

The module does not write a new battery level to "BatteryService", the battery HAL, kernel nodes, or Samsung charging services.

---

Limitations

- Settings, Device Care, widgets, diagnostic applications, and third-party applications may continue showing the physical percentage.
- Applications reading "/sys/class/power_supply/battery/capacity" receive the physical battery level.
- The percentage shown outside SystemUI may differ from the status bar, lock screen, Quick Settings, or AOD.
- SystemUI implementations vary between One UI and Android versions.
- A future One UI update may change how SystemUI obtains battery information and require a module update.
- This fork does not claim official Samsung support.

---

Requirements

- Android 10 or newer
- Samsung One UI or another compatible Android SystemUI
- LSPosed, Vector, or another compatible Xposed framework
- A functioning Zygisk environment through Magisk, KernelSU, or another supported implementation

---

Installation

1. Build or download the APK.
2. Install the APK.
3. Enable BatteryRemapper OneUI in LSPosed or Vector.
4. Select only System UI as its scope.
5. Restart SystemUI using the method supported by your framework.

A complete hardware reboot is not required by the module itself, although the active Xposed framework determines when newly enabled hooks are loaded.

---

Upstream attribution

BatteryRemapper OneUI is based on:

"Dhangofa/BatteryRemapper" (https://github.com/Dhangofa/BatteryRemapper)

This fork changes the battery mapping and removes all automatic shutdown and Battery Saver behavior.

Review the upstream license before redistributing source code or compiled APKs.