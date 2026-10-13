# 進捗・引き継ぎログ

このファイルはAIエージェント間で開発コンテキストを引き継ぐためのログ。
タスクの区切りで必ず最新の状態をここに記録すること。

---

## 現在のステータス
- **プロジェクト**: Albii
- **現在地**: Phase 0（雛形）作成済み。ビルド検証は未実施
- **作業ブランチ**: `feature/android-album-app`

## 要件・計画
- 計画書: [android-album-app-plan.md](./android-album-app-plan.md)
- ADR: [ADR-0001](../docs/adr/ADR-0001-android-tech-stack.md)（Kotlin + Jetpack Compose）

## 実装済みの内容
- プロジェクトセットアップ（Kiro-style Copilot設定、`docs/adr/`）
- 計画書・ADR-0001 作成
- Phase 0 雛形
  - `settings.gradle.kts` / `build.gradle.kts` / `gradle.properties` / `gradle/libs.versions.toml`
  - `app/build.gradle.kts`（minSdk 34 / targetSdk 35 / Compose有効）
  - `AndroidManifest.xml`（READ_MEDIA_IMAGES・READ_MEDIA_VIDEO のみ。INTERNET なし）
  - `MainActivity.kt`（空のCompose画面）
  - `.gitignore`

## 次のアクション
- Android Studio で開いて Gradle Wrapper を生成（gradlew / gradle-wrapper.jar は未生成）
- `./gradlew assembleDebug` でビルド確認、必要ならバージョン調整
- Phase 1: 権限 + MediaStore + グリッド表示

## 発生している課題・特記事項
- 作業環境に Android SDK・Gradle が無く、Java は 8 のみのため、この時点でビルド検証はしていない
- Hilt の要否は Phase 0 で未判断

## AIへの伝言
「Phase 0 の雛形まで完了。まず Android Studio でビルドを通してから Phase 1 に進んでね。」
