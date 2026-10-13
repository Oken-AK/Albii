---
applyTo: "docs/adr/**"
---

# ADR（Architecture Decision Records）記述ガイド

## ADRとは

アーキテクチャ上の重要な意思決定を記録するドキュメント。

## いつADRを書くか

1. 技術選定（フレームワーク・ライブラリ・インフラ等）
4. 「なぜそうしたか」が後で不明になりそうな決定

## ファイル命名規則

`docs/adr/ADR-NNNN-短いタイトル.md`

## ステータス

- **Proposed** / **Accepted** / **Deprecated** / **Superseded by ADR-XXXX**

## 注意事項

- ADRは追記・参照のみ。書いたADRを後から書き換えるのはNG
- 方針変更したい場合は新しいADRを書いて「Supersedes ADR-XXXX」と記載する
