---
applyTo: "docs/adr/**"
---

# ADR（Architecture Decision Records）記述ガイド

## ADRとは

アーキテクチャ上の重要な意思決定を記録するドキュメント。
「なぜそうしたのか」を後から追えるようにするための記録。

## いつADRを書くか

1. 技術選定（フレームワーク・ライブラリ・インフラ等）
2. 将来変えにくい設計決定
3. 代替案を検討して選んだもの
4. 「なぜそうしたか」が後で不明になりそうな決定

## ファイル命名規則

\\docs/adr/ADR-NNNN-短いタイトル.md\\${nl}
## ステータス

- **Proposed** / **Accepted** / **Deprecated** / **Superseded by ADR-XXXX**

## 注意事項

- ADRは追記・参照のみ。書いたADRを後から書き換えるのはNG
- 方針変更したい場合は新しいADRを書いて「Supersedes ADR-XXXX」と記載する
