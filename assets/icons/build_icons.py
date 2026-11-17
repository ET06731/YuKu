from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
out = Path(__file__).parent
watch = root / 'examples/yukuai-sync-dev/src/common/yukuai-icon.png'
source = Image.open(watch).convert('RGBA')

# Keep one source artwork for the watch builds, Android launcher, and store art.
watch.with_name('logo.png').write_bytes(watch.read_bytes())
source.resize((512, 512), Image.Resampling.LANCZOS).save(out / 'store-512.png')
res = root / 'apps/yukuai-android/app/src/main/res'
for density, scale in [('mdpi', 1), ('hdpi', 1.5), ('xhdpi', 2), ('xxhdpi', 3), ('xxxhdpi', 4)]:
    folder = res / ('mipmap-' + density)
    folder.mkdir(parents=True, exist_ok=True)
    source.resize((round(48 * scale), round(48 * scale)), Image.Resampling.LANCZOS).save(folder / 'ic_launcher.png')
    size = round(108 * scale)
    layer = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    art_size = round(72 * scale)
    art = source.resize((art_size, art_size), Image.Resampling.LANCZOS)
    offset = (size - art_size) // 2
    layer.alpha_composite(art, (offset, offset))
    layer.save(folder / 'ic_launcher_foreground.png')

adaptive = res / 'mipmap-anydpi-v26'
adaptive.mkdir(parents=True, exist_ok=True)
(adaptive / 'ic_launcher.xml').write_text('''<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/icon_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
''', encoding='utf-8')
(res / 'values').mkdir(parents=True, exist_ok=True)
(res / 'values/icon_colors.xml').write_text('<resources><color name="icon_background">#477B60</color></resources>\n', encoding='utf-8')
preview = Image.new('RGBA', (192, 192), 'black')
preview.alpha_composite(source.resize((96, 96), Image.Resampling.LANCZOS), (48, 48))
preview.convert('RGB').save(out / 'watch-preview.png')
print('Updated watch copy, Android launcher densities, adaptive icon, and store artwork from the shared 96x96 icon.')


