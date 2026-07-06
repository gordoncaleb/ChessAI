import { useMemo, useState } from 'react'
import Board from './Board'
import { colorOf } from './pieces'
import { ApiError, engineMove, makeMove, newGame } from './api'
import { isTerminal, type GameState, type Side } from './types'

const THINK_OPTIONS = [
  { label: 'Fast', ms: 600 },
  { label: 'Normal', ms: 1500 },
  { label: 'Strong', ms: 3000 },
]

const key = (r: number, c: number) => `${r},${c}`

export default function App() {
  const [game, setGame] = useState<GameState | null>(null)
  const [selected, setSelected] = useState<[number, number] | null>(null)
  const [promotion, setPromotion] = useState<{ from: [number, number]; to: [number, number] } | null>(null)
  const [thinking, setThinking] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Choices for the "new game" form.
  const [colorChoice, setColorChoice] = useState<'WHITE' | 'BLACK' | 'RANDOM'>('WHITE')
  const [thinkMs, setThinkMs] = useState(1500)

  const orientation: 'white' | 'black' =
    game && game.humanSide === 'BLACK' ? 'black' : 'white'

  const humanColor = game ? (game.humanSide === 'WHITE' ? 'white' : 'black') : 'white'

  const isHumanTurn =
    !!game && !thinking && !isTerminal(game.status) && game.turn === game.humanSide

  const targets = useMemo(() => {
    const set = new Set<string>()
    if (game && selected) {
      for (const m of game.legalMoves) {
        if (m.fromRow === selected[0] && m.fromCol === selected[1]) {
          set.add(key(m.toRow, m.toCol))
        }
      }
    }
    return set
  }, [game, selected])

  const hasMovesFrom = (r: number, c: number) =>
    !!game && game.legalMoves.some((m) => m.fromRow === r && m.fromCol === c)

  async function runEngine(id: string) {
    setThinking(true)
    try {
      const g = await engineMove(id)
      setGame(g)
    } catch (e) {
      reportError(e)
    } finally {
      setThinking(false)
    }
  }

  async function startGame() {
    setError(null)
    setSelected(null)
    setPromotion(null)
    const side: Side =
      colorChoice === 'RANDOM' ? (Math.random() < 0.5 ? 'WHITE' : 'BLACK') : colorChoice
    try {
      const g = await newGame(side, thinkMs)
      setGame(g)
      if (g.engineToMove) runEngine(g.gameId)
    } catch (e) {
      reportError(e)
    }
  }

  async function submitMove(
    from: [number, number],
    to: [number, number],
    promotionPiece?: string,
  ) {
    if (!game) return
    setSelected(null)
    setPromotion(null)
    setError(null)
    try {
      const g = await makeMove(game.gameId, from[0], from[1], to[0], to[1], promotionPiece)
      setGame(g)
      if (g.engineToMove) runEngine(g.gameId)
    } catch (e) {
      reportError(e)
    }
  }

  function handleSquareClick(r: number, c: number) {
    if (!game || !isHumanTurn) return
    const code = game.pieces[r][c]
    const ownPiece = code != null && colorOf(code) === humanColor && hasMovesFrom(r, c)

    if (selected) {
      if (selected[0] === r && selected[1] === c) {
        setSelected(null)
        return
      }
      if (targets.has(key(r, c))) {
        const needsPromotion = game.legalMoves.some(
          (m) =>
            m.fromRow === selected[0] &&
            m.fromCol === selected[1] &&
            m.toRow === r &&
            m.toCol === c &&
            m.promotion,
        )
        if (needsPromotion) {
          setPromotion({ from: selected, to: [r, c] })
        } else {
          submitMove(selected, [r, c])
        }
        return
      }
      setSelected(ownPiece ? [r, c] : null)
      return
    }

    if (ownPiece) setSelected([r, c])
  }

  function reportError(e: unknown) {
    if (e instanceof ApiError) {
      setError(e.status === 422 ? 'That move is not legal.' : `Server error (${e.status}).`)
    } else {
      setError('Could not reach the server. Is the backend running on :7070?')
    }
  }

  return (
    <div className="app">
      <header>
        <h1>Chess</h1>
        <p className="subtitle">You vs. the engine</p>
      </header>

      <main>
        <section className="board-wrap">
          {game ? (
            <Board
              state={game}
              orientation={orientation}
              selected={selected}
              targets={targets}
              onSquareClick={handleSquareClick}
              interactive={isHumanTurn}
            />
          ) : (
            <div className="board placeholder">
              <span>Start a new game to play.</span>
            </div>
          )}
        </section>

        <aside className="panel">
          <div className="card">
            <h2>New game</h2>
            <label>
              Play as
              <div className="segmented">
                {(['WHITE', 'BLACK', 'RANDOM'] as const).map((opt) => (
                  <button
                    key={opt}
                    className={colorChoice === opt ? 'active' : ''}
                    onClick={() => setColorChoice(opt)}
                  >
                    {opt[0] + opt.slice(1).toLowerCase()}
                  </button>
                ))}
              </div>
            </label>
            <label>
              Engine strength
              <div className="segmented">
                {THINK_OPTIONS.map((opt) => (
                  <button
                    key={opt.ms}
                    className={thinkMs === opt.ms ? 'active' : ''}
                    onClick={() => setThinkMs(opt.ms)}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
            </label>
            <button className="primary" onClick={startGame}>
              {game ? 'Restart' : 'Start game'}
            </button>
          </div>

          {game && (
            <div className="card status">
              <h2>Status</h2>
              <StatusLine game={game} thinking={thinking} />
              <dl>
                <div>
                  <dt>You</dt>
                  <dd>{game.humanSide[0] + game.humanSide.slice(1).toLowerCase()}</dd>
                </div>
                <div>
                  <dt>Moves</dt>
                  <dd>{game.moveNumber}</dd>
                </div>
              </dl>
            </div>
          )}

          {error && <div className="card error">{error}</div>}
        </aside>
      </main>

      {promotion && (
        <div className="modal-backdrop" onClick={() => setPromotion(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Promote to</h3>
            <div className="promo-choices">
              {['Q', 'R', 'B', 'N'].map((p) => (
                <button key={p} onClick={() => submitMove(promotion.from, promotion.to, p)}>
                  {p}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function StatusLine({ game, thinking }: { game: GameState; thinking: boolean }) {
  if (thinking) {
    return (
      <p className="status-line thinking">
        <span className="spinner" /> Engine is thinking…
      </p>
    )
  }
  const you = game.humanSide
  switch (game.status) {
    case 'CHECKMATE':
      return (
        <p className="status-line big">
          Checkmate — {game.winner === you ? 'you win! 🎉' : 'engine wins.'}
        </p>
      )
    case 'STALEMATE':
      return <p className="status-line big">Stalemate — it's a draw.</p>
    case 'DRAW':
      return <p className="status-line big">Draw.</p>
    case 'CHECK':
      return (
        <p className="status-line">
          {game.turn === you ? 'You are in check.' : 'Engine is in check.'}
        </p>
      )
    default:
      return (
        <p className="status-line">
          {game.turn === you ? 'Your move.' : 'Waiting for the engine…'}
        </p>
      )
  }
}
