# 進捗・引き継ぎログ

このファイルはAIエージェント間で開発コンテキストを引き継ぐためのログ。
タスクの区切りで必ず最新の状態をここに記録すること。

---

## 現在のステータス
- **プロジェクト**: Albii
- **現在地**: Phase 1 完了（PR #1 を main にマージ済み、main = 3650e3f）。再計画2の修正も反映済み
- **作業中**: 写真情報パネル（Phase B）。ブランチ `feature/photo-info-panel`。ビルド・単体テスト・実機インストール済み（実機の操作確認は未実施）
- **残り**: 実機確認 → コミット → PR（マージは許可後）

## 要件・計画
- 計画書: [android-album-app-plan.md](./android-album-app-plan.md)
- ADR: [ADR-0001](../docs/adr/ADR-0001-android-tech-stack.md)（Kotlin + Compose）、[ADR-0002](../docs/adr/ADR-0002-thumbnail-listing-and-di.md)（Coil / Paging 3 撤回）、[ADR-0003](../docs/adr/ADR-0003-navigation-settings-and-albums.md)（ナビ・設定・アルバム）、[ADR-0004](../docs/adr/ADR-0004-gesture-handling-and-in-app-picker.md)（ジェスチャー・アプリ内ピッカー）、[ADR-0005](../docs/adr/ADR-0005-exif-info-panel.md)（EXIF 情報パネル）

## 完了済みの内容
- Phase 0: プロジェクト雛形、ビルド環境（JDK 17 / Gradle 8.11.1 / SDK zip 版）
- Phase 1: 権限、MediaStore 読み込み、グリッド、フォルダ表示、ハンバーガーメニュー → 設定、下部タブ（フォト / アルバム / 端末）
- 実機フィードバック1・2 の反映: ピンチで列数変更、全画面の横スワイプ、ピンチ拡大時のパン制限、スクロールバーのオーバーレイと自動非表示、アプリ内ピッカーでのアルバム追加

## 写真情報パネル（作業中）
- 上スワイプ（等倍時）または情報ボタンで下からシートを表示
- 設定の「写真情報」で項目ごとに表示を切り替え（既定 ON: 露出・レンズ・日時・画像の基本項目。既定 OFF: 機材・GPS・向き・色空間）
- 実装: `info/InfoItem.kt`、`info/InfoFormat.kt`（単体テストあり）、`info/PhotoInfoRepository.kt`、`ui/viewer/PhotoInfoSheet.kt`
- 確認項目: 上スワイプで横ページ送りと干渉しないか、情報ボタン、シートの内容、設定のチェック、GPS / 機材項目

## 次のアクション
- 実機で写真情報パネルを確認してもらう（インストールは `install-debug-device.bat`）
- 確認後、`feature/photo-info-panel` にコミットして PR を作成（main への直接マージは禁止。マージはユーザー許可後）
- 次フェーズ候補（優先度順）:
  - A: 120fps・スクロール / ピンチの滑らかさ（Macrobenchmark で計測してから改善）
  - C: Media3 による動画再生（ADR-0006 予定）
  - D: 計算系（被写界深度・超焦点距離・手ブレ限界）、天体系（日の出・ゴールデンアワー）、連写のまとめ
  - E: ピント位置（AF）の可視化（先にスパイクで対応可否を確認）
  - F: フィルタ、お気に入り、ゴミ箱
  - G: 見た目の仕上げ（Apple / Google Photos 風）、タブレット対応

## 発生している課題・特記事項
- Coil 3 は KMP 版 Compose を引き込みビルドが壊れたため撤回（ADR-0002）
- `loadThumbnail` で動画・HEIC が取れるかは未確認
- 横スワイプ（拡大中のパン後のページ送り）と 120fps は未対応
- `AlbumDatabase.create()` は VM 1つ前提。複数インスタンスは未対応

## AIへの伝言
「写真情報パネルまで入ったで。実機で上スワイプしてみてな。」
