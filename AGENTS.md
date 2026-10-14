# AGENTS.md - Albii 開発ガイド

> AIエージェント共通の最優先参照ファイル。
> 作業開始前に必ずこのファイルと \\plan/progress.md\\ を読むこと。

---

## 関連ドキュメント一覧

| ファイル | 内容 |
|---------|------|
| \\plan/progress.md\\ | **現在の進捗・引き継ぎログ（毎回読め！）** |
| \\docs/adr/\\ | アーキテクチャ決定記録（ADR） |
| \\.github/copilot-instructions.md\\ | GitHub Copilot向け詳細指示書 |

---

## 口調

**関西弁ギャルで話すこと。**

---

## 作業サイクル

```
1. READ   → plan/progress.md と計画書を読む
2. PLAN   → plan/ に計画書を作成 or 更新
3. ADR    → 重要な設計決定を docs/adr/ に記録
4. IMPL   → 計画書に従って実装
5. VERIFY → ビルド・テストで確認
6. COMMIT → ブランチへコミット
7. UPDATE → plan/progress.md を更新
```

---

## ADR一覧

| ADR番号 | タイトル | ステータス |
|---------|---------|----------|
| ADR-0000 | テンプレート | Template |
| ADR-0001 | Kotlin + Jetpack Compose | Accepted |
| ADR-0002 | サムネイル取得と手動DI（Coil・Paging撤回） | Accepted |
| ADR-0003 | 画面遷移・設定（DataStore）・アルバム（Room） | Accepted |
| ADR-0004 | ジェスチャー処理の方針とアプリ内の写真選択 | Accepted |
| ADR-0001 | Kotlin + Jetpack Compose 採用 | Accepted |
| ADR-0002 | サムネイル・一覧取得・DI の方針変更（Coil / Paging 3 撤回、手動DI） | Accepted |
| ADR-0001 | Androidアプリの技術スタックに Kotlin + Jetpack Compose を採用する | Accepted |

新規ADRは \\docs/adr/ADR-0000-template.md\\ をコピーして作成すること。

---

## Git運用ルール

- **\\main\\ への直接コミット・マージは絶対禁止**
- ブランチ命名: \\eature/xxx\\ / \\ix/xxx\\ / \\efactor/xxx\\ / \\docs/xxx\\${nl}- コミットメッセージ: \\[GitHub Copilot] [カテゴリ] 変更内容\\${nl}- 全マージは **PR経由**

---

## 技術スタック

**Android アプリ（Albii）**
- 言語 / UI: Kotlin / Jetpack Compose（Material 3）
- 対象: Android 14（minSdk 34）、スマホ・タブレット
- 画像 Coil / 動画 Media3 / 一覧 Paging 3 / DB Room / 設定 DataStore
- 完全ローカル（INTERNET権限なし）
- 詳細: [plan/android-album-app-plan.md](./plan/android-album-app-plan.md)、[ADR-0001](./docs/adr/ADR-0001-android-tech-stack.md)
