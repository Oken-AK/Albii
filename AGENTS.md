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

新規ADRは \\docs/adr/ADR-0000-template.md\\ をコピーして作成すること。

---

## Git運用ルール

- **\\main\\ への直接コミット・マージは絶対禁止**
- ブランチ命名: \\eature/xxx\\ / \\ix/xxx\\ / \\efactor/xxx\\ / \\docs/xxx\\${nl}- コミットメッセージ: \\[GitHub Copilot] [カテゴリ] 変更内容\\${nl}- 全マージは **PR経由**

---

## 技術スタック

**不明**

> このセクションはプロジェクト固有の内容に書き換えること。
