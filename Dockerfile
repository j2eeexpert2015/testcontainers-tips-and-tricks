# File: Dockerfile
FROM redis:7-alpine

HEALTHCHECK --interval=5s --timeout=3s --start-period=5s --retries=5 \
  CMD redis-cli ping | grep PONG || exit 1
