# 進捗・引き継ぎログ

このファイルはAIエージェント間で開発コンテキストを引き継ぐためのログ。
タスクの区切りで必ず最新の状態をここに記録すること。

---

## 現在のステータス
- **プロジェクト**: Albii
- **現在地**: 滑らかさ改善は P0 で解決。release 相当ビルド（`releaseMeasure`）の実機体感は「めっちゃいい感じ」とユーザー確認済み。main にマージ済み（PR #8）
- **直近の変更**: `releaseMeasure` buildType（R8 minify・デバッグ署名）と `install-release-measure-device.bat` を追加。主因は debug ビルドで評価していたこと（下記）
- **残り**: Coil 化・Telephoto 化・ピンチ連続スケール等のリファクタ（P1〜P6）は、体感が良好なため必須ではなくなった。必要になったら再開（特にビューアの画像デコード待ち・隣ページ先読みなし）。次フェーズは C（Media3 動画再生）候補。今後の体感確認は `install-release-measure-device.bat` のビルドで行う
- **Macrobenchmark**: 作りかけは `git stash`（"macrobenchmark WIP (paused)"）に退避

## 要件・計画
- 計画書: [android-album-app-plan.md](./android-album-app-plan.md)
- ADR: [ADR-0001](../docs/adr/ADR-0001-android-tech-stack.md)（Kotlin + Compose）、[ADR-0002](../docs/adr/ADR-0002-thumbnail-listing-and-di.md)（Coil / Paging 3 撤回）、[ADR-0003](../docs/adr/ADR-0003-navigation-settings-and-albums.md)（ナビ・設定・アルバム）、[ADR-0004](../docs/adr/ADR-0004-gesture-handling-and-in-app-picker.md)（ジェスチャー・アプリ内ピッカー）、[ADR-0005](../docs/adr/ADR-0005-exif-info-panel.md)（EXIF 情報パネル）

## 完了済みの内容
- Phase 0: プロジェクト雛形、ビルド環境（JDK 17 / Gradle 8.11.1 / SDK zip 版）
- Phase 1: 権限、MediaStore 読み込み、グリッド、フォルダ表示、ハンバーガーメニュー → 設定、下部タブ（フォト / アルバム / 端末）
- 実機フィードバック1・2 の反映: ピンチで列数変更、全画面の横スワイプ、ピンチ拡大時のパン制限、スクロールバーのオーバーレイと自動非表示、アプリ内ピッカーでのアルバム追加

## 写真情報パネル（完了・マージ済み）
- 上スワイプ（等倍時）または情報ボタンで下からシートを表示。実機で表示を確認済み
- 設定の「写真情報」で項目ごとに ON/OFF（既定 ON: 基本項目。OFF: 機材・GPS・向き・色空間）
- 未確認: 設定切り替えの反映、GPS・機材項目の表示
- 実装: `info/InfoItem.kt`、`info/InfoFormat.kt`（単体テストあり）、`info/PhotoInfoRepository.kt`、`ui/viewer/PhotoInfoSheet.kt`

## 次のフェーズ計画（優先度順）
1. **A: 120fps・スクロール / ピンチの滑らかさ**（最優先・実機フィードバックの未解決事項）
   - Macrobenchmark / JankStats で計測し、フレーム落ちの原因を特定してから直す
   - 完了条件: スクロールとピンチで計測上のジャンクが減り、実機で滑らかさを確認できる
   - 実機確認: 等倍での横移動は OK（2 回目のスワイプで切り替わり）。拡大中の縁からの横移動は未確認
2. **横スワイプ（拡大中のパン後のページ送り）**: 34cc99c で実装済み。実機で指による確認待ち
3. **C: Media3 による動画再生**（ADR-0006 予定）
4. **D: 計算系（被写界深度・超焦点距離・手ブレ限界）、天体系（日の出・ゴールデンアワー）、連写のまとめ**
5. **E: ピント位置（AF）の可視化**（先にスパイクで対応可否を確認）
6. **F: フィルタ、お気に入り、ゴミ箱**
7. **G: 見た目の仕上げ（Apple / Google Photos 風）、タブレット対応**

## 発生している課題・特記事項
- Coil 3 は KMP 版 Compose を引き込みビルドが壊れたため撤回（ADR-0002）
- `loadThumbnail` で動画・HEIC が取れるかは未確認
- 拡大中の横スワイプは実装済み（実機で拡大中の縁を確認待ち）。
- 実機スクロール計測（Pixel 10a・120Hz、adb で上下スクロール 12 往復、dumpsys gfxinfo。adb のスワイプは指と違うため参考値）:

  | ビルド | フレーム | ジャンク | p50 | p90 | p95 | p99 |
  |--------|---------|---------|-----|-----|-----|-----|
  | debug | 1523 / 1610 | 6.8% / 7.2% | 9ms | 19〜20ms | 26〜28ms | 48ms |
  | releaseMeasure（R8 minify） | 1982 | 0.1% | 5ms | 9ms | 10ms | 12ms |

  → debug は Compose が極端に遅く、体感評価に使えない。滑らかさの判断は `install-release-measure-device.bat` のビルドで行う
- 滑らかさ・ジェスチャー改善の分析と計画（Coil 2.7.x + loadThumbnail 用 Fetcher、Telephoto、ピンチ連続スケール、Baseline Profile、BOM 更新）は P0 の結果を見て範囲を確定する。主な仮説: サムネが自前読込でキャンセル・プレースホルダ無し、全セルの `animateItemPlacement()`、ビューアが画像デコード完了まで空で隣ページ先読み無し、ズーム/パンが自前実装
- 実機の画面ロック中は gfxinfo が 0 フレームになる。計測前にロック解除が必要（`adb shell svc power stayon usb` で点灯維持）
- `AlbumDatabase.create()` は VM 1つ前提。複数インスタンスは未対応

## AIへの伝言
「滑らかさは release 相当ビルドで解決したで（体感OK確認済み）。以後の体感確認は install-release-measure-device.bat で行ってな。次は C（Media3 動画再生、ADR-0006）に進めるで。ビューアの黒→遅れて表示が気になったら P4 を再開な。」
