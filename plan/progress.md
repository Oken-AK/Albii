# 進捗・引き継ぎログ

このファイルはAIエージェント間で開発コンテキストを引き継ぐためのログ。
タスクの区切りで必ず最新の状態をここに記録すること。

---

## 現在のステータス
- **プロジェクト**: Albii
- **現在地**: 写真情報パネル（Phase B）完了。PR #2 を main にマージ済み（merge commit 896e89f）
- **作業中**: Phase A（120fps・スクロール / ピンチの滑らかさ）。ブランチ `perf/scroll-pinch-120fps`（main から作成）
- **残り**: 計測 → 改善 → 実機確認 → PR（マージは許可後）

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
2. **横スワイプ（拡大中のパン後のページ送り）**: 拡大中の端での挙動を整える
3. **C: Media3 による動画再生**（ADR-0006 予定）
4. **D: 計算系（被写界深度・超焦点距離・手ブレ限界）、天体系（日の出・ゴールデンアワー）、連写のまとめ**
5. **E: ピント位置（AF）の可視化**（先にスパイクで対応可否を確認）
6. **F: フィルタ、お気に入り、ゴミ箱**
7. **G: 見た目の仕上げ（Apple / Google Photos 風）、タブレット対応**

## 発生している課題・特記事項
- Coil 3 は KMP 版 Compose を引き込みビルドが壊れたため撤回（ADR-0002）
- `loadThumbnail` で動画・HEIC が取れるかは未確認
- 横スワイプ（拡大中のパン後のページ送り）と 120fps は未対応
- `AlbumDatabase.create()` は VM 1つ前提。複数インスタンスは未対応

## AIへの伝言
「写真情報パネルまで入ったで。実機で上スワイプしてみてな。」
