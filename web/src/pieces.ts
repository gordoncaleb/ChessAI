import type { PieceCode } from './types'

// Filled glyphs for every piece; colour is applied via CSS so white and black
// stay legible on both square shades.
const GLYPH: Record<string, string> = {
  K: '♚',
  Q: '♛',
  R: '♜',
  B: '♝',
  N: '♞',
  P: '♟',
}

export function glyph(code: PieceCode): string {
  if (!code) return ''
  return GLYPH[code[1]] ?? ''
}

export function colorOf(code: PieceCode): 'white' | 'black' | null {
  if (!code) return null
  return code[0] === 'w' ? 'white' : 'black'
}
