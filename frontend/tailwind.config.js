/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        navy: {
          50: '#f0f4ff',
          100: '#dbe4ff',
          700: '#1e3a5f',
          800: '#162d4a',
          900: '#0d1f35',
          950: '#080f1a',
        }
      }
    }
  },
  plugins: []
}
