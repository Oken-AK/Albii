---
applyTo: "**"
---

# Git ワークフロー指示書

## ブランチ戦略

- **\\main\\ への直接コミット・マージは絶対禁止**
- \\eature/xxx\\ / \\ix/xxx\\ / \\efactor/xxx\\ / \\docs/xxx\\${nl}
## コミット規約

フォーマット: \\[GitHub Copilot] [カテゴリ] 変更内容の概要\\${nl}
カテゴリ: \\[機能追加]\\ \\[バグ修正]\\ \\[リファクタリング]\\ \\[テスト]\\ \\[ドキュメント]\\ \\[設定]\\ \\[計画書]\\ \\[ADR]\\${nl}
### 例

```
[GitHub Copilot] [機能追加] ユーザー認証機能を実装
[GitHub Copilot] [バグ修正] ログイン時のエラーを修正
[GitHub Copilot] [計画書] Phase 3の計画書を作成
[GitHub Copilot] [ADR] ADR-0003 DBにPostgreSQLを採用
```

## PR の必須内容

```markdown
## 概要

## 変更内容
- ...

## テスト・検証
- [ ] ビルドエラーなし
- [ ] テスト成功

## 関連する計画書・ADR
```
