---
applyTo: "**"
---

# Git ワークフロー指示書

## ブランチ戦略

- **`main` への直接コミット・マージは絶対禁止**
- `feature/xxx` / `fix/xxx` / `refactor/xxx` / `docs/xxx`

## コミット規約

フォーマット: `[GitHub Copilot] [カテゴリ] 変更内容の概要`

カテゴリ: `[機能追加]` `[バグ修正]` `[リファクタリング]` `[テスト]` `[ドキュメント]` `[設定]` `[計画書]` `[ADR]`

### 例
```
[GitHub Copilot] [機能追加] ユーザー認証機能を実装
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

## 関連する計画書・ADR
```
