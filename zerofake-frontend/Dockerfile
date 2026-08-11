# syntax=docker/dockerfile:1

# ---- build ----------------------------------------------------------------
FROM node:22-alpine AS build

WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Vite inlines these at build time, so they are build arguments rather than
# runtime environment variables.
ARG VITE_AUTH_API=http://localhost:8081
ARG VITE_PRODUCT_API=http://localhost:8082
ARG VITE_BLOCKCHAIN_API=http://localhost:8083
ARG VITE_FRAUD_API=http://localhost:8085

ENV VITE_AUTH_API=$VITE_AUTH_API \
    VITE_PRODUCT_API=$VITE_PRODUCT_API \
    VITE_BLOCKCHAIN_API=$VITE_BLOCKCHAIN_API \
    VITE_FRAUD_API=$VITE_FRAUD_API

RUN npm run build

# ---- runtime --------------------------------------------------------------
FROM nginx:1.27-alpine AS runtime

COPY --from=build /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s \
    CMD wget -q --spider http://localhost/ || exit 1
