# 進捗・引き継ぎログ

このファイルはAIエージェント間で開発コンテキストを引き継ぐためのログ。
タスクの区切りで必ず最新の状態をここに記録すること。

---

## 現在のステータス
- **プロジェクト**: Albii
- **現在地**: Phase 1（権限 + MediaStore + グリッド）実装済み。ビルド・単体テスト通過。実機未確認
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

## 次のアクション
- Phase 1 の実機確認（権限ダイアログ、グリッド表示、並び順、日別・月別、右端スクロールバー）
- 実機で大量データ（数千件以上）のスクロールを確認し、完了条件を満たすか判定
- Phase 2: 詳細表示（フルサイズ表示・上スワイプで情報パネル）

## 発生している課題・特記事項
- Coil 3 は KMP 版 Compose を引き込み、ビルドが壊れたため撤回（ADR-0002）
- `loadThumbnail` で動画・HEIC が取れない場合の表示は未確認
- Kotlin 関連の plugin（`kotlin-android`）が未適用だったため、テストが動いていなかった。修正済み

## AIへの伝言
「Phase 1 のビルドとテストは通ったで。実機で動かして、見た目と並び順を確認してな。」
