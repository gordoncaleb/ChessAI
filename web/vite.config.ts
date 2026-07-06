import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// During dev, Vite serves the UI on :5173 and proxies API calls to the
// Javalin backend on :7070. `npm run build` emits the production bundle
// straight into the ui module's resources so the fat jar can serve it.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:7070',
    },
  },
  build: {
    outDir: '../ui/src/main/resources/public',
    emptyOutDir: true,
  },
})
