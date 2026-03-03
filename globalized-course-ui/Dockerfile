# syntax=docker/dockerfile:1

# --- Build stage ---
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies first (layer cache: only re-run if package.json changes)
COPY package*.json ./
RUN npm ci --silent

# Copy source and build
COPY . .
RUN npm run build         # runs tsc --noEmit + vite build

# --- Runtime stage: nginx serves the static bundle ---
FROM nginx:1.25-alpine AS runtime

# Remove default nginx HTML
RUN rm /usr/share/nginx/html/index.html

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy locale files (served as static JSON)
COPY --from=build /app/public/locales /usr/share/nginx/html/locales

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

HEALTHCHECK --interval=30s --timeout=5s \
    CMD wget -qO- http://localhost:80/ > /dev/null || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
