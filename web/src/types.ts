// Mirrors the JSON records served by the Javalin backend (Dtos.java).

export type Side = 'WHITE' | 'BLACK'

export type GameStatus =
  | 'IN_PLAY'
  | 'CHECK'
  | 'CHECKMATE'
  | 'STALEMATE'
  | 'DRAW'

/** e.g. "wP" (white pawn), "bK" (black king), or null for an empty square. */
export type PieceCode = string | null

export interface MoveDto {
  fromRow: number
  fromCol: number
  toRow: number
  toCol: number
  promotion: boolean
  promotionPiece: string | null
}

export interface GameState {
  gameId: string
  pieces: PieceCode[][] // [row][col], row 0 = rank 8 (black back rank)
  turn: Side
  humanSide: Side
  status: GameStatus
  inCheck: boolean
  winner: Side | null
  lastMove: [number, number, number, number] | null
  moveNumber: number
  engineToMove: boolean
  legalMoves: MoveDto[]
}

export const TERMINAL: GameStatus[] = ['CHECKMATE', 'STALEMATE', 'DRAW']

export function isTerminal(status: GameStatus): boolean {
  return TERMINAL.includes(status)
}
