import type { GameState, Side } from './types'

async function post(url: string, body?: unknown): Promise<GameState> {
  const res = await fetch(url, {
    method: 'POST',
    headers: body ? { 'content-type': 'application/json' } : {},
    body: body ? JSON.stringify(body) : undefined,
  })
  if (!res.ok) {
    throw new ApiError(res.status, await res.text())
  }
  return res.json()
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message)
  }
}

export function newGame(humanSide: Side, thinkTimeMs: number): Promise<GameState> {
  return post('/api/games', { humanSide, thinkTimeMs })
}

export function makeMove(
  gameId: string,
  fromRow: number,
  fromCol: number,
  toRow: number,
  toCol: number,
  promotionPiece?: string,
): Promise<GameState> {
  return post(`/api/games/${gameId}/moves`, {
    fromRow,
    fromCol,
    toRow,
    toCol,
    promotionPiece: promotionPiece ?? null,
  })
}

export function engineMove(gameId: string): Promise<GameState> {
  return post(`/api/games/${gameId}/engine-move`)
}
