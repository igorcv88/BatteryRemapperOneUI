# BatteryRemapper OneUI

BatteryRemapper OneUI is a modern Xposed module for Samsung SystemUI. It changes only the battery percentage displayed by SystemUI while the device and charging controller continue using the physical battery level.

## What it does

- Lets you choose whether physical 80%, 85%, or 90% is displayed as 100%.
- Keeps physical 0% through 5% unchanged, so physical 5% is always displayed as 5%.
- Continuously stretches the range between physical 5% and the selected full level into displayed 5% through 100%.
- Changes the setting live through the modern Xposed remote-preferences API.
- Hooks only `com.android.systemui`.

It does not change charging behavior, Samsung Battery Protection, the battery HAL, kernel battery values, Battery Saver, or shutdown behavior.

## Mapping

For a physical level `p` and selected full level `F`, the displayed percentage is:

```text
p                         when p <= 5
5 + round((p - 5) × 95 / (F - 5))   when 5 < p < F
100                       when p >= F
```

Examples:

| Physical | Full at 80% | Full at 85% | Full at 90% |
|---:|---:|---:|---:|
| 0% | 0% | 0% | 0% |
| 5% | 5% | 5% | 5% |
| 20% | 24% | 23% | 22% |
| 50% | 62% | 58% | 55% |
| 80% | 100% | 94% | 89% |
| 85% | 100% | 100% | 94% |
| 90% | 100% | 100% | 100% |

Rounding can skip some displayed values because SystemUI receives only integer physical percentages.

## Requirements

- Android 10 or newer
- A modern Xposed framework implementing API 101 or newer
- `com.android.systemui` enabled in the module scope

The module targets modern API 102 and uses its module format. It no longer contains the legacy `IXposedHookLoadPackage`, `XposedBridge`, or `assets/xposed_init` entry point.

## Installation and use

1. Install the APK.
2. Enable BatteryRemapper OneUI in the Xposed manager.
3. Confirm that SystemUI is in scope.
4. Restart SystemUI or reboot after first enabling the module.
5. Open BatteryRemapper OneUI and select 80%, 85%, or 90%.

Changing the selected level does not require another reboot while the framework service and SystemUI hook are active.

## Scope and limitations

The module intercepts reads of `BatteryManager.EXTRA_LEVEL` from the `ACTION_BATTERY_CHANGED` intent inside SystemUI. Other applications, Device Care, diagnostic tools, and `/sys/class/power_supply/battery/capacity` may continue showing the physical value.

A One UI update can change how SystemUI obtains its battery level. If Samsung stops using this broadcast path, the hook will need to be adapted.

## Build

```bash
gradle test assembleRelease -PversionCode=1
```

The project uses Android Gradle Plugin 8.2, Java 17 for the build environment, `io.github.libxposed:api:102.0.0`, and `io.github.libxposed:service:102.0.0`.

## License

MIT. See [LICENSE](LICENSE).
