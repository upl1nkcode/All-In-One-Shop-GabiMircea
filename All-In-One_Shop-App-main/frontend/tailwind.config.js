/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f0f0ff',
          100: '#e0e0ff',
          200: '#c7c7fe',
          300: '#a3a3fc',
          400: '#7c6df8',
          500: '#6c4ff3',
          600: '#5c32e8',
          700: '#4e25cc',
          800: '#4020a5',
          900: '#371e85',
          950: '#210f58',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
