# ══════════════════════════════════════════════
#  Frontend Dockerfile — React 18 / Vite / Nginx
#  Multi-stage: build static files, serve via Nginx
# ══════════════════════════════════════════════

# Build arguments for environment variables
ARG VITE_API_BASE_URL=/api
ARG VITE_AI_BASE_URL=/ai

# ── Stage 1: Build ─────────────────────────────
FROM node:18-alpine AS builder
WORKDIR /app

# Create .env file with build vars (Vite needs these at build time)
RUN echo "VITE_API_BASE_URL=$VITE_API_BASE_URL" > .env && \
    echo "VITE_AI_BASE_URL=$VITE_AI_BASE_URL" >> .env

# Cache dependencies
COPY package.json package-lock.json* ./
RUN npm install

# Copy source and build
COPY . .
RUN npm run build

# ── Stage 2: Serve ─────────────────────────────
FROM nginx:alpine
# Copy built files
COPY --from=builder /app/dist /usr/share/nginx/html
# Copy custom Nginx configuration for React Router
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
