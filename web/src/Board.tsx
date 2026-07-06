import type { GameState } from './types'
import { glyph, colorOf } from './pieces'

const FILES = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h']

interface Props {
  state: GameState
  orientation: 'white' | 'black'
  selected: [number, number] | null
  targets: Set<string> // "row,col" the selected piece can move to
  onSquareClick: (row: number, col: number) => void
  interactive: boolean
}

const key = (r: number, c: number) => `${r},${c}`

export default function Board({
  state,
  orientation,
  selected,
  targets,
  onSquareClick,
  interactive,
}: Props) {
  const rows = orientation === 'white' ? [0, 1, 2, 3, 4, 5, 6, 7] : [7, 6, 5, 4, 3, 2, 1, 0]
  const cols = orientation === 'white' ? [0, 1, 2, 3, 4, 5, 6, 7] : [7, 6, 5, 4, 3, 2, 1, 0]

  const last = state.lastMove
  const isLastSquare = (r: number, c: number) =>
    last != null && ((last[0] === r && last[1] === c) || (last[2] === r && last[3] === c))

  return (
    <div className="board" role="grid" aria-label="Chess board">
      {rows.map((r) =>
        cols.map((c) => {
          const code = state.pieces[r][c]
          const light = (r + c) % 2 === 0
          const isSelected = selected != null && selected[0] === r && selected[1] === c
          const isTarget = targets.has(key(r, c))
          const capture = isTarget && code != null

          const classes = [
            'square',
            light ? 'light' : 'dark',
            isSelected ? 'selected' : '',
            isLastSquare(r, c) ? 'last' : '',
          ]
            .filter(Boolean)
            .join(' ')

          const showRank = c === cols[0]
          const showFile = r === rows[rows.length - 1]

          return (
            <button
              key={key(r, c)}
              className={classes}
              onClick={() => interactive && onSquareClick(r, c)}
              disabled={!interactive}
              aria-label={`${FILES[c]}${8 - r}${code ? ` ${code}` : ''}`}
            >
              {showRank && <span className="coord rank">{8 - r}</span>}
              {showFile && <span className="coord file">{FILES[c]}</span>}
              {isTarget && !capture && <span className="dot" />}
              {capture && <span className="capture-ring" />}
              {code && (
                <span className={`piece ${colorOf(code)}`}>{glyph(code)}</span>
              )}
            </button>
          )
        }),
      )}
    </div>
  )
}
