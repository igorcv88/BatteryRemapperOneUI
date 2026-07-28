# BatteryRemapper OneUI

> [!IMPORTANT]
> **BatteryRemapper OneUI is an independent safety-focused fork of [Dhangofa/BatteryRemapper](https://github.com/Dhangofa/BatteryRemapper).**
>
> Unlike the upstream module, this fork:
>
> - Does **not** force the device to shut down at 20% physical battery
> - Does **not** enable or disable Battery Saver
> - Preserves a visible battery reserve below 20%
> - Is intended for Samsung One UI and ephemeral-root environments where root access is lost after a shutdown or hardware reboot

BatteryRemapper OneUI is a lightweight, headless Xposed/LSPosed module that visually remaps the battery percentage shown by Android SystemUI.

It is designed for Samsung devices using the native 80% Battery Protection limit. When the physical battery reaches 80%, SystemUI displays 100%.

The module only changes the percentage shown inside `com.android.systemui`. It does not modify the physical battery level, charging behavior, Samsung Battery Protection, Adaptive Power Saving, or Android system services.

---

## Differences from upstream

| Behavior | Upstream BatteryRemapper | BatteryRemapper OneUI |
|---|---|---|
| Remaps the SystemUI battery percentage | Yes | Yes |
| Displays physical 80% as 100% | Yes | Yes |
| Displays physical 20% as 0% | Yes | No, displays 10% |
| Shows battery levels below 20% | No | Yes |
| Forces shutdown at 20% physical | Yes | Removed |
| Shows a 30-second shutdown countdown | Yes | Removed |
| Automatically enables Battery Saver | Yes | Removed |
| Automatically disables Battery Saver | Yes | Removed |
| Reads charger state | Yes | Removed |
| Calls `PowerManager` | Yes | Removed |
| Calls `BatterySaverUtils` | Yes | Removed |
| Overrides Adaptive Power Saving decisions | Possible | No |
| Runs only inside SystemUI | Yes | Yes |
| Uses a background service or polling loop | No | No |

This fork was created for devices that must not be intentionally shut down or have their Battery Saver state overridden by the module.

---

## Features

- **Visual SystemUI remapping:** Physical battery levels from 20% to 80% are displayed as 10% to 100%.
- **Visible low-battery reserve:** Physical battery levels from 0% to 20% are displayed as 0% to 10%.
- **No forced shutdown:** The module never requests a shutdown or reboot.
- **No Battery Saver automation:** Battery Saver remains controlled by Android and Samsung services.
- **Adaptive Power Saving compatibility:** Samsung remains free to enable or disable Power Saving Mode.
- **Battery Protection compatibility:** Samsung remains responsible for stopping charging at the configured physical limit.
- **SystemUI-only scope:** The hook is loaded only inside `com.android.systemui`.
- **No permanent background process:** The module does not use a service, polling loop, alarm, root shell, scheduled task, or wakelock.

---

## Battery mapping

The module uses two linear ranges:

- Physical 0%–20% becomes displayed 0%–10%.
- Physical 20%–80% becomes displayed 10%–100%.
- Physical levels of 80% or higher are displayed as 100%.

| Physical battery | Displayed battery |
|---:|---:|
| 100% | 100% |
| 80% | 100% |
| 75% | 93% |
| 70% | 85% |
| 65% | 78% |
| 60% | 70% |
| 50% | 55% |
| 40% | 40% |
| 30% | 25% |
| 25% | 18% |
| 20% | 10% |
| 15% | 8% |
| 10% | 5% |
| 5% | 3% |
| 0% | 0% |

Because the physical 20%–80% range is expanded into a larger displayed range, some displayed percentage values may be skipped. This is expected.

---

## Samsung Battery Protection

BatteryRemapper OneUI does not control charging.

Configure the physical charging limit through One UI:

`Settings → Battery → Battery protection`

With the charging limit configured to 80%:

1. The physical battery reaches 80%.
2. Samsung Battery Protection stops or pauses charging.
3. SystemUI reads the physical 80% value.
4. BatteryRemapper OneUI displays it as 100%.

The displayed 100% does not replace the physical battery level used by Samsung's charging controller.

---

## Samsung Adaptive Power Saving

BatteryRemapper OneUI does not control Power Saving Mode.

The following upstream behavior was removed:

- Automatic Battery Saver activation
- Automatic Battery Saver deactivation
- Battery Saver hysteresis
- Charger-state bypass
- `PowerManager.setPowerSaveModeEnabled()` calls
- `BatterySaverUtils.setPowerSaveMode()` calls

Samsung Adaptive Power Saving can continue enabling or disabling Power Saving Mode according to Samsung's own rules and physical battery data.

---

## Ephemeral-root safety

Some devices obtain root access through a post-boot exploit. On these devices, root access may be lost after a hardware reboot or shutdown.

For this reason, BatteryRemapper OneUI completely removes:

- The physical 20% shutdown trigger
- The 30-second shutdown countdown
- The system shutdown dialog
- The charger cancellation logic
- The shutdown intent
- The immediate shutdown fallback used when the dialog fails

At 20% physical battery, the module displays 10%. The displayed percentage continues decreasing until physical 0% is displayed as 0%.

---

## LSPosed configuration

Enable the module only for:

- [x] **System UI** (`com.android.systemui`)

Do not add these packages or components to the module scope:

- Android System Framework
- Settings
- Device Care
- Third-party applications

> [!NOTE]
> Android system services, Samsung power-management components, the battery HAL, kernel battery interfaces, and charging-control services continue receiving the physical battery level.

---

## Technical scope

The module hooks `Intent.getIntExtra(String, int)` inside `com.android.systemui`.

When the requested key matches `BatteryManager.EXTRA_LEVEL`, the physical value returned to SystemUI is converted into the displayed value.

The module does not write a modified battery level to:

- `BatteryService`
- Android Health HAL
- Kernel battery nodes
- Samsung charging services
- Samsung power-management services

---

## Limitations

- Settings, Device Care, widgets, diagnostic tools, and third-party applications may continue showing the physical battery percentage.
- Applications reading `/sys/class/power_supply/battery/capacity` receive the physical battery level.
- Battery percentages shown outside SystemUI may differ from the status bar, lock screen, Quick Settings, or Always On Display.
- Compatibility may vary between One UI and Android versions.
- A future One UI update may change how SystemUI reads battery information.
- This project is not affiliated with or officially supported by Samsung.

---

## Requirements

- Android 10 or newer
- Samsung One UI or another compatible Android SystemUI
- LSPosed, Vector, or another compatible Xposed framework
- A functioning Zygisk environment through KernelSU, Magisk, or another supported implementation

---

## Installation

1. Download or build the APK.
2. Install the APK.
3. Enable **BatteryRemapper OneUI** in LSPosed or Vector.
4. Select only **System UI** as its scope.
5. Restart SystemUI using a method supported by your framework.

The module itself does not require or request a hardware reboot.

---

## Upstream attribution

BatteryRemapper OneUI is based on:

[Dhangofa/BatteryRemapper](https://github.com/Dhangofa/BatteryRemapper)

This fork changes the battery mapping and removes all automatic shutdown and Battery Saver behavior.

Review the upstream license before redistributing modified source code or compiled APKs.