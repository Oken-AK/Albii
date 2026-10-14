# 進捗・引き継ぎログ

このファイルはAIエージェント間で開発コンテキストを引き継ぐためのログ。
タスクの区切りで必ず最新の状態をここに記録すること。

---

## 現在のステータス
- **プロジェクト**: Albii
- **現在地**: Phase 1 完了。再計画（実機フィードバック）の実装まで完了。ビルド通過。実機未確認
- **残り**: Phase 2b（情報パネル・設定のチェック項目・動画再生）は未着手
- **作業ブランチ**: `feature/android-album-app`

## 要件・計画
- 計画書: [android-album-app-plan.md](./android-album-app-plan.md)
- ADR: [ADR-0001](../docs/adr/ADR-0001-android-tech-stack.md)（Kotlin + Jetpack Compose）、[ADR-0002](../docs/adr/ADR-0002-thumbnail-listing-and-di.md)（Coil / Paging 3 撤回、手動DI）

## 実装済みの内容
- プロジェクトセットアップ（Kiro-style Copilot設定、`docs/adr/`）
- 計画書・ADR-0001 作成
- Phase 0 雛形
  - `settings.gradle.kts` / `build.gradle.kts` / `gradle.properties` / `gradle/libs.versions.toml`
  - `app/build.gradle.kts`（minSdk 34 / targetSdk 35 / Compose有効）
  - `AndroidManifest.xml`（READ_MEDIA_IMAGES・READ_MEDIA_VIDEO のみ。INTERNET なし）
  - `MainActivity.kt`（空のCompose画面）
  - `.gitignore`

## ビルド環境（検証済み）
- JDK 17（Temurin 17.0.20、`%USERPROFILE%\dev\jdk-17.0.20.1+1`）
- Android SDK（`%LOCALAPPDATA%\Android\Sdk`）: platform-tools / platforms;android-35 / build-tools;35.0.0 / build-tools;34.0.0
- Gradle Wrapper 8.11.1 生成済み（`gradlew` / `gradlew.bat`）
- `./gradlew assembleDebug` 成功（`app/build/outputs/apk/debug/app-debug.apk`）
- Android Studio は winget の管理者インストールが UAC 待ちで止まったため、zip 版の SDK とコマンドラインで代替。Studio で開けば同じプロジェクトを扱える
- `local.properties`（`sdk.dir`）は各自の環境で作るもので、Git 管理外

## 再計画（実機フィードバック後）の実装状況
- 右端スクロールバーのラベル二重表示: ラベルを幅無制限で描画する形に修正（実機未確認）
- ピンチで列数変更: `detectTransformGestures`（`ui/grid/MediaGridView.kt`）。列数は 2〜6、既定 4
- 設定（DataStore）とハンバーガーメニュー → 設定画面: `data/SettingsStore.kt`、`ui/main/MainScreen.kt`、`ui/settings/SettingsScreen.kt`
- 写真の全画面表示: `ui/viewer/ViewerScreen.kt`（横スワイプ、ピンチ拡大）
- 下部タブ: フォト / アルバム（Room）/ 端末（フォルダ別）
- アルバム: `data/album/AlbumDatabase.kt`、`ui/albums/`
- iOS 風 UI: `ui/theme/Theme.kt`（配色・角丸・グループ背景）。ぼかし・アニメーションは未実装
- 詳細: [ADR-0003](../docs/adr/ADR-0003-navigation-settings-and-albums.md)

## 再計画2（実機フィードバック2回目）の実装状況
- ピンチ列変更: 親の Initial パスで2本指を検知（`PinchToChangeColumns`）。子の LazyGrid より先に拾う
- ビューア横スワイプ: 等倍時は1本指をPagerに渡す。拡大時のみ消費（`ViewerPage`）
- スクロールバー: グリッドとの重なりを許可（contentPadding 0）。操作しないと1.5秒後にフェードアウト（`FastScrollbar`）
- アルバムへの写真追加: システムピッカーをやめ、アプリ内の `ui/albums/MediaPickerScreen.kt`（4列・複数選択）に変更
- 未着手: アルバム画面の見た目調整、Apple/Google Photos 風 UI の仕上げ、ADR-0004（ジェスチャー方針）
- 詳細: 実機確認待ち

## 次のアクション
- 実機確認（インストールは `install-debug-device.bat`）: ピンチ、ビューア横スワイプ、スクロールバー自動非表示、アプリ内写真追加
- 見た目の調整（アルバム画面・全体の Apple/Google Photos 風）を次フェーズで実施
- Phase 2b: 上スワイプの情報パネル、設定の項目チェック、Media3 による動画再生、ピント位置の可視化
- iOS 風 UI の残り（ぼかし、アニメーション、ジェスチャーの統一）

## 発生している課題・特記事項
- Coil 3 は KMP 版 Compose を引き込み、ビルドが壊れたため撤回（ADR-0002）
- `loadThumbnail` で動画・HEIC が取れない場合の表示は未確認
- Kotlin 関連の plugin（`kotlin-android`）が未適用だったため、テストが動いていなかった。修正済み

## AIへの伝言
「Phase 1 のビルドとテストは通ったで。実機で動かして、見た目と並び順を確認してな。」
